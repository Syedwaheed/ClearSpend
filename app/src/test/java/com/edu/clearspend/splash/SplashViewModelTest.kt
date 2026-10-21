package com.edu.clearspend.splash

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.edu.feature.auth.domain.model.StartDestination
import com.edu.feature.auth.domain.usecase.DetermineStartDestinationUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {

    private val determineStartDestination: DetermineStartDestinationUseCase = mockk()

    @Before
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun createViewModel() = SplashViewModel(
        savedStateHandle = SavedStateHandle(),
        initialState = SplashUiState(),
        determineStartDestination = determineStartDestination,
        minimumDisplayMillis = 0,
    )

    private fun assertRoutes(destination: StartDestination, expected: SplashEffect) = runTest {
        coEvery { determineStartDestination() } returns destination
        val viewModel = createViewModel()

        viewModel.effect.test { assertEquals(expected, awaitItem()) }
    }

    @Test
    fun `Should open onboarding on first launch`() =
        assertRoutes(StartDestination.ONBOARDING, SplashEffect.NavigateToOnboarding)

    @Test
    fun `Should open connect bank When there is no session`() =
        assertRoutes(StartDestination.CONNECT_BANK, SplashEffect.NavigateToConnectBank)

    @Test
    fun `Should open biometric login When the session is locked`() =
        assertRoutes(StartDestination.BIOMETRIC, SplashEffect.NavigateToBiometric)

    @Test
    fun `Should open the dashboard When the session is valid`() =
        assertRoutes(StartDestination.DASHBOARD, SplashEffect.NavigateToDashboard)

    @Test
    fun `Should stop resolving After routing`() = runTest {
        coEvery { determineStartDestination() } returns StartDestination.DASHBOARD
        val viewModel = createViewModel()

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isResolving)
    }
}
