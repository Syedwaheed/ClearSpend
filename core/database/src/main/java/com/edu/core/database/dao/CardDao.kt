package com.edu.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.edu.core.database.entity.CardBalanceEntity
import com.edu.core.database.entity.CardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {

    @Query("SELECT * FROM cards")
    fun observeCards(): Flow<List<CardEntity>>

    @Upsert
    suspend fun upsertAll(cards: List<CardEntity>)

    @Query("DELETE FROM cards")
    suspend fun clear()
}

@Dao
interface CardBalanceDao {

    @Query("SELECT * FROM card_balances WHERE cardId = :cardId")
    fun observeCardBalance(cardId: String): Flow<CardBalanceEntity?>

    @Upsert
    suspend fun upsert(cardBalance: CardBalanceEntity)

    @Query("DELETE FROM card_balances")
    suspend fun clear()
}
