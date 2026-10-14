package com.edu.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.edu.core.database.entity.DirectDebitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DirectDebitDao {

    @Query("SELECT * FROM direct_debits WHERE accountId = :accountId")
    fun observeDirectDebits(accountId: String): Flow<List<DirectDebitEntity>>

    @Upsert
    suspend fun upsertAll(directDebits: List<DirectDebitEntity>)

    @Query("DELETE FROM direct_debits WHERE accountId = :accountId")
    suspend fun clear(accountId: String)
}
