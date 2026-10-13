package com.example.data.db

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getUserProfileSync(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateUserProfile(profile: UserProfile)

    // Wallet
    @Query("SELECT * FROM wallet_account WHERE id = 1")
    fun getWalletFlow(): Flow<WalletAccount?>

    @Query("SELECT * FROM wallet_account WHERE id = 1")
    suspend fun getWalletSync(): WalletAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateWallet(wallet: WalletAccount)

    // Draw Rounds
    @Query("SELECT * FROM draw_rounds WHERE gameMode = :mode ORDER BY timestamp DESC LIMIT 60")
    fun getRecentRoundsFlow(mode: String): Flow<List<DrawRound>>

    @Query("SELECT * FROM draw_rounds WHERE gameMode = :mode ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestRound(mode: String): DrawRound?

    @Query("SELECT COUNT(*) FROM draw_rounds WHERE gameMode = :mode")
    suspend fun getRoundCount(mode: String): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRound(round: DrawRound)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRounds(rounds: List<DrawRound>)

    // User Bets
    @Query("SELECT * FROM user_bets ORDER BY timestamp DESC LIMIT 100")
    fun getAllBetsFlow(): Flow<List<UserBet>>

    @Query("SELECT * FROM user_bets WHERE periodId = :periodId")
    suspend fun getBetsForPeriod(periodId: String): List<UserBet>

    @Query("SELECT * FROM user_bets ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastBet(): UserBet?

    @Insert
    suspend fun insertBet(bet: UserBet): Long

    @Update
    suspend fun updateBet(bet: UserBet)

    @Update
    suspend fun updateBets(bets: List<UserBet>)

    // Transactions
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT 60")
    fun getTransactionsFlow(): Flow<List<TransactionRecord>>

    @Query("SELECT * FROM transactions WHERE category = 'WITHDRAWAL' AND status = 'PROCESSING' ORDER BY timestamp DESC")
    fun getPendingWithdrawalsFlow(): Flow<List<TransactionRecord>>

    @Query("UPDATE transactions SET status = :status WHERE id = :id")
    suspend fun updateTransactionStatus(id: Long, status: String)

    @Insert
    suspend fun insertTransaction(transaction: TransactionRecord)
}
