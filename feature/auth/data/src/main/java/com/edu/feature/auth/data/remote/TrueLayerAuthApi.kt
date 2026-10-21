package com.edu.feature.auth.data.remote

import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.feature.auth.data.TrueLayerAuthConfig
import com.edu.feature.auth.data.remote.dto.TokenResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Parameters
import io.ktor.http.parameters
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import java.io.IOException

/** Sandbox-only [AuthApi]: talks to TrueLayer directly with the on-device secret. */
class TrueLayerAuthApi(
    private val httpClient: HttpClient,
    private val config: TrueLayerAuthConfig,
) : AuthApi {

    private val tokenUrl = config.authBaseUrl.trimEnd('/') + "/connect/token"

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

    private suspend fun postToken(form: Parameters): Result<TokenResponseDto, DataError.NetworkError> =
        try {
            val response = httpClient.submitForm(url = tokenUrl, formParameters = form)
            toResult(response)
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpRequestTimeoutException) {
            Result.Error(DataError.NetworkError.REQUEST_TIMEOUT)
        } catch (e: SerializationException) {
            Result.Error(DataError.NetworkError.SERIALIZATION)
        } catch (e: IOException) {
            // Includes UnresolvedAddressException's IO cousins, ConnectException, etc.
            Result.Error(DataError.NetworkError.NO_INTERNET)
        } catch (e: java.nio.channels.UnresolvedAddressException) {
            Result.Error(DataError.NetworkError.NO_INTERNET)
        } catch (e: Exception) {
            Result.Error(DataError.NetworkError.UNKNOWN)
        }

    private suspend fun toResult(response: HttpResponse): Result<TokenResponseDto, DataError.NetworkError> =
        when (response.status.value) {
            in 200..299 -> Result.Success(response.body<TokenResponseDto>())
            400, 401, 403 -> Result.Error(DataError.NetworkError.UNAUTHORIZED)
            408 -> Result.Error(DataError.NetworkError.REQUEST_TIMEOUT)
            429 -> Result.Error(DataError.NetworkError.TOO_MANY_REQUESTS)
            in 500..599 -> Result.Error(DataError.NetworkError.SERVER_ERROR)
            else -> Result.Error(DataError.NetworkError.UNKNOWN)
        }
}
