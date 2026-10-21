package com.edu.feature.auth.domain.usecase

import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.feature.auth.domain.fake.FakeUserPreferencesStorage
import com.edu.feature.auth.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class ConnectBankUseCaseTest {

    private val now = Instant.parse("2026-10-21T07:42:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)
    private val authRepository: AuthRepository = mockk()
    private val preferences = FakeUserPreferencesStorage()
    private val useCase = ConnectBankUseCase(
        authRepository = authRepository,
        recordSuccessfulLogin = RecordSuccessfulLoginUseCase(preferences, clock),
    )

    @Test
    fun `Should record last login When exchange succeeds`() = runTest {
        coEvery { authRepository.exchangeAuthorizationCode("code") } returns Result.Success(Unit)

        useCase("code")

        assertEquals(now.toEpochMilli(), preferences.lastLogin.value)
    }

    @Test
    fun `Should not record last login When exchange fails`() = runTest {
        coEvery { authRepository.exchangeAuthorizationCode("code") } returns
            Result.Error(DataError.NetworkError.UNAUTHORIZED)

        useCase("code")

        assertNull(preferences.lastLogin.value)
    }

    @Test
    fun `Should pass the exchange error through`() = runTest {
        coEvery { authRepository.exchangeAuthorizationCode("code") } returns
            Result.Error(DataError.NetworkError.NO_INTERNET)

        assertEquals(Result.Error(DataError.NetworkError.NO_INTERNET), useCase("code"))
    }
}
