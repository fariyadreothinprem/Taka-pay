package com.example.data

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.*

class ClickAndGoRepository(private val context: Context) {

    private val db: ClickAndGoDatabase = Room.databaseBuilder(
        context.applicationContext,
        ClickAndGoDatabase::class.java,
        "click_and_go_db"
    )
    .fallbackToDestructiveMigration()
    .build()

    val dao: ClickAndGoDao = db.dao()

    suspend fun prepopulateIfEmpty() {
        // Checking if we already have users
        val users = dao.getAllUsers().firstOrNull() ?: emptyList()
        if (users.isEmpty()) {
            // 1. Insert System Users
            val consumer = UserEntity(
                id = "01712345678",
                name = "Anisur Rahman",
                email = "anis@clickngo.com.bd",
                pin = "1234",
                kycStatus = "APPROVED",
                nidNumber = "1994261456789",
                nidFrontUri = "nid_anisur_front",
                selfieUri = "selfie_anisur",
                role = "CONSUMER",
                balanceBdt = 8500.00,
                balanceUsd = 120.00,
                loyaltyPoints = 1250,
                referralsCount = 3,
                isBiometricEnabled = true
            )

            val merchant = UserEntity(
                id = "01812345678",
                name = "Dhaka Supermart",
                email = "dhakasuper@commercial.bd",
                pin = "1122",
                kycStatus = "APPROVED",
                nidNumber = "1988261111222",
                role = "MERCHANT",
                balanceBdt = 45500.00,
                balanceUsd = 0.00,
                loyaltyPoints = 0,
                referralsCount = 0,
                isBiometricEnabled = false
            )

            val admin = UserEntity(
                id = "01912345678",
                name = "Fintech Ops Manager",
                email = "manager@clickngo.gov.bd",
                pin = "9999",
                kycStatus = "APPROVED",
                role = "ADMIN",
                balanceBdt = 1250000.00,
                balanceUsd = 4500.00,
                loyaltyPoints = 0,
                referralsCount = 0,
                isBiometricEnabled = false
            )

            dao.insertUser(consumer)
            dao.insertUser(merchant)
            dao.insertUser(admin)

            // 2. Insert initial virtual cards
            val visaCard = CardEntity(
                cardId = "card_visa_8820",
                userId = "01712345678",
                cardNumber = "4152  8830  2210  8820",
                cardHolder = "ANISUR RAHMAN",
                expiry = "08/29",
                cvv = "415",
                brand = "VISA",
                linkedBalanceBdt = 5000.00,
                isActive = true
            )
            val masterCard = CardEntity(
                cardId = "card_master_1102",
                userId = "01712345678",
                cardNumber = "5412  9922  0055  1102",
                cardHolder = "ANISUR RAHMAN",
                expiry = "12/30",
                cvv = "992",
                brand = "MASTERCARD",
                linkedBalanceBdt = 0.00,
                isActive = false
            )
            dao.insertCard(visaCard)
            dao.insertCard(masterCard)

            // 3. Insert merchant profile details
            val merchantDetails = MerchantEntity(
                merchantId = "01812345678",
                businessName = "Dhaka Supermart",
                category = "Groceries & Retail",
                dailySalesBdt = 12500.00,
                monthlySalesBdt = 378000.00,
                settlementRequestedBdt = 0.00,
                staticQrCodeData = "clickngo://pay?merchantId=01812345678&merchantName=Dhaka%20Supermart"
            )
            dao.insertMerchant(merchantDetails)

            // 4. Insert default transactions for consumer user
            val now = System.currentTimeMillis()
            val dayMs = 24 * 60 * 60 * 1000L

            dao.insertTransaction(TransactionEntity(
                senderId = "BANK_SCB",
                senderName = "Standard Chartered",
                receiverId = "01712345678",
                receiverName = "Anisur Rahman",
                amount = 10000.00,
                currency = "BDT",
                charge = 0.0,
                type = "ADD_MONEY",
                gateway = "BANK",
                status = "APPROVED",
                timestamp = now - 3 * dayMs,
                description = "Deposit from SCB Account"
            ))

            dao.insertTransaction(TransactionEntity(
                senderId = "01712345678",
                senderName = "Anisur Rahman",
                receiverId = "01812345678",
                receiverName = "Dhaka Supermart",
                amount = 1500.00,
                currency = "BDT",
                charge = 0.0,
                type = "MERCHANT_PAYMENT",
                gateway = "SYSTEM",
                status = "APPROVED",
                timestamp = now - 2 * dayMs,
                description = "Weekly grocery payment"
            ))

            dao.insertTransaction(TransactionEntity(
                senderId = "PAYPAL_RECEIVE",
                senderName = "Upwork Freelance Inc",
                receiverId = "01712345678",
                receiverName = "Anisur Rahman",
                amount = 120.00,
                currency = "USD",
                charge = 2.4,
                type = "REWARD", // International code
                gateway = "SYSTEM",
                status = "APPROVED",
                timestamp = now - 1 * dayMs,
                description = "Upwork Freelance Earning (USD Wallet)"
            ))

            dao.insertTransaction(TransactionEntity(
                senderId = "01712345678",
                senderName = "Anisur Rahman",
                receiverId = "DESCO_BILL",
                receiverName = "DESCO",
                amount = 1200.00,
                currency = "BDT",
                charge = 5.0,
                type = "UTILITY_BILL",
                gateway = "SYSTEM",
                status = "APPROVED",
                timestamp = now - 12 * 3600 * 1000,
                description = "DESCO Electricity Payment"
            ))

            // 5. Insert mock bills
            dao.insertBill(BillEntity("desco_jun26", "DESCO", "Electricity", "1002994821", 1680.00, false, "25-06-2026"))
            dao.insertBill(BillEntity("wasa_jun26", "Dhaka WASA", "Water", "8820194851", 450.00, false, "30-06-2026"))
            dao.insertBill(BillEntity("titas_jun26", "Titas Gas", "Gas", "309485123", 1080.00, false, "20-06-2026"))
            dao.insertBill(BillEntity("amber_jun26", "Amber IT", "Internet", "AIT-99281", 575.00, false, "18-06-2026"))
            dao.insertBill(BillEntity("gp_recharge", "Grameenphone", "Recharge", "01712345678", 50.00, true, "Paid Today"))

            // 6. Insert initial welcome notification
            dao.insertNotification(NotificationEntity(
                userId = "01712345678",
                title = "Welcome to Click 'n Go!",
                message = "Explore digital wallet services in Bangladesh. Verify your NID eKYC to experience full limits.",
                timestamp = now,
                isRead = false
            ))
        }
    }
}
