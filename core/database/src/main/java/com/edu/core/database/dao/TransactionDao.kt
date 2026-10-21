package com.edu.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.edu.core.database.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query(
        "SELECT * FROM transactions WHERE accountId = :accountId AND isPending = 0 " +
            "AND timestampEpochMillis BETWEEN :fromEpochMillis AND :toEpochMillis " +
            "ORDER BY timestampEpochMillis DESC",
    )
    fun observeTransactions(
        accountId: String,
        fromEpochMillis: Long,
        toEpochMillis: Long,
    ): Flow<List<TransactionEntity>>

    @Query(
        "SELECT * FROM transactions WHERE accountId = :accountId AND isPending = 0 " +
            "AND timestampEpochMillis BETWEEN :fromEpochMillis AND :toEpochMillis " +
            "ORDER BY timestampEpochMillis DESC",
    )
    suspend fun getTransactions(
        accountId: String,
        fromEpochMillis: Long,
        toEpochMillis: Long,
    ): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId AND isPending = 1 ORDER BY timestampEpochMillis DESC")
    suspend fun getPendingTransactions(accountId: String): List<TransactionEntity>

    /** One cached transaction (settled or pending) — Transaction Detail (14-16) loads by id. */
    @Query("SELECT * FROM transactions WHERE transactionId = :transactionId LIMIT 1")
    suspend fun getTransaction(transactionId: String): TransactionEntity?

    @Query("DELETE FROM transactions WHERE accountId = :accountId AND isPending = 1")
    suspend fun clearPending(accountId: String)

    /** Pending transactions are a snapshot: replace them wholesale on every sync. */
    @Transaction
    suspend fun replacePending(accountId: String, transactions: List<TransactionEntity>) {
        clearPending(accountId)
        upsertAll(transactions)
    }

    @Query("SELECT * FROM transactions WHERE accountId = :accountId AND isPending = 1")
    fun observePendingTransactions(accountId: String): Flow<List<TransactionEntity>>

    @Upsert
    suspend fun upsertAll(transactions: List<TransactionEntity>)

    @Query("DELETE FROM transactions WHERE accountId = :accountId")
    suspend fun clear(accountId: String)
}
