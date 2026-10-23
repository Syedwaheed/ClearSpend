package com.edu.feature.auth.data.remote

import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.core.network.buildUnauthenticatedHttpClient
import com.edu.feature.auth.data.fake.testAuthConfig
import com.edu.feature.auth.data.remote.dto.TokenResponseDto
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.client.request.forms.FormDataContent
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class TrueLayerAuthApiTest {

    private var lastRequest: HttpRequestData? = null

    private fun api(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) =
        TrueLayerAuthApi(
            httpClient = buildUnauthenticatedHttpClient(
                baseUrl = "${testAuthConfig.authBaseUrl}/",
                engine = MockEngine { request ->
                    lastRequest = request
                    handler(request)
                },
            ),
            config = testAuthConfig,
        )

    private val tokenJson = """
        {"access_token":"at-1","expires_in":3600,"token_type":"Bearer",
         "refresh_token":"rt-1","scope":"info accounts"}
    """.trimIndent()

    private fun MockRequestHandleScope.json(body: String, status: HttpStatusCode) = respond(
        content = body,
        status = status,
        headers = headersOf(HttpHeaders.ContentType, "application/json"),
    )

    @Test
    fun `Should parse tokens When exchange succeeds`() = runTest {
        val result = api { json(tokenJson, HttpStatusCode.OK) }.exchangeAuthorizationCode("code-1")

        assertEquals(
            Result.Success(TokenResponseDto("at-1", 3600, "Bearer", "rt-1", "info accounts")),
            result,
        )
    }

    @Test
    fun `Should post authorization_code grant to connect-token`() = runTest {
        api { json(tokenJson, HttpStatusCode.OK) }.exchangeAuthorizationCode("code-1")

        val request = lastRequest!!
        val form = (request.body as FormDataContent).formData
        assertEquals("https://auth.truelayer-sandbox.com/connect/token", request.url.toString())
        assertEquals("authorization_code", form["grant_type"])
        assertEquals("code-1", form["code"])
        assertEquals("clearspend://callback", form["redirect_uri"])
        assertEquals("sandbox-client", form["client_id"])
        assertEquals("test-secret", form["client_secret"])
    }

    @Test
    fun `Should return unauthorized When exchange answers 401`() = runTest {
        val result = api { json("""{"error":"invalid_client"}""", HttpStatusCode.Unauthorized) }
            .exchangeAuthorizationCode("code-1")

        assertEquals(Result.Error(DataError.NetworkError.UNAUTHORIZED), result)
    }

    @Test
    fun `Should return unauthorized When code is rejected with 400 invalid_grant`() = runTest {
        val result = api { json("""{"error":"invalid_grant"}""", HttpStatusCode.BadRequest) }
            .exchangeAuthorizationCode("code-1")

        assertEquals(Result.Error(DataError.NetworkError.UNAUTHORIZED), result)
    }

    @Test
    fun `Should return no internet When the network fails`() = runTest {
        val result = api { throw IOException("offline") }.exchangeAuthorizationCode("code-1")

        assertEquals(Result.Error(DataError.NetworkError.NO_INTERNET), result)
    }

    @Test
    fun `Should return server error When TrueLayer answers 503`() = runTest {
        val result = api { json("{}", HttpStatusCode.ServiceUnavailable) }.exchangeAuthorizationCode("c")

        assertEquals(Result.Error(DataError.NetworkError.SERVER_ERROR), result)
    }

    @Test
    fun `Should post refresh_token grant When refreshing`() = runTest {
        api { json(tokenJson, HttpStatusCode.OK) }.refreshAccessToken("rt-0")

        val form = (lastRequest!!.body as FormDataContent).formData
        assertEquals("refresh_token", form["grant_type"])
        assertEquals("rt-0", form["refresh_token"])
    }
}
