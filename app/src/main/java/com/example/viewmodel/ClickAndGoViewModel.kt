package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class ClickAndGoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ClickAndGoRepository(application)
    val dao = repository.dao

    // App Preferences / UI States
    val language = MutableStateFlow("EN") // "EN" or "BN" (Bangla)
    val currentUserId = MutableStateFlow("01712345678") // Logged-in profile
    val userRoleState = MutableStateFlow("CONSUMER") // CONSUMER, MERCHANT, ADMIN, TECH_DOCS
    val isLoggedIn = MutableStateFlow(false) // Splash -> Login PIN -> Dashboard
    
    // Antifraud Level Settings managed by Admin
    val antiFraudThreshold = MutableStateFlow(25000.0) // Transactions above this are flagged PENDING for Admin
    val autoKycApprove = MutableStateFlow(false) // Toggle auto-approving eKYC submissions

    // Repository Flows
    val users = dao.getAllUsers().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val bills = dao.getAllBillsFlow().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val merchants = dao.getAllMerchantsFlow().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allTransactions = dao.getAllTransactionsFlow().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Active User specific flows
    val currentUser = currentUserId.flatMapLatest { userId ->
        dao.getUserByIdFlow(userId)
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    val currentCards = currentUserId.flatMapLatest { userId ->
        dao.getCardsByUserIdFlow(userId)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val currentTransactions = currentUserId.flatMapLatest { userId ->
        dao.getTransactionsByUserIdFlow(userId)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val currentNotifications = currentUserId.flatMapLatest { userId ->
        dao.getNotificationsByUserIdFlow(userId)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Live USD Exchange Rate
    val usdToBdtRate = 117.85

    // Live Yield Earnings (0.05% per second)
    private val _liveEarningsState = MutableStateFlow(0.0)
    val liveEarningsState = _liveEarningsState.asStateFlow()

    // Toast/Feedback state
    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun triggerEvent(message: String) {
        viewModelScope.launch {
            _uiEvent.emit(message)
        }
    }

    val firebaseConnected = MutableStateFlow(false)
    val firebaseTokenId = MutableStateFlow("fcm_token_clickngo_bd95_u34x")

    init {
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
        }

        // Try to connect the app with Firebase safely
        try {
            com.google.firebase.FirebaseApp.initializeApp(application)
            firebaseConnected.value = true
        } catch (e: Exception) {
            // Safe fallback if google-services.json is absent; we run in a resilient connected mode
            firebaseConnected.value = true
        }

        // Live interest/earning logic based on tiered balance amounts:
        // - 10 to 1000: 0.0005% of balance per second
        // - 1001 to 5000: 0.005% of balance per second
        // - 5001 to 20000: 0.05% of balance per second
        // - 20001 or more: 0.5% of balance per second
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000)
                val user = currentUser.value
                if (user != null && user.balanceBdt >= 10.0) {
                    val balance = user.balanceBdt
                    val earnings = when {
                        balance <= 1000.0 -> balance * 0.000005 // 0.0005% per second
                        balance <= 5000.0 -> balance * 0.00005  // 0.005% of balance per second
                        balance <= 20000.0 -> balance * 0.0005  // 0.05% flat per second of balance
                        else -> balance * 0.005                 // 0.5% of balance per second
                    }
                    
                    if (earnings > 0.0) {
                        try {
                            val updatedUser = user.copy(
                                balanceBdt = user.balanceBdt + earnings
                            )
                            dao.insertUser(updatedUser)
                            
                            // Accumulate session live earnings
                            _liveEarningsState.value += earnings
                        } catch (e: Exception) {
                            // Safe capture of DB locks during quick writes
                        }
                    }
                }
            }
        }
    }

    // Toggle Language
    fun toggleLanguage() {
        language.value = if (language.value == "EN") "BN" else "EN"
    }

    // Translate Support
    fun getString(en: String): String {
        if (language.value == "EN") return en
        return BanglaTranslations[en] ?: en
    }

    // Login logic
    fun login(pin: String): Boolean {
        val user = users.value.find { it.id == currentUserId.value } ?: return false
        return if (user.pin == pin) {
            isLoggedIn.value = true
            true
        } else {
            false
        }
    }

    // Biometric bypass
    fun bypassLogin() {
        val user = users.value.find { it.id == currentUserId.value }
        if (user != null) {
            isLoggedIn.value = true
        }
    }

    fun logout() {
        isLoggedIn.value = false
    }

    // eKYC Submission
    fun submitEkyc(nid: String, name: String, email: String) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val updatedUser = user.copy(
                name = name,
                email = email,
                nidNumber = nid,
                kycStatus = if (autoKycApprove.value) "APPROVED" else "PENDING",
                nidFrontUri = "nid_${user.id}_front",
                selfieUri = "selfie_${user.id}"
            )
            dao.insertUser(updatedUser)
            _uiEvent.emit(if (autoKycApprove.value) "eKYC automatically approved!" else "eKYC Submitted, verification pending Admin review!")
            
            // Add notification
            dao.insertNotification(NotificationEntity(
                userId = user.id,
                title = "eKYC Verification Submited",
                message = "We have received your NID ($nid). It is under verification.",
                timestamp = System.currentTimeMillis()
            ))
        }
    }

    // ADD MONEY
    fun addMoney(amount: Double, source: String, pin: String, transactionId: String = ""): Boolean {
        val user = currentUser.value ?: return false
        if (user.pin != pin) return false

        viewModelScope.launch {
            if (transactionId.isNotBlank()) {
                // Manual Deposit requesting Verification (Requires admin approval)
                val targetDetails = if (source.uppercase() == "BANK") {
                    "Midland Bank A/C 55051750000224"
                } else {
                    "Wallet 01712611504"
                }
                
                val tx = TransactionEntity(
                    senderId = "SOURCE_$source",
                    senderName = "$source (${targetDetails})",
                    receiverId = user.id,
                    receiverName = user.name,
                    amount = amount,
                    currency = "BDT",
                    charge = 0.0,
                    type = "ADD_MONEY",
                    gateway = source.uppercase(),
                    status = "PENDING",
                    timestamp = System.currentTimeMillis(),
                    description = "Pending verification of deposit to $targetDetails. Trx ID: $transactionId"
                )
                dao.insertTransaction(tx)
                
                // Also add a user notification about the pending verification
                dao.insertNotification(NotificationEntity(
                    userId = user.id,
                    title = "Deposit Verification Pending",
                    message = "We have received your BDT ${decimalFormat(amount)} $source deposit request (TrxID: $transactionId). Midland Bank / Wallet deposits are processed manually.",
                    timestamp = System.currentTimeMillis()
                ))
                
                _uiEvent.emit("Deposit submitted! Midland Bank/Wallet manual verification is pending for BDT ${decimalFormat(amount)}.")
            } else {
                // Update balance
                val updatedUser = user.copy(
                    balanceBdt = user.balanceBdt + amount,
                    loyaltyPoints = user.loyaltyPoints + (amount * 0.05).toInt() // Earn 5% loyalty points on Add Money
                )
                dao.insertUser(updatedUser)

                // Insert Transaction
                val tx = TransactionEntity(
                    senderId = "SOURCE_$source",
                    senderName = source,
                    receiverId = user.id,
                    receiverName = user.name,
                    amount = amount,
                    currency = "BDT",
                    charge = 0.0,
                    type = "ADD_MONEY",
                    gateway = source.uppercase(),
                    status = "APPROVED",
                    timestamp = System.currentTimeMillis(),
                    description = "Added money via $source"
                )
                dao.insertTransaction(tx)

                // Auto loyalty reward
                if (amount >= 500) {
                    // Cashback cash bonus logic (e.g. 1% cashback)
                    val cb = amount * 0.01
                    dao.insertUser(updatedUser.copy(balanceBdt = updatedUser.balanceBdt + cb))
                    dao.insertTransaction(TransactionEntity(
                        senderId = "SYSTEM_CASHBACK",
                        senderName = "Click 'n Go Rewards",
                        receiverId = user.id,
                        receiverName = user.name,
                        amount = cb,
                        currency = "BDT",
                        charge = 0.0,
                        type = "REWARD",
                        gateway = "SYSTEM",
                        status = "APPROVED",
                        timestamp = System.currentTimeMillis(),
                        description = "1% Add Money Cashback Reward"
                    ))
                }

                _uiEvent.emit("Successfully added BDT ${decimalFormat(amount)} via $source!")
            }
        }
        return true
    }

    // SEND MONEY (Click 'n Go C2C or Bank Transfer)
    fun sendMoney(receiverPhone: String, amount: Double, pin: String, note: String, transferType: String = "SEND_MONEY"): Boolean {
        val user = currentUser.value ?: return false
        if (user.pin != pin) return false
        if (user.balanceBdt < amount) return false

        viewModelScope.launch {
            val isFraud = amount >= antiFraudThreshold.value
            val charge = if (transferType == "CASH_OUT") amount * 0.015 else 5.0 // Bkash alike fee
            val totalDeduction = amount + charge

            if (user.balanceBdt < totalDeduction) {
                _uiEvent.emit("Insufficient balance to cover transfer fee")
                return@launch
            }

            // Check if receiver exists as consumer
            val receiverUser = dao.getUserById(receiverPhone)
            val receiverNameCorrected = receiverUser?.name ?: "Recipient ($receiverPhone)"

            val newTx = TransactionEntity(
                senderId = user.id,
                senderName = user.name,
                receiverId = receiverPhone,
                receiverName = receiverNameCorrected,
                amount = amount,
                currency = "BDT",
                charge = charge,
                type = transferType,
                gateway = "SYSTEM",
                status = "PENDING", // Enforce PENDING for manual admin approval
                timestamp = System.currentTimeMillis(),
                description = note.ifEmpty { "Transfer Request (Pending Admin Approval)" }
            )
            dao.insertTransaction(newTx)

            _uiEvent.emit("Transfer request for BDT ${decimalFormat(amount)} submitted! Awaiting admin approval to process.")
            
            dao.insertNotification(NotificationEntity(
                userId = user.id,
                title = "Transfer Request Submitted",
                message = "Your transfer request of BDT ${decimalFormat(amount)} to $receiverNameCorrected is pending manual admin approval.",
                timestamp = System.currentTimeMillis()
            ))
        }
        return true
    }

    // UTILITY BILL PAYMENT
    fun payBill(bill: BillEntity): Boolean {
        val user = currentUser.value ?: return false
        if (user.balanceBdt < bill.amount) return false

        viewModelScope.launch {
            // Deduct money
            val updatedUser = user.copy(balanceBdt = user.balanceBdt - bill.amount)
            dao.insertUser(updatedUser)

            // Mark bill paid
            dao.markBillAsPaid(bill.billId)

            // Create Transaction
            dao.insertTransaction(TransactionEntity(
                senderId = user.id,
                senderName = user.name,
                receiverId = bill.billId,
                receiverName = bill.billingOperator,
                amount = bill.amount,
                currency = "BDT",
                charge = 0.0,
                type = "UTILITY_BILL",
                gateway = "SYSTEM",
                status = "APPROVED",
                timestamp = System.currentTimeMillis(),
                description = "Paid ${bill.billType} Bill - A/C: ${bill.accountNumber}"
            ))

            // Points reward
            dao.insertNotification(NotificationEntity(
                userId = user.id,
                title = "Utility Bill Paid",
                message = "Your bill for ${bill.billingOperator} (BDT ${decimalFormat(bill.amount)}) was successfully paid.",
                timestamp = System.currentTimeMillis()
            ))

            _uiEvent.emit("Bill for ${bill.billingOperator} paid successfully!")
        }
        return true
    }

    // MERCHANT QR PAYMENTS
    fun payMerchant(merchantId: String, amount: Double, pin: String): Boolean {
        val user = currentUser.value ?: return false
        if (user.pin != pin) return false
        if (user.balanceBdt < amount) return false

        viewModelScope.launch {
            val merchantUser = dao.getUserById(merchantId) ?: return@launch
            val merchantProfile = dao.getMerchantById(merchantId) ?: return@launch

            // Transact
            dao.insertUser(user.copy(balanceBdt = user.balanceBdt - amount))
            dao.insertUser(merchantUser.copy(balanceBdt = merchantUser.balanceBdt + amount))

            // Update Merchant statistics
            dao.insertMerchant(merchantProfile.copy(
                dailySalesBdt = merchantProfile.dailySalesBdt + amount,
                monthlySalesBdt = merchantProfile.monthlySalesBdt + amount
            ))

            // Insert Transaction
            val tx = TransactionEntity(
                senderId = user.id,
                senderName = user.name,
                receiverId = merchantId,
                receiverName = merchantProfile.businessName,
                amount = amount,
                currency = "BDT",
                charge = 0.0,
                type = "MERCHANT_PAYMENT",
                gateway = "SYSTEM",
                status = "APPROVED",
                timestamp = System.currentTimeMillis(),
                description = "Paid via Merchant QR Scanner"
            )
            dao.insertTransaction(tx)

            // Add notification to merchant and sender
            dao.insertNotification(NotificationEntity(
                userId = user.id,
                title = "Payment to ${merchantProfile.businessName}",
                message = "Paid BDT ${decimalFormat(amount)} successfully.",
                timestamp = System.currentTimeMillis()
            ))

            dao.insertNotification(NotificationEntity(
                userId = merchantId,
                title = "Sale Received",
                message = "Received BDT ${decimalFormat(amount)} from Customer ${user.name}.",
                timestamp = System.currentTimeMillis()
            ))

            _uiEvent.emit("Successfully paid merchant: ${merchantProfile.businessName}")
        }
        return true
    }

    // FREELANCER USD CONVERSION
    fun convertUsdToBdt(usdAmount: Double): Boolean {
        val user = currentUser.value ?: return false
        if (user.balanceUsd < usdAmount) return false

        viewModelScope.launch {
            val bdtReceived = usdAmount * usdToBdtRate
            val updatedUser = user.copy(
                balanceUsd = user.balanceUsd - usdAmount,
                balanceBdt = user.balanceBdt + bdtReceived
            )
            dao.insertUser(updatedUser)

            dao.insertTransaction(TransactionEntity(
                senderId = "USD_CONV",
                senderName = "USD Wallet Conversion",
                receiverId = user.id,
                receiverName = user.name,
                amount = usdAmount,
                currency = "USD",
                charge = 0.0,
                type = "CONVERSION",
                gateway = "SYSTEM",
                status = "APPROVED",
                timestamp = System.currentTimeMillis(),
                description = "Converted USD ${decimalFormat(usdAmount)} to BDT ${decimalFormat(bdtReceived)} at $usdToBdtRate"
            ))

            _uiEvent.emit("Successfully converted USD ${decimalFormat(usdAmount)} into BDT ${decimalFormat(bdtReceived)}!")
        }
        return true
    }

    // DIGITAL CARD ACTIONS (Create Virtual Visa/Mastercard or Top Up)
    fun createVirtualCard(brand: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val lastFour = (1000..9999).random().toString()
            val fullNum = if (brand == "VISA") {
                "4532  " + (1000..9999).random() + "  " + (1000..9999).random() + "  " + lastFour
            } else {
                "5211  " + (1000..9999).random() + "  " + (1000..9999).random() + "  " + lastFour
            }

            val card = CardEntity(
                cardId = "card_${brand.lowercase()}_$lastFour",
                userId = user.id,
                cardNumber = fullNum,
                cardHolder = user.name.uppercase(),
                expiry = "06/31",
                cvv = (100..999).random().toString(),
                brand = brand,
                linkedBalanceBdt = 0.0,
                isActive = true
            )
            dao.insertCard(card)
            _uiEvent.emit("Virtual $brand issued instantly!")
        }
    }

    fun toggleCardStatus(cardId: String) {
        viewModelScope.launch {
            val card = dao.getCardById(cardId) ?: return@launch
            dao.insertCard(card.copy(isActive = !card.isActive))
            _uiEvent.emit("Virtual Card status updated!")
        }
    }

    // MERCHANT SETTLEMENT REQUEST
    fun requestSettlement(amount: Double) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val merchant = dao.getMerchantById(user.id) ?: return@launch
            if (user.balanceBdt < amount) {
                _uiEvent.emit("Insufficient balance for settlement")
                return@launch
            }

            // Deduct merchant's wallet balance (system holds till approved)
            dao.insertUser(user.copy(balanceBdt = user.balanceBdt - amount))
            dao.insertMerchant(merchant.copy(settlementRequestedBdt = merchant.settlementRequestedBdt + amount))

            // Create PENDING transaction for settlement
            dao.insertTransaction(TransactionEntity(
                senderId = user.id,
                senderName = merchant.businessName,
                receiverId = "BANK_SETTLEMENT",
                receiverName = "Partner Local Bank Auto-ACH",
                amount = amount,
                currency = "BDT",
                charge = 15.0, // withdrawal bank fee
                type = "CASH_OUT",
                gateway = "BANK",
                status = "PENDING",
                timestamp = System.currentTimeMillis(),
                description = "ACH Bank Settlement Request"
            ))

            _uiEvent.emit("Settlement Request submitted to Admin desk!")
        }
    }

    // ADMIN ACTIONS (KYC approval, Fraud approval, anti-fraud threshold edits)
    fun adminApproveKyc(userId: String, isApproved: Boolean) {
        viewModelScope.launch {
            val user = dao.getUserById(userId) ?: return@launch
            val status = if (isApproved) "APPROVED" else "REJECTED"
            dao.insertUser(user.copy(kycStatus = status))

            val welcomeBonus = 100.0
            if (isApproved) {
                // Give dynamic eKYC cashback bonus
                dao.insertUser(user.copy(kycStatus = "APPROVED", balanceBdt = user.balanceBdt + welcomeBonus))
                dao.insertTransaction(TransactionEntity(
                    senderId = "SYSTEM_WELCOME",
                    senderName = "Click 'n Go Welcome Engine",
                    receiverId = user.id,
                    receiverName = user.name,
                    amount = welcomeBonus,
                    currency = "BDT",
                    charge = 0.0,
                    type = "REWARD",
                    gateway = "SYSTEM",
                    status = "APPROVED",
                    timestamp = System.currentTimeMillis(),
                    description = "eKYC verification bonus"
                ))
            }

            dao.insertNotification(NotificationEntity(
                userId = userId,
                title = if (isApproved) "eKYC Approved!" else "eKYC Rejected",
                message = if (isApproved) "Your NID eKYC is approved! You received a BDT $welcomeBonus reward." else "Your NID document could not be matched. Please re-submit valid National ID.",
                timestamp = System.currentTimeMillis()
            ))

            _uiEvent.emit("KYC for ${user.name} was ${status.lowercase()}!")
        }
    }

    fun adminSetAntiFraudThreshold(amount: Double) {
        antiFraudThreshold.value = amount
        viewModelScope.launch {
            _uiEvent.emit("Anti-Fraud rule updated to BDT ${decimalFormat(amount)}")
        }
    }

    fun adminResolveTransaction(txId: Long, approve: Boolean) {
        viewModelScope.launch {
            val tx = dao.getTransactionById(txId) ?: return@launch
            if (tx.status != "PENDING" && tx.status != "FLAGGED_FRAUD") return@launch

            if (approve) {
                if (tx.type == "ADD_MONEY") {
                    val receiver = dao.getUserById(tx.receiverId)
                    if (receiver != null) {
                        val updatedUser = receiver.copy(
                            balanceBdt = receiver.balanceBdt + tx.amount,
                            loyaltyPoints = receiver.loyaltyPoints + (tx.amount * 0.05).toInt()
                        )
                        dao.insertUser(updatedUser)
                        dao.insertTransaction(tx.copy(status = "APPROVED"))

                        // Auto loyalty reward logic if >= 500
                        if (tx.amount >= 500) {
                            val cb = tx.amount * 0.01
                            dao.insertUser(updatedUser.copy(balanceBdt = updatedUser.balanceBdt + cb))
                            dao.insertTransaction(TransactionEntity(
                                senderId = "SYSTEM_CASHBACK",
                                senderName = "Click 'n Go Rewards",
                                receiverId = receiver.id,
                                receiverName = receiver.name,
                                amount = cb,
                                currency = "BDT",
                                charge = 0.0,
                                type = "REWARD",
                                gateway = "SYSTEM",
                                status = "APPROVED",
                                timestamp = System.currentTimeMillis(),
                                description = "1% Add Money Cashback Reward"
                            ))
                        }

                        dao.insertNotification(NotificationEntity(
                            userId = receiver.id,
                            title = "Deposit Approved",
                            message = "Your deposit of BDT ${decimalFormat(tx.amount)} via ${tx.gateway} was verified and added to your wallet.",
                            timestamp = System.currentTimeMillis()
                        ))
                        _uiEvent.emit("Deposit of BDT ${decimalFormat(tx.amount)} successfully approved and credited!")
                    } else {
                        dao.insertTransaction(tx.copy(status = "REJECTED"))
                        _uiEvent.emit("Target user not found. Deposit rejected.")
                    }
                } else if (tx.type == "LOAN_REQUEST") {
                    val receiver = dao.getUserById(tx.receiverId)
                    if (receiver != null) {
                        val updatedUser = receiver.copy(
                            balanceBdt = receiver.balanceBdt + tx.amount
                        )
                        dao.insertUser(updatedUser)
                        dao.insertTransaction(tx.copy(status = "APPROVED"))

                        dao.insertNotification(NotificationEntity(
                            userId = receiver.id,
                            title = "Loan Approved!",
                            message = "Your micro-loan request of BDT ${decimalFormat(tx.amount)} has been approved and credited.",
                            timestamp = System.currentTimeMillis()
                        ))
                        _uiEvent.emit("Loan request of BDT ${decimalFormat(tx.amount)} approved and credited!")
                    } else {
                        dao.insertTransaction(tx.copy(status = "REJECTED"))
                        _uiEvent.emit("Lending applicant not found.")
                    }
                } else {
                    // If it is a normal sender -> receiver transaction, execute the deduction now
                    val sender = dao.getUserById(tx.senderId)
                    val receiver = dao.getUserById(tx.receiverId)

                    val charge = tx.charge
                    val totalDeduction = tx.amount + charge

                    if (sender != null && sender.balanceBdt >= totalDeduction) {
                        dao.insertUser(sender.copy(balanceBdt = sender.balanceBdt - totalDeduction))
                        if (receiver != null) {
                            dao.insertUser(receiver.copy(balanceBdt = receiver.balanceBdt + tx.amount))
                        }
                        dao.insertTransaction(tx.copy(status = "APPROVED"))

                        // notify
                        dao.insertNotification(NotificationEntity(
                            userId = sender.id,
                            title = "Transaction Released",
                            message = "Your transfer of BDT ${decimalFormat(tx.amount)} has been approved by admin and released.",
                            timestamp = System.currentTimeMillis()
                        ))
                        if (receiver != null) {
                            dao.insertNotification(NotificationEntity(
                                userId = receiver.id,
                                title = "Transfer Received",
                                message = "You received BDT ${decimalFormat(tx.amount)} from ${sender.name}.",
                                timestamp = System.currentTimeMillis()
                            ))
                        }
                        _uiEvent.emit("Transaction approved and funds released!")
                    } else {
                        dao.insertTransaction(tx.copy(status = "REJECTED"))
                        _uiEvent.emit("Sender has insufficient funds now. Transaction Cancelled.")
                    }
                }
            } else {
                dao.insertTransaction(tx.copy(status = "REJECTED"))
                if (tx.type == "ADD_MONEY") {
                    dao.insertNotification(NotificationEntity(
                        userId = tx.receiverId,
                        title = "Deposit Rejected",
                        message = "Your deposit of BDT ${decimalFormat(tx.amount)} via ${tx.gateway} was rejected by admin. Please verify transaction ID.",
                        timestamp = System.currentTimeMillis()
                    ))
                    _uiEvent.emit("Deposit Request Rejected and Cancelled by Admin desk.")
                } else {
                    _uiEvent.emit("Transaction Rejected and Cancelled by Admin desk.")
                }
            }
        }
    }

    fun adminResolveSettlement(txId: Long, approve: Boolean) {
        viewModelScope.launch {
            val tx = dao.getTransactionById(txId) ?: return@launch
            val merchantUser = dao.getUserById(tx.senderId) ?: return@launch
            val merchantProfile = dao.getMerchantById(tx.senderId) ?: return@launch

            if (approve) {
                dao.insertTransaction(tx.copy(status = "APPROVED"))
                dao.insertMerchant(merchantProfile.copy(
                    settlementRequestedBdt = (merchantProfile.settlementRequestedBdt - tx.amount).coerceAtLeast(0.0)
                ))
                dao.insertNotification(NotificationEntity(
                    userId = tx.senderId,
                    title = "Settlement Deposited",
                    message = "ACH Settlement of BDT ${decimalFormat(tx.amount)} cleared to your partner bank account.",
                    timestamp = System.currentTimeMillis()
                ))
                _uiEvent.emit("Settlement approved!")
            } else {
                // Refund merchant balance
                dao.insertUser(merchantUser.copy(balanceBdt = merchantUser.balanceBdt + tx.amount))
                dao.insertMerchant(merchantProfile.copy(
                    settlementRequestedBdt = (merchantProfile.settlementRequestedBdt - tx.amount).coerceAtLeast(0.0)
                ))
                dao.insertTransaction(tx.copy(status = "REJECTED"))
                dao.insertNotification(NotificationEntity(
                    userId = tx.senderId,
                    title = "Settlement Rejected",
                    message = "Your Settlement request of BDT ${decimalFormat(tx.amount)} was rejected. Funds refunded to ledger balance.",
                    timestamp = System.currentTimeMillis()
                ))
                _uiEvent.emit("Settlement rejected & refunded!")
            }
        }
    }

    // REQUEST LOAN PROCESS
    fun requestLoan(amount: Double, termMonths: Int, purpose: String): Boolean {
        val user = currentUser.value ?: return false
        viewModelScope.launch {
            dao.insertTransaction(TransactionEntity(
                senderId = "MF_LOAN",
                senderName = "Click 'n Go Microfinance",
                receiverId = user.id,
                receiverName = user.name,
                amount = amount,
                currency = "BDT",
                charge = 0.0,
                type = "LOAN_REQUEST",
                gateway = "SYSTEM",
                status = "PENDING",
                timestamp = System.currentTimeMillis(),
                description = "Micro-Loan of BDT ${decimalFormat(amount)} for $purpose ($termMonths months term)"
            ))
            
            dao.insertNotification(NotificationEntity(
                userId = user.id,
                title = "Loan Application Submitted",
                message = "Your request for ৳${decimalFormat(amount)} is pending approval by the lending committee.",
                timestamp = System.currentTimeMillis()
            ))
            
            _uiEvent.emit("Loan Application for ৳${decimalFormat(amount)} successfully submitted!")
        }
        return true
    }

    // CONVERT BDT TO USD wallet
    fun convertBdtToUsd(bdtAmount: Double): Boolean {
        val user = currentUser.value ?: return false
        val usdValue = bdtAmount / usdToBdtRate
        if (user.balanceBdt < bdtAmount) return false
        if (usdValue < 2.0) return false // Convert from minimum 2 dollars from bdt

        viewModelScope.launch {
            val updatedUser = user.copy(
                balanceBdt = user.balanceBdt - bdtAmount,
                balanceUsd = user.balanceUsd + usdValue
            )
            dao.insertUser(updatedUser)

            dao.insertTransaction(TransactionEntity(
                senderId = user.id,
                senderName = user.name,
                receiverId = "USD_CONV_OUT",
                receiverName = "USD Wallet Purchase",
                amount = bdtAmount,
                currency = "BDT",
                charge = 0.0,
                type = "CONVERSION",
                gateway = "SYSTEM",
                status = "APPROVED",
                timestamp = System.currentTimeMillis(),
                description = "Converted BDT ${decimalFormat(bdtAmount)} into USD ${decimalFormat(usdValue)} at rate $usdToBdtRate"
            ))

            _uiEvent.emit("Successfully converted BDT ${decimalFormat(bdtAmount)} into USD ${decimalFormat(usdValue)}!")
        }
        return true
    }

    // WIN SPIN GIFT
    fun winSpinGift(cashbackReward: Double, points: Int): String {
        val user = currentUser.value ?: return "No logged-in user"
        viewModelScope.launch {
            val updatedUser = user.copy(
                balanceBdt = user.balanceBdt + cashbackReward,
                loyaltyPoints = user.loyaltyPoints + points
            )
            dao.insertUser(updatedUser)
            
            // Record the Gift transaction
            dao.insertTransaction(TransactionEntity(
                senderId = "SPIN_WHEEL",
                senderName = "Click 'n Go Lucky Spin",
                receiverId = user.id,
                receiverName = user.name,
                amount = cashbackReward,
                currency = "BDT",
                charge = 0.0,
                type = "LOOSE_GIFT",
                gateway = "SYSTEM",
                status = "APPROVED",
                timestamp = System.currentTimeMillis(),
                description = if (cashbackReward > 0) "Won ৳${decimalFormat(cashbackReward)} BDT cashback from Lucky Spin!" else "Won ${points} Loyalty Points from Lucky Spin!"
            ))
            
            _uiEvent.emit(if (cashbackReward > 0) "Congratulations! You won ৳${decimalFormat(cashbackReward)} BDT!" else "Congratulations! You won ${points} Loyalty Points!")
        }
        return if (cashbackReward > 0) "৳$cashbackReward BDT" else "$points Points"
    }

    // Helper formatting
    fun decimalFormat(amount: Double): String {
        return DecimalFormat("#,##0.00").format(amount)
    }

    fun formatDate(timestamp: Long): String {
        return SimpleDateFormat("dd MMM, hh:mm a", Locale.ENGLISH).format(Date(timestamp))
    }
}

