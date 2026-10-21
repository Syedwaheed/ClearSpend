package com.edu.feature.auth.domain.usecase

import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.feature.auth.domain.fake.FakeUserPreferencesStorage
import com.edu.feature.auth.domain.model.SessionState
import com.edu.feature.auth.domain.model.StartDestination
import com.edu.feature.auth.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DetermineStartDestinationUseCaseTest {

    private val authRepository: AuthRepository = mockk()

    private fun useCase(onboardingCompleted: Boolean = false, biometricEnabled: Boolean = false) =
        DetermineStartDestinationUseCase(
            authRepository = authRepository,
            userPreferencesStorage = FakeUserPreferencesStorage(
                onboardingCompleted = onboardingCompleted,
                biometricEnabled = biometricEnabled,
            ),
        )

    @Test
    fun `Should go to onboarding When no session and onboarding not completed`() = runTest {
        coEvery { authRepository.getSessionState() } returns SessionState.NO_SESSION

        assertEquals(StartDestination.ONBOARDING, useCase(onboardingCompleted = false)())
    }

    @Test
    fun `Should go to connect bank When no session and onboarding completed`() = runTest {
        coEvery { authRepository.getSessionState() } returns SessionState.NO_SESSION

        assertEquals(StartDestination.CONNECT_BANK, useCase(onboardingCompleted = true)())
    }

    @Test
    fun `Should go to biometric When session active and biometric enabled`() = runTest {
        coEvery { authRepository.getSessionState() } returns SessionState.ACTIVE

        assertEquals(StartDestination.BIOMETRIC, useCase(biometricEnabled = true)())
    }

    @Test
    fun `Should go to dashboard When session active and biometric disabled`() = runTest {
        coEvery { authRepository.getSessionState() } returns SessionState.ACTIVE

        assertEquals(StartDestination.DASHBOARD, useCase(biometricEnabled = false)())
    }

    @Test
    fun `Should not refresh When session active`() = runTest {
        coEvery { authRepository.getSessionState() } returns SessionState.ACTIVE

        useCase()()

        coVerify(exactly = 0) { authRepository.refreshSession() }
    }

    @Test
    fun `Should go to dashboard When expired token refreshes and biometric disabled`() = runTest {
        coEvery { authRepository.getSessionState() } returns SessionState.ACCESS_TOKEN_EXPIRED
        coEvery { authRepository.refreshSession() } returns Result.Success(Unit)

        assertEquals(StartDestination.DASHBOARD, useCase(biometricEnabled = false)())
    }

    @Test
    fun `Should go to biometric When expired token refreshes and biometric enabled`() = runTest {
        coEvery { authRepository.getSessionState() } returns SessionState.ACCESS_TOKEN_EXPIRED
        coEvery { authRepository.refreshSession() } returns Result.Success(Unit)

        assertEquals(StartDestination.BIOMETRIC, useCase(biometricEnabled = true)())
    }

    @Test
    fun `Should go to connect bank When refresh is rejected as unauthorized`() = runTest {
        coEvery { authRepository.getSessionState() } returns SessionState.ACCESS_TOKEN_EXPIRED
        coEvery { authRepository.refreshSession() } returns
            Result.Error(DataError.NetworkError.UNAUTHORIZED)

        assertEquals(StartDestination.CONNECT_BANK, useCase(biometricEnabled = true)())
    }

    @Test
    fun `Should keep the session When refresh fails because offline`() = runTest {
        coEvery { authRepository.getSessionState() } returns SessionState.ACCESS_TOKEN_EXPIRED
        coEvery { authRepository.refreshSession() } returns
            Result.Error(DataError.NetworkError.NO_INTERNET)

        assertEquals(StartDestination.DASHBOARD, useCase(biometricEnabled = false)())
    }
}
