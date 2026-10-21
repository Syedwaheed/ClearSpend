package com.edu.feature.accounts.data.remote

import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.core.network.ktorclient.get
import com.edu.core.network.model.TrueLayerEnvelope
import com.edu.feature.accounts.data.remote.dto.CardBalanceDto
import com.edu.feature.accounts.data.remote.dto.CardDto
import io.ktor.client.HttpClient

/** Data API cards endpoints (scope `cards`). */
interface CardsApi {
    suspend fun getCards(): Result<TrueLayerEnvelope<CardDto>, DataError.NetworkError>
    suspend fun getCardBalance(cardId: String): Result<TrueLayerEnvelope<CardBalanceDto>, DataError.NetworkError>
}

class TrueLayerCardsApi(
    private val dataHttpClient: HttpClient,
) : CardsApi {

    override suspend fun getCards(): Result<TrueLayerEnvelope<CardDto>, DataError.NetworkError> =
        dataHttpClient.get(route = "/data/v1/cards")

    override suspend fun getCardBalance(cardId: String): Result<TrueLayerEnvelope<CardBalanceDto>, DataError.NetworkError> =
        dataHttpClient.get(route = "/data/v1/cards/$cardId/balance")
}