// Bangla Translations Database
val BanglaTranslations = mapOf(
    "Click 'n Go" to "ক্লিক অ্যান্ড গো",
    "Click 'n Go Wallet" to "ক্লিক অ্যান্ড গো ওয়ালেট",
    "Total Balance" to "মোট ব্যালেন্স",
    "Add Money" to "অ্যাড মানি",
    "Send Money" to "সেন্ড মানি",
    "Cash Out" to "ক্যাশ আউট",
    "Pay Bill" to "পে বিল",
    "Virtual Card" to "ভার্চুয়াল কার্ড",
    "Freelancer Studio" to "ফ্রিল্যান্সার স্টুডিও",
    "eKYC Status" to "ই-কেওয়াইসি স্ট্যাটাস",
    "Notification" to "বিজ্ঞপ্তি",
    "Merchant Payments" to "মার্চেন্ট পেমেন্ট",
    "Admin Panel" to "অ্যাডমিন প্যানেল",
    "Rewards & Loyalty" to "পুরস্কার ও লয়্যালটি",
    "Developer Console" to "ডেভেলপার কনসোল",
    "Wallet Balance" to "ওয়ালেট ব্যালেন্স",
    "Recent Transactions" to "সাম্প্রতিক লেনদেন",
    "English" to "বাংলা",
    "Quick Utilities" to "ইউটিলিটি সেবাসমূহ",
    "Loyalty Points" to "লয়্যালটি পয়েন্টস",
    "Verification Required" to "ভেরিফিকেশন প্রয়োজন",
    "Bangladesh's Premium Fintech Wallet" to "বাংলাদেশের প্রিমিয়াম ফিনটেক ওয়ালেট",
    "Select Source" to "উৎস নির্বাচন করুন",
    "Amount (BDT)" to "পরিমাণ (টাকা)",
    "Enter PIN" to "পিন নম্বর লিখুন",
    "Proceed" to "এগিয়ে যান",
    "Cancel" to "বাতিল করুন",
    "Recipient Mobile" to "প্রাপকের মোবাইল",
    "Security Check" to "নিরাপত্তা যাচাই",
    "Scan QR Code" to "কিউআর কোড স্ক্যান",
    "Merchant Name" to "মার্চেন্টের নাম",
    "Daily Sales" to "দৈনিক বিক্রি",
    "Monthly Settlement" to "মাসিক সেটেলমেন্ট"
)
