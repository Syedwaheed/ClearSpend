package com.edu.feature.transactions.data.remote

import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.core.network.ktorclient.get
import com.edu.core.network.model.TrueLayerEnvelope
import com.edu.feature.transactions.data.remote.dto.TransactionDto
import io.ktor.client.HttpClient
import java.time.LocalDate

/** Data API transaction endpoints (scope `transactions`). */
interface TransactionsApi {
    suspend fun getTransactions(
        accountId: String,
        from: LocalDate,
        to: LocalDate,
    ): Result<TrueLayerEnvelope<TransactionDto>, DataError.NetworkError>

    suspend fun getPendingTransactions(accountId: String): Result<TrueLayerEnvelope<TransactionDto>, DataError.NetworkError>
}

class TrueLayerTransactionsApi(
    private val dataHttpClient: HttpClient,
) : TransactionsApi {

    override suspend fun getTransactions(
        accountId: String,
        from: LocalDate,
        to: LocalDate,
    ): Result<TrueLayerEnvelope<TransactionDto>, DataError.NetworkError> = dataHttpClient.get(
        route = "/data/v1/accounts/$accountId/transactions",
        queryParameters = mapOf("from" to from.toString(), "to" to to.toString()),
    )

    override suspend fun getPendingTransactions(
        accountId: String,
    ): Result<TrueLayerEnvelope<TransactionDto>, DataError.NetworkError> =
        dataHttpClient.get(route = "/data/v1/accounts/$accountId/transactions/pending")
}
