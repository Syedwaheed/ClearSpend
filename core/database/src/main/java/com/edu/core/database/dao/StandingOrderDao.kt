package com.edu.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.edu.core.database.entity.StandingOrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StandingOrderDao {

    @Query("SELECT * FROM standing_orders WHERE accountId = :accountId")
    fun observeStandingOrders(accountId: String): Flow<List<StandingOrderEntity>>

    @Upsert
    suspend fun upsertAll(standingOrders: List<StandingOrderEntity>)

    @Query("DELETE FROM standing_orders WHERE accountId = :accountId")
    suspend fun clear(accountId: String)
}
