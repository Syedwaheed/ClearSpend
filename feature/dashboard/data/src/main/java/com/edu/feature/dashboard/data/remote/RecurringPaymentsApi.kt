package com.edu.feature.dashboard.data.remote

import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.core.network.ktorclient.get
import com.edu.core.network.model.TrueLayerEnvelope
import com.edu.feature.dashboard.data.remote.dto.DirectDebitDto
import com.edu.feature.dashboard.data.remote.dto.StandingOrderDto
import io.ktor.client.HttpClient

/** Data API recurring-payment endpoints (scopes `direct_debits`, `standing_orders`). */
interface RecurringPaymentsApi {
    suspend fun getDirectDebits(accountId: String): Result<TrueLayerEnvelope<DirectDebitDto>, DataError.NetworkError>
    suspend fun getStandingOrders(accountId: String): Result<TrueLayerEnvelope<StandingOrderDto>, DataError.NetworkError>
}

class TrueLayerRecurringPaymentsApi(
    private val dataHttpClient: HttpClient,
) : RecurringPaymentsApi {

    override suspend fun getDirectDebits(accountId: String): Result<TrueLayerEnvelope<DirectDebitDto>, DataError.NetworkError> =
        dataHttpClient.get(route = "/data/v1/accounts/$accountId/direct_debits")

    override suspend fun getStandingOrders(accountId: String): Result<TrueLayerEnvelope<StandingOrderDto>, DataError.NetworkError> =
        dataHttpClient.get(route = "/data/v1/accounts/$accountId/standing_orders")
}
