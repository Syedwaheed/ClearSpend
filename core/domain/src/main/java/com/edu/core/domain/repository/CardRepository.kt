package com.edu.core.domain.repository

import com.edu.core.domain.model.Card
import com.edu.core.domain.model.CardBalance
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result

interface CardRepository {
    suspend fun getCards(): Result<List<Card>, DataError>
    suspend fun getCardBalance(cardId: String): Result<CardBalance, DataError>
}
