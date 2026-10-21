package com.edu.feature.auth.data.remote

import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.core.network.ktorclient.get
import com.edu.core.network.model.TrueLayerEnvelope
import com.edu.feature.auth.data.remote.dto.UserInfoDto
import io.ktor.client.HttpClient

/** `GET /data/v1/info` (scope `info`), used to greet the user on screen 08. */
interface UserInfoApi {
    suspend fun getUserInfo(): Result<TrueLayerEnvelope<UserInfoDto>, DataError.NetworkError>
}

class TrueLayerUserInfoApi(
    private val dataHttpClient: HttpClient,
) : UserInfoApi {
    override suspend fun getUserInfo(): Result<TrueLayerEnvelope<UserInfoDto>, DataError.NetworkError> =
        dataHttpClient.get(route = "/data/v1/info")
}
