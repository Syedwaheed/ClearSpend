package com.edu.core.network.plugins

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class TokenRefreshPluginTest {

    @Test
    fun `retries once with refreshed token When first response is 401`() = runTest {
        val callCount = AtomicInteger(0)
        var refreshCalled = false
        var tokenUsedOnSecondCall: String? = null

        val mockEngine = MockEngine { request ->
            val attempt = callCount.incrementAndGet()
            if (attempt == 1) {
                respond(content = "", status = HttpStatusCode.Unauthorized)
            } else {
                tokenUsedOnSecondCall = request.headers["Authorization"]
                respond(content = "", status = HttpStatusCode.OK)
            }
        }

        val client = HttpClient(mockEngine) {
            install(TokenRefreshPlugin) {
                tokenProvider = { "new-token" }
                onRefreshToken = {
                    refreshCalled = true
                    true
                }
            }
        }

        val response = client.get("https://example.com/resource")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(2, callCount.get())
        assertTrue(refreshCalled)
        assertEquals("Bearer new-token", tokenUsedOnSecondCall)
    }

    @Test
    fun `does not retry When refresh fails`() = runTest {
        val callCount = AtomicInteger(0)

        val mockEngine = MockEngine {
            callCount.incrementAndGet()
            respond(content = "", status = HttpStatusCode.Unauthorized)
        }

        val client = HttpClient(mockEngine) {
            install(TokenRefreshPlugin) {
                tokenProvider = { "token" }
                onRefreshToken = { false }
            }
        }

        val response = client.get("https://example.com/resource")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(1, callCount.get())
    }
}
