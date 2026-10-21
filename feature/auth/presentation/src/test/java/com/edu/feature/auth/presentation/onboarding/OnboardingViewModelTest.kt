package com.edu.feature.auth.presentation.onboarding

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.edu.feature.auth.presentation.fake.FakeUserPreferencesStorage
import com.edu.feature.auth.presentation.util.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class OnboardingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val preferences = FakeUserPreferencesStorage()

    private fun createViewModel(initialState: OnboardingUiState = OnboardingUiState()) =
        OnboardingViewModel(SavedStateHandle(), initialState, preferences)

    @Test
    fun `Should advance one page When next clicked`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(OnboardingAction.NextClicked)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.currentPage)
    }

    @Test
    fun `Should stay on last page When next clicked on last page`() = runTest {
        val viewModel = createViewModel(OnboardingUiState(currentPage = 2))

        viewModel.onAction(OnboardingAction.NextClicked)
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.currentPage)
    }

    @Test
    fun `Should follow the pager When a page settles`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(OnboardingAction.PageSettled(2))
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.currentPage)
    }

    @Test
    fun `Should clamp an out of range page`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(OnboardingAction.PageSettled(9))
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.currentPage)
    }

    @Test
    fun `Should navigate to connect bank When get started clicked`() = runTest {
        val viewModel = createViewModel(OnboardingUiState(currentPage = 2))

        viewModel.effect.test {
            viewModel.onAction(OnboardingAction.GetStartedClicked)
            assertEquals(OnboardingEffect.NavigateToConnectBank, awaitItem())
        }
    }

    @Test
    fun `Should remember onboarding is done When get started clicked`() = runTest {
        val viewModel = createViewModel(OnboardingUiState(currentPage = 2))

        viewModel.onAction(OnboardingAction.GetStartedClicked)
        advanceUntilIdle()

        assertTrue(preferences.onboarding.value)
    }

    @Test
    fun `Should navigate to sign in When sign in clicked`() = runTest {
        val viewModel = createViewModel(OnboardingUiState(currentPage = 2))

        viewModel.effect.test {
            viewModel.onAction(OnboardingAction.SignInClicked)
            assertEquals(OnboardingEffect.NavigateToSignIn, awaitItem())
        }
    }
}
