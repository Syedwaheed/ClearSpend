package com.edu.feature.auth.data.token

import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.feature.auth.data.fake.FakeSecureDataStorage
import com.edu.feature.auth.data.remote.AuthApi
import com.edu.feature.auth.data.remote.dto.TokenResponseDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class TrueLayerTokenProviderTest {

    private val now = Instant.parse("2026-10-21T10:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)
    private val authApi: AuthApi = mockk()
    private val tokenStore = TokenStore(FakeSecureDataStorage())
    private val manager = SessionTokenManager(tokenStore, authApi, TokenMapper(clock), clock)
    private val provider = TrueLayerTokenProvider(manager)

    private val validTokens = StoredTokens("valid", "rt", now.plusSeconds(3_000))
    private val expiredTokens = StoredTokens("expired", "rt", now.minusSeconds(1))

    @Test
    fun `Should return null When no session stored`() = runTest {
        assertNull(provider.getAccessToken())
    }

    @Test
    fun `Should return stored token When still valid`() = runTest {
        tokenStore.saveTokens(validTokens)

        assertEquals("valid", provider.getAccessToken())
    }

    @Test
    fun `Should refresh first When stored token expired`() = runTest {
        tokenStore.saveTokens(expiredTokens)
        coEvery { authApi.refreshAccessToken("rt") } returns
            Result.Success(TokenResponseDto(accessToken = "fresh", expiresIn = 3600, refreshToken = "rt-2"))

        assertEquals("fresh", provider.getAccessToken())
    }

    @Test
    fun `Should rotate the stored refresh token After refreshing`() = runTest {
        tokenStore.saveTokens(expiredTokens)
        coEvery { authApi.refreshAccessToken("rt") } returns
            Result.Success(TokenResponseDto(accessToken = "fresh", expiresIn = 3600, refreshToken = "rt-2"))

        provider.getAccessToken()

        assertEquals("rt-2", tokenStore.getTokens()?.refreshToken)
    }

    @Test
    fun `Should report failure When forced refresh is rejected`() = runTest {
        tokenStore.saveTokens(validTokens)
        coEvery { authApi.refreshAccessToken("rt") } returns Result.Error(DataError.NetworkError.UNAUTHORIZED)

        assertFalse(provider.refreshAccessToken())
    }

    @Test
    fun `Should report success When forced refresh succeeds`() = runTest {
        tokenStore.saveTokens(validTokens)
        coEvery { authApi.refreshAccessToken("rt") } returns
            Result.Success(TokenResponseDto(accessToken = "fresh", expiresIn = 3600))

        assertTrue(provider.refreshAccessToken())
    }

    @Test
    fun `Should not call TrueLayer When there is no refresh token`() = runTest {
        tokenStore.saveTokens(StoredTokens("expired", null, now.minusSeconds(1)))

        provider.refreshAccessToken()

        coVerify(exactly = 0) { authApi.refreshAccessToken(any()) }
    }
}
