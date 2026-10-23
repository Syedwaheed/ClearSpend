package com.edu.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.edu.core.database.entity.CardBalanceEntity
import com.edu.core.database.entity.CardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {

    @Query("SELECT * FROM cards")
    fun observeCards(): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards")
    suspend fun getCards(): List<CardEntity>

    @Upsert
    suspend fun upsertAll(cards: List<CardEntity>)

    @Query("DELETE FROM cards")
    suspend fun clear()

    /** Replaces the cached card list with a fresh one from the API. */
    @Transaction
    suspend fun replaceAll(cards: List<CardEntity>) {
        clear()
        upsertAll(cards)
    }
}

@Dao
interface CardBalanceDao {

    @Query("SELECT * FROM card_balances WHERE cardId = :cardId")
    fun observeCardBalance(cardId: String): Flow<CardBalanceEntity?>

    @Query("SELECT * FROM card_balances WHERE cardId = :cardId")
    suspend fun getCardBalance(cardId: String): CardBalanceEntity?

    @Upsert
    suspend fun upsert(cardBalance: CardBalanceEntity)

    @Query("DELETE FROM card_balances")
    suspend fun clear()
}
