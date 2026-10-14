package com.edu.core.database.dao

import androidx.room.Dao
import androidx.room.Query
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

    @Query("SELECT * FROM transactions WHERE accountId = :accountId AND isPending = 1")
    fun observePendingTransactions(accountId: String): Flow<List<TransactionEntity>>

    @Upsert
    suspend fun upsertAll(transactions: List<TransactionEntity>)

    @Query("DELETE FROM transactions WHERE accountId = :accountId")
    suspend fun clear(accountId: String)
}
