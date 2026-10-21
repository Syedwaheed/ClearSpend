package com.edu.feature.auth.data.token

import com.edu.feature.auth.data.remote.dto.TokenResponseDto
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class TokenMapperTest {

    private val now = Instant.parse("2026-10-21T10:00:00Z")
    private val mapper = TokenMapper(Clock.fixed(now, ZoneOffset.UTC))

    @Test
    fun `Should convert expires_in to an absolute expiry`() {
        val tokens = mapper.map(TokenResponseDto(accessToken = "a", expiresIn = 3600, refreshToken = "r"))

        assertEquals(StoredTokens("a", "r", Instant.parse("2026-10-21T11:00:00Z")), tokens)
    }

    @Test
    fun `Should keep the previous refresh token When response omits one`() {
        val tokens = mapper.map(TokenResponseDto(accessToken = "a", expiresIn = 60), previousRefreshToken = "old")

        assertEquals("old", tokens.refreshToken)
    }

    @Test
    fun `Should treat a negative expires_in as already expired`() {
        val tokens = mapper.map(TokenResponseDto(accessToken = "a", expiresIn = -5))

        assertEquals(now, tokens.accessTokenExpiresAt)
    }

    @Test
    fun `Should not print secrets in toString`() {
        val text = StoredTokens("secret-access", "secret-refresh", now).toString()

        assertEquals(false, text.contains("secret"))
    }
}
