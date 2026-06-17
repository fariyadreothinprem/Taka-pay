package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ==========================================
// 1. ENTITIES
// ==========================================

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String, // Mobile number (e.g. 01712345678)
    val name: String,
    val email: String,
    val pin: String,
    val kycStatus: String, // "NOT_STARTED", "PENDING", "APPROVED", "REJECTED"
    val nidNumber: String = "",
    val nidFrontUri: String = "",
    val selfieUri: String = "",
    val role: String, // "CONSUMER", "MERCHANT", "ADMIN"
    val balanceBdt: Double,
    val balanceUsd: Double,
    val loyaltyPoints: Int,
    val referralsCount: Int,
    val isBiometricEnabled: Boolean = false
)

@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey val cardId: String,
    val userId: String,
    val cardNumber: String,
    val cardHolder: String,
    val expiry: String,
    val cvv: String,
    val brand: String, // "VISA", "MASTERCARD"
    val linkedBalanceBdt: Double,
    val isActive: Boolean
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val txId: Long = 0L,
    val senderId: String,
    val senderName: String,
    val receiverId: String,
    val receiverName: String,
    val amount: Double,
    val currency: String, // "BDT", "USD"
    val charge: Double,
    val type: String, // "SEND_MONEY", "ADD_MONEY", "CASH_OUT", "UTILITY_BILL", "MERCHANT_PAYMENT", "REWARD", "CONVERSION"
    val gateway: String, // "BKASH", "NAGAD", "ROCKET", "CARD", "BANK", "SYSTEM"
    val status: String, // "PENDING", "APPROVED", "REJECTED", "FLAGGED_FRAUD"
    val timestamp: Long,
    val description: String
)

@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey val billId: String,
    val billingOperator: String, // "DESCO", "WASA", "Titas Gas", "Amber IT", "Grameenphone"
    val billType: String, // "Electricity", "Water", "Gas", "Internet", "Recharge"
    val accountNumber: String,
    val amount: Double,
    val isPaid: Boolean,
    val dueDate: String
)

@Entity(tableName = "merchants")
data class MerchantEntity(
    @PrimaryKey val merchantId: String, // Matches UserEntity.id
    val businessName: String,
    val category: String, // "Retail", "Groceries", "Food", "Tech", "Freelancer Studio"
    val dailySalesBdt: Double,
    val monthlySalesBdt: Double,
    val settlementRequestedBdt: Double,
    val staticQrCodeData: String
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userId: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean = false
)

// ==========================================
// 2. DAOS
// ==========================================

@Dao
interface ClickAndGoDao {
    // User Queries
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserByIdFlow(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    // Card Queries
    @Query("SELECT * FROM cards WHERE userId = :userId")
    fun getCardsByUserIdFlow(userId: String): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE cardId = :cardId LIMIT 1")
    suspend fun getCardById(cardId: String): CardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CardEntity)

    // Transaction Queries
    @Query("SELECT * FROM transactions WHERE senderId = :userId OR receiverId = :userId ORDER BY timestamp DESC")
    fun getTransactionsByUserIdFlow(userId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactionsFlow(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE txId = :txId LIMIT 1")
    suspend fun getTransactionById(txId: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    // Bill Queries
    @Query("SELECT * FROM bills")
    fun getAllBillsFlow(): Flow<List<BillEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: BillEntity)

    @Query("UPDATE bills SET isPaid = 1 WHERE billId = :billId")
    suspend fun markBillAsPaid(billId: String)

    // Merchant Queries
    @Query("SELECT * FROM merchants WHERE merchantId = :merchantId LIMIT 1")
    fun getMerchantByIdFlow(merchantId: String): Flow<MerchantEntity?>

    @Query("SELECT * FROM merchants WHERE merchantId = :merchantId LIMIT 1")
    suspend fun getMerchantById(merchantId: String): MerchantEntity?

    @Query("SELECT * FROM merchants")
    fun getAllMerchantsFlow(): Flow<List<MerchantEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMerchant(merchant: MerchantEntity)

    // Notification Queries
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getNotificationsByUserIdFlow(userId: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markNotificationsAsRead(userId: String)
}

// ==========================================
// 3. DATABASE
// ==========================================

@Database(
    entities = [
        UserEntity::class,
        CardEntity::class,
        TransactionEntity::class,
        BillEntity::class,
        MerchantEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ClickAndGoDatabase : RoomDatabase() {
    abstract fun dao(): ClickAndGoDao
}
