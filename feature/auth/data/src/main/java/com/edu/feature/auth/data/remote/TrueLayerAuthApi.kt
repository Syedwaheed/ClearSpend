package com.edu.feature.auth.data.remote

import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.core.network.ktorclient.constructRoute
import com.edu.core.network.ktorclient.safeCall
import com.edu.feature.auth.data.TrueLayerAuthConfig
import com.edu.feature.auth.data.remote.dto.TokenResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.submitForm
import io.ktor.http.Parameters
import io.ktor.http.parameters

/** Sandbox-only [AuthApi]: talks to TrueLayer directly with the on-device secret. */
class TrueLayerAuthApi(
    private val httpClient: HttpClient,
    private val config: TrueLayerAuthConfig,
) : AuthApi {

    override suspend fun exchangeAuthorizationCode(
        code: String,
    ): Result<TokenResponseDto, DataError.NetworkError> = postToken(
        parameters {
            append("grant_type", "authorization_code")
            append("client_id", config.clientId)
            append("client_secret", config.clientSecret)
            append("redirect_uri", config.redirectUri)
            append("code", code)
        },
    )

    override suspend fun refreshAccessToken(
        refreshToken: String,
    ): Result<TokenResponseDto, DataError.NetworkError> = postToken(
        parameters {
            append("grant_type", "refresh_token")
            append("client_id", config.clientId)
            append("client_secret", config.clientSecret)
            append("refresh_token", refreshToken)
        },
    )

    // Uses core:network's shared safeCall/constructRoute (never a module-local
    // copy of exception/status handling) with a statusOverride, because
    // OAuth's /connect/token returns 400 invalid_grant (and 403) for a
    // rejected code/refresh token, which must force re-auth here exactly like
    // a 401 would, unlike on every other endpoint.
    private suspend fun postToken(form: Parameters): Result<TokenResponseDto, DataError.NetworkError> =
        safeCall(
            statusOverride = { status ->
                if (status == 400 || status == 403) DataError.NetworkError.UNAUTHORIZED else null
            },
        ) {
            httpClient.submitForm(url = httpClient.constructRoute("/connect/token"), formParameters = form)
        }
}
