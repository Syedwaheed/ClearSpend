package com.edu.core.network

import com.edu.core.domain.network.TokenProvider
import com.edu.core.network.plugins.AuthPlugin
import com.edu.core.network.plugins.TokenRefreshPlugin
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import timber.log.Timber

fun buildHttpClient(
    baseUrl: String,
    tokenProvider: TokenProvider,
): HttpClient {
    val client = HttpClient(CIO) {
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
