package com.edu.feature.auth.data.repository

import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.core.network.model.TrueLayerEnvelope
import com.edu.feature.auth.data.fake.FakeSecureDataStorage
import com.edu.feature.auth.data.fake.FakeUserPreferencesStorage
import com.edu.feature.auth.data.fake.testAuthConfig
import com.edu.feature.auth.data.oauth.AuthUrlBuilder
import com.edu.feature.auth.data.remote.AuthApi
import com.edu.feature.auth.data.remote.UserInfoApi
import com.edu.feature.auth.data.remote.dto.TokenResponseDto
import com.edu.feature.auth.data.remote.dto.UserInfoDto
import com.edu.feature.auth.data.token.SessionTokenManager
import com.edu.feature.auth.data.token.StoredTokens
import com.edu.feature.auth.data.token.TokenMapper
import com.edu.feature.auth.data.token.TokenStore
import com.edu.feature.auth.domain.model.SessionState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class AuthRepositoryImplTest {

    private val now = Instant.parse("2026-10-21T10:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)
    private val authApi: AuthApi = mockk()
    private val userInfoApi: UserInfoApi = mockk()
    private val secureStorage = FakeSecureDataStorage()
    private val tokenStore = TokenStore(secureStorage)
    private val preferences = FakeUserPreferencesStorage()
    private val tokenMapper = TokenMapper(clock)

    private val repository = AuthRepositoryImpl(
        authApi = authApi,
        userInfoApi = userInfoApi,
        tokenStore = tokenStore,
        tokenManager = SessionTokenManager(tokenStore, authApi, tokenMapper, clock),
        tokenMapper = tokenMapper,
        authUrlBuilder = AuthUrlBuilder(testAuthConfig),
        stateGenerator = { "fixed-state" },
        userPreferencesStorage = preferences,
        clock = clock,
    )

    private val tokenResponse = TokenResponseDto(accessToken = "at", expiresIn = 3600, refreshToken = "rt")

    @Before
    fun setUp() {
        coEvery { userInfoApi.getUserInfo() } returns
            Result.Success(TrueLayerEnvelope(results = listOf(UserInfoDto(fullName = "Jane Doe"))))
    }

    // --- token exchange ---

    @Test
    fun `Should store tokens When exchange succeeds`() = runTest {
        coEvery { authApi.exchangeAuthorizationCode("code") } returns Result.Success(tokenResponse)

        repository.exchangeAuthorizationCode("code")

        assertEquals(StoredTokens("at", "rt", now.plusSeconds(3600)), tokenStore.getTokens())
    }

    @Test
    fun `Should record consent date When exchange succeeds`() = runTest {
        coEvery { authApi.exchangeAuthorizationCode("code") } returns Result.Success(tokenResponse)

        repository.exchangeAuthorizationCode("code")

        assertEquals(LocalDate.parse("2026-10-21").toEpochDay(), preferences.consentDate.value)
    }

    @Test
    fun `Should cache the account holder name When exchange succeeds`() = runTest {
        coEvery { authApi.exchangeAuthorizationCode("code") } returns Result.Success(tokenResponse)

        repository.exchangeAuthorizationCode("code")

        assertEquals("Jane Doe", repository.getAccountHolderName())
    }

    @Test
    fun `Should still succeed When user info fails`() = runTest {
        coEvery { authApi.exchangeAuthorizationCode("code") } returns Result.Success(tokenResponse)
        coEvery { userInfoApi.getUserInfo() } returns Result.Error(DataError.NetworkError.SERVER_ERROR)

        assertEquals(Result.Success(Unit), repository.exchangeAuthorizationCode("code"))
    }

    @Test
    fun `Should return unauthorized When exchange answers 401`() = runTest {
        coEvery { authApi.exchangeAuthorizationCode("code") } returns
            Result.Error(DataError.NetworkError.UNAUTHORIZED)

        assertEquals(
            Result.Error(DataError.NetworkError.UNAUTHORIZED),
            repository.exchangeAuthorizationCode("code"),
        )
    }

    @Test
    fun `Should not store tokens When exchange answers 401`() = runTest {
        coEvery { authApi.exchangeAuthorizationCode("code") } returns
            Result.Error(DataError.NetworkError.UNAUTHORIZED)

        repository.exchangeAuthorizationCode("code")

        assertNull(tokenStore.getTokens())
    }

    @Test
    fun `Should return no internet When the network fails during exchange`() = runTest {
        coEvery { authApi.exchangeAuthorizationCode("code") } returns
            Result.Error(DataError.NetworkError.NO_INTERNET)

        assertEquals(
            Result.Error(DataError.NetworkError.NO_INTERNET),
            repository.exchangeAuthorizationCode("code"),
        )
    }

    @Test
    fun `Should not record consent When the network fails during exchange`() = runTest {
        coEvery { authApi.exchangeAuthorizationCode("code") } returns
            Result.Error(DataError.NetworkError.NO_INTERNET)

        repository.exchangeAuthorizationCode("code")

        assertNull(preferences.consentDate.value)
    }

    // --- auth url + state ---

    @Test
    fun `Should embed the generated state in the auth url`() = runTest {
        val url = (repository.createAuthorizationUrl(listOf("uk-cs-mock")) as Result.Success).data

        assertTrue(url.contains("state=fixed-state"))
    }

    @Test
    fun `Should hand the pending state out only once`() = runTest {
        repository.createAuthorizationUrl(listOf("uk-cs-mock"))

        repository.consumePendingState()

        assertNull(repository.consumePendingState())
    }

    @Test
    fun `Should return the pending state that was generated`() = runTest {
        repository.createAuthorizationUrl(listOf("uk-cs-mock"))

        assertEquals("fixed-state", repository.consumePendingState())
    }

    @Test
    fun `Should report a local error When the state cannot be stored`() = runTest {
        secureStorage.failWrites = true

        assertEquals(
            Result.Error(DataError.LocalError.DISKFULL),
            repository.createAuthorizationUrl(listOf("uk-cs-mock")),
        )
    }

    // --- session state ---

    @Test
    fun `Should report no session When nothing stored`() = runTest {
        assertEquals(SessionState.NO_SESSION, repository.getSessionState())
    }

    @Test
    fun `Should report active When access token valid`() = runTest {
        tokenStore.saveTokens(StoredTokens("at", "rt", now.plusSeconds(600)))

        assertEquals(SessionState.ACTIVE, repository.getSessionState())
    }

    @Test
    fun `Should report expired When access token expired but refresh token present`() = runTest {
        tokenStore.saveTokens(StoredTokens("at", "rt", now.minusSeconds(1)))

        assertEquals(SessionState.ACCESS_TOKEN_EXPIRED, repository.getSessionState())
    }

    @Test
    fun `Should report no session When access token expired and no refresh token`() = runTest {
        tokenStore.saveTokens(StoredTokens("at", null, now.minusSeconds(1)))

        assertEquals(SessionState.NO_SESSION, repository.getSessionState())
    }

    @Test
    fun `Should delete tokens on sign out`() = runTest {
        tokenStore.saveTokens(StoredTokens("at", "rt", now.plusSeconds(600)))

        repository.signOut()

        assertEquals(SessionState.NO_SESSION, repository.getSessionState())
    }
}
