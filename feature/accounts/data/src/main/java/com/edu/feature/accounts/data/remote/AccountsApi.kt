package com.edu.feature.accounts.data.remote

import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.core.network.ktorclient.get
import com.edu.core.network.model.TrueLayerEnvelope
import com.edu.feature.accounts.data.remote.dto.AccountDto
import com.edu.feature.accounts.data.remote.dto.BalanceDto
import io.ktor.client.HttpClient

/** Data API accounts endpoints (scopes `accounts`, `balance`). */
interface AccountsApi {
    suspend fun getAccounts(): Result<TrueLayerEnvelope<AccountDto>, DataError.NetworkError>
    suspend fun getAccount(accountId: String): Result<TrueLayerEnvelope<AccountDto>, DataError.NetworkError>
    suspend fun getBalance(accountId: String): Result<TrueLayerEnvelope<BalanceDto>, DataError.NetworkError>
}

/** Every call goes through core:network's shared `get` (route building + error mapping). */
class TrueLayerAccountsApi(
    private val dataHttpClient: HttpClient,
) : AccountsApi {

    override suspend fun getAccounts(): Result<TrueLayerEnvelope<AccountDto>, DataError.NetworkError> =
        dataHttpClient.get(route = "/data/v1/accounts")

    override suspend fun getAccount(accountId: String): Result<TrueLayerEnvelope<AccountDto>, DataError.NetworkError> =
        dataHttpClient.get(route = "/data/v1/accounts/$accountId")

    override suspend fun getBalance(accountId: String): Result<TrueLayerEnvelope<BalanceDto>, DataError.NetworkError> =
        dataHttpClient.get(route = "/data/v1/accounts/$accountId/balance")
}
