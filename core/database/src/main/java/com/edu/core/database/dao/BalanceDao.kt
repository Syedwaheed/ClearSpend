package com.edu.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.edu.core.database.entity.BalanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BalanceDao {

    @Query("SELECT * FROM balances WHERE accountId = :accountId")
    fun observeBalance(accountId: String): Flow<BalanceEntity?>

    @Upsert
    suspend fun upsert(balance: BalanceEntity)

    @Query("DELETE FROM balances")
    suspend fun clear()
}
