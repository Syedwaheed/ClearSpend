package com.edu.feature.dashboard.data.fake

import com.edu.core.domain.network.TokenProvider
import com.edu.core.domain.storage.SecureDataStorage
import com.edu.core.domain.storage.SyncStatusStorage
import com.edu.core.network.buildHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import java.time.Instant

const val TEST_BASE_URL = "https://api.truelayer-sandbox.com/"

private object FakeTokenProvider : TokenProvider {
    override suspend fun getAccessToken(): String = "token"
    override suspend fun refreshAccessToken(): Boolean = false
}

/** The real core:network client (shared safeCall + error mapping) over a MockEngine. */
fun mockDataClient(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): HttpClient =
    buildHttpClient(baseUrl = TEST_BASE_URL, tokenProvider = FakeTokenProvider, engine = MockEngine(handler))

fun MockRequestHandleScope.json(body: String, status: HttpStatusCode = HttpStatusCode.OK) = respond(
    content = body,
    status = status,
    headers = headersOf(HttpHeaders.ContentType, "application/json"),
)

class FakeSyncStatusStorage : SyncStatusStorage {
    var syncCount = 0
    var servedFromCache = false
    override suspend fun recordSuccessfulSync() { syncCount++; servedFromCache = false }
    override suspend fun recordServedFromCache() { servedFromCache = true }
    override suspend fun getLastSyncedAt(): Instant? = null
    override suspend fun getOfflineSince(): Instant? = null
}

class FakeSecureDataStorage : SecureDataStorage {
    private val values = mutableMapOf<String, Any>()
    override suspend fun putString(key: String, value: String) { values[key] = value }
    override suspend fun getString(key: String): String? = values[key] as? String
    override suspend fun putInt(key: String, value: Int) { values[key] = value }
    override suspend fun getInt(key: String): Int? = values[key] as? Int
    override suspend fun remove(key: String) { values.remove(key) }
    override suspend fun clear() = values.clear()
    override fun contains(key: String): Boolean = key in values
}
