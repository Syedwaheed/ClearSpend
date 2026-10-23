package com.edu.feature.accounts.data.remote

import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.core.network.ktorclient.get
import com.edu.core.network.model.TrueLayerEnvelope
import com.edu.feature.accounts.data.remote.dto.UserInfoDto
import io.ktor.client.HttpClient

/** `GET /data/v1/info` (scope `info`): account-holder name for the Home greeting. */
interface InfoApi {
    suspend fun getInfo(): Result<TrueLayerEnvelope<UserInfoDto>, DataError.NetworkError>
}

class TrueLayerInfoApi(
    private val dataHttpClient: HttpClient,
) : InfoApi {
    override suspend fun getInfo(): Result<TrueLayerEnvelope<UserInfoDto>, DataError.NetworkError> =
        dataHttpClient.get(route = "/data/v1/info")
}
