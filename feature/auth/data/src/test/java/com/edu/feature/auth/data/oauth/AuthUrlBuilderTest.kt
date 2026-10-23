package com.edu.feature.auth.data.oauth

import com.edu.feature.auth.data.fake.testAuthConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.URI
import java.net.URLDecoder

class AuthUrlBuilderTest {

    private val builder = AuthUrlBuilder(testAuthConfig)
    private val url = builder.build(listOf("uk-cs-mock", "uk-ob-all", "uk-oauth-all"), state = "abc_123-XYZ")

    private fun queryParam(name: String): String? = URI(url).rawQuery.split("&")
        .map { it.split("=", limit = 2) }
        .firstOrNull { it[0] == name }
        ?.get(1)

    @Test
    fun `Should target the auth host root`() {
        assertTrue(url.startsWith("https://auth.truelayer-sandbox.com/?"))
    }

    @Test
    fun `Should encode scopes space separated as percent-20`() {
        assertEquals(
            "info%20accounts%20balance%20cards%20transactions%20direct_debits%20standing_orders%20offline_access",
            queryParam("scope"),
        )
    }

    @Test
    fun `Should include info and cards scopes`() {
        val scopes = URLDecoder.decode(queryParam("scope"), "UTF-8").split(" ")
        assertTrue(scopes.containsAll(listOf("info", "cards", "offline_access")))
    }

    @Test
    fun `Should include the state`() {
        assertEquals("abc_123-XYZ", queryParam("state"))
    }

    @Test
    fun `Should encode providers space separated`() {
        assertEquals("uk-cs-mock%20uk-ob-all%20uk-oauth-all", queryParam("providers"))
    }

    @Test
    fun `Should encode the redirect uri`() {
        assertEquals("clearspend%3A%2F%2Fcallback", queryParam("redirect_uri"))
    }

    @Test
    fun `Should request an authorization code for the client`() {
        assertEquals("code", queryParam("response_type"))
        assertEquals("sandbox-client", queryParam("client_id"))
    }

    @Test
    fun `Should never put the client secret in the url`() {
        assertFalse(url.contains("test-secret"))
    }
}
