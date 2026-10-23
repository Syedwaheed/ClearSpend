package com.edu.feature.accounts.data.repository

import com.edu.core.data.cache.networkFirst
import com.edu.core.data.cache.networkFirstList
import com.edu.core.database.dao.CardBalanceDao
import com.edu.core.database.dao.CardDao
import com.edu.core.domain.model.Card
import com.edu.core.domain.model.CardBalance
import com.edu.core.domain.repository.CardRepository
import com.edu.core.domain.storage.SyncStatusStorage
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.core.domain.util.map
import com.edu.feature.accounts.data.mapper.CardMapper
import com.edu.feature.accounts.data.remote.CardsApi

/** `GET /data/v1/cards` + `/cards/{id}/balance`, cached in Room for offline use. */
class CardRepositoryImpl(
    private val api: CardsApi,
    private val cardDao: CardDao,
    private val cardBalanceDao: CardBalanceDao,
    private val syncStatus: SyncStatusStorage,
    private val mapper: CardMapper = CardMapper(),
) : CardRepository {

    override suspend fun getCards(): Result<List<Card>, DataError> = networkFirstList(
        syncStatus = syncStatus,
        fetch = { api.getCards().map { envelope -> envelope.results.map(mapper::toDomain) } },
        saveToCache = { cards -> cardDao.replaceAll(cards.map(mapper::toEntity)) },
        readCache = { cardDao.getCards().map(mapper::fromEntity) },
    )

    override suspend fun getCardBalance(cardId: String): Result<CardBalance, DataError> = networkFirst(
        syncStatus = syncStatus,
        fetch = {
            when (val result = api.getCardBalance(cardId)) {
                is Result.Success -> result.data.results.firstOrNull()
                    ?.let { Result.Success(mapper.toDomain(cardId, it)) }
                    ?: Result.Error(DataError.NetworkError.NOT_FOUND)
                is Result.Error -> result
            }
        },
        saveToCache = { balance -> cardBalanceDao.upsert(mapper.toEntity(balance)) },
        readCache = { cardBalanceDao.getCardBalance(cardId)?.let(mapper::fromEntity) },
    )
}
