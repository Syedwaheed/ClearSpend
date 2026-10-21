package com.edu.core.network

import com.edu.core.domain.network.TokenProvider
import com.edu.core.network.plugins.AuthPlugin
import com.edu.core.network.plugins.TokenRefreshPlugin
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import timber.log.Timber

/**
 * Authenticated Data API client. [engine] defaults to CIO; tests pass a
 * Ktor `MockEngine` so repositories can be exercised end-to-end through the
 * shared [com.edu.core.network.ktorclient.safeCall] mapping.
 */
fun buildHttpClient(
    baseUrl: String,
    tokenProvider: TokenProvider,
    engine: HttpClientEngine = CIO.create(),
): HttpClient {
    val client = HttpClient(engine) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                },
            )
        }

        install(Logging) {
            level = LogLevel.INFO
            logger = object : Logger {
                override fun log(message: String) {
                    Timber.tag("ClearSpendNetwork").d(message)
                }
            }
        }

        install(AuthPlugin) {
            this.tokenProvider = { tokenProvider.getAccessToken() }
        }

        install(TokenRefreshPlugin) {
            this.tokenProvider = { tokenProvider.getAccessToken() }
            this.onRefreshToken = { tokenProvider.refreshAccessToken() }
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 15_000
        }
    }

    client.attributes.put(BaseUrlKey, baseUrl)
    return client
}

/**
 * Client for the TrueLayer auth host (`/connect/token`). It carries NO bearer
 * token and NO refresh plugin: a 401/400 from the token endpoint must surface to
 * the caller instead of triggering a refresh loop. Logging is disabled because
 * request and response bodies contain auth codes, secrets and tokens.
 *
 * [baseUrl] is carried the same way [buildHttpClient] carries its base URL (via
 * [BaseUrlKey]), so callers use the same shared [com.edu.core.network.ktorclient.constructRoute] /
 * [com.edu.core.network.ktorclient.safeCall] helpers instead of hand-building URLs.
 * Pass it with a trailing slash (e.g. `"https://auth.truelayer-sandbox.com/"`).
 */
fun buildUnauthenticatedHttpClient(
    baseUrl: String,
    engine: HttpClientEngine = CIO.create(),
): HttpClient {
    val client = HttpClient(engine) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                },
            )
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 15_000
        }
        expectSuccess = false
    }
    client.attributes.put(BaseUrlKey, baseUrl)
    return client
}
