package com.edu.core.network.ktorclient

import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.core.network.BaseUrlKey
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

@Serializable
private data class PingDto(val value: String)

private fun clientRespondingWith(status: HttpStatusCode, body: String = "{}"): HttpClient {
    val mockEngine = MockEngine { _ ->
        respond(
            content = body,
            status = status,
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
        )
    }
    return HttpClient(mockEngine) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }.apply {
        attributes.put(BaseUrlKey, "https://example.com/")
    }
}

class HttpClientExtTest {

    @Test
    fun `get returns Success When response is 200`() = runTest {
        val client = clientRespondingWith(HttpStatusCode.OK, """{"value":"ok"}""")

        val result = client.get<PingDto>("/ping")

        assertEquals(Result.Success(PingDto("ok")), result)
    }

    @Test
    fun `get maps 401 to UNAUTHORIZED`() = runTest {
        val client = clientRespondingWith(HttpStatusCode.Unauthorized)

        val result = client.get<PingDto>("/ping")

        assertEquals(Result.Error(DataError.NetworkError.UNAUTHORIZED), result)
    }

    @Test
    fun `get maps 404 to NOT_FOUND`() = runTest {
        val client = clientRespondingWith(HttpStatusCode.NotFound)

        val result = client.get<PingDto>("/ping")

        assertEquals(Result.Error(DataError.NetworkError.NOT_FOUND), result)
    }

    @Test
    fun `get maps 408 to REQUEST_TIMEOUT`() = runTest {
        val client = clientRespondingWith(HttpStatusCode.RequestTimeout)

        val result = client.get<PingDto>("/ping")

        assertEquals(Result.Error(DataError.NetworkError.REQUEST_TIMEOUT), result)
    }

    @Test
    fun `get maps 409 to CONFLICT`() = runTest {
        val client = clientRespondingWith(HttpStatusCode.Conflict)

        val result = client.get<PingDto>("/ping")

        assertEquals(Result.Error(DataError.NetworkError.CONFLICT), result)
    }

    @Test
    fun `get maps 500 to SERVER_ERROR`() = runTest {
        val client = clientRespondingWith(HttpStatusCode.InternalServerError)

        val result = client.get<PingDto>("/ping")

        assertEquals(Result.Error(DataError.NetworkError.SERVER_ERROR), result)
    }

    @Test
    fun `get maps malformed body on 200 to SERIALIZATION error`() = runTest {
        val client = clientRespondingWith(HttpStatusCode.OK, "not-json")

        val result = client.get<PingDto>("/ping")

        assertEquals(Result.Error(DataError.NetworkError.SERIALIZATION), result)
    }

    @Test
    fun `get maps 429 to TOO_MANY_REQUESTS`() = runTest {
        val client = clientRespondingWith(HttpStatusCode.TooManyRequests)

        val result = client.get<PingDto>("/ping")

        assertEquals(Result.Error(DataError.NetworkError.TOO_MANY_REQUESTS), result)
    }

    @Test
    fun `get maps 413 to PAYLOAD_TOO_LARGE`() = runTest {
        val client = clientRespondingWith(HttpStatusCode.PayloadTooLarge)

        val result = client.get<PingDto>("/ping")

        assertEquals(Result.Error(DataError.NetworkError.PAYLOAD_TOO_LARGE), result)
    }

    @Test
    fun `safeCall statusOverride takes precedence over the default status mapping`() = runTest {
        val client = clientRespondingWith(HttpStatusCode.BadRequest)

        val result = safeCall<PingDto>(
            statusOverride = { status -> if (status == 400) DataError.NetworkError.UNAUTHORIZED else null },
        ) {
            client.get { url(client.constructRoute("/ping")) }
        }

        assertEquals(Result.Error(DataError.NetworkError.UNAUTHORIZED), result)
    }

    @Test
    fun `safeCall statusOverride returning null falls back to default mapping`() = runTest {
        val client = clientRespondingWith(HttpStatusCode.NotFound)

        val result = safeCall<PingDto>(statusOverride = { null }) {
            client.get { url(client.constructRoute("/ping")) }
        }

        assertEquals(Result.Error(DataError.NetworkError.NOT_FOUND), result)
    }
}
