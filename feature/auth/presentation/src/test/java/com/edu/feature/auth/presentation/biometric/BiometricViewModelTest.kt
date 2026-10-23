package com.edu.feature.auth.presentation.biometric

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.edu.feature.auth.domain.model.LoginProfile
import com.edu.feature.auth.domain.repository.AuthRepository
import com.edu.feature.auth.domain.usecase.GetLoginProfileUseCase
import com.edu.feature.auth.domain.usecase.RecordSuccessfulLoginUseCase
import com.edu.feature.auth.presentation.fake.FakeWelcomeUiMapper
import com.edu.feature.auth.presentation.model.BiometricAvailabilityUi
import com.edu.feature.auth.presentation.model.UnlockFailureUi
import com.edu.feature.auth.presentation.model.UnlockMethodUi
import com.edu.feature.auth.presentation.model.WelcomeUi
import com.edu.feature.auth.presentation.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BiometricViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getLoginProfile: GetLoginProfileUseCase = mockk()
    private val recordSuccessfulLogin: RecordSuccessfulLoginUseCase = mockk()
    private val authRepository: AuthRepository = mockk()
    private var availability = BiometricAvailabilityUi.BIOMETRIC_READY

    @Before
    fun setUp() {
        coEvery { getLoginProfile() } returns LoginProfile("Jane", null)
        coEvery { recordSuccessfulLogin() } just runs
        coEvery { authRepository.signOut() } just runs
    }

    private fun createViewModel(
        initialState: BiometricUiState = BiometricUiState(),
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
    ) = BiometricViewModel(
        savedStateHandle = savedStateHandle,
        initialState = initialState,
        getLoginProfile = getLoginProfile,
        recordSuccessfulLogin = recordSuccessfulLogin,
        authRepository = authRepository,
        availabilityChecker = { availability },
        welcomeMapper = FakeWelcomeUiMapper(),
    )

    @Test
    fun `Should greet the user When loaded`() = runTest {
        val viewModel = createViewModel()

        advanceUntilIdle()

        assertEquals(WelcomeUi(firstName = "Jane"), viewModel.uiState.value.welcome)
    }

    @Test
    fun `Should open the biometric prompt automatically When biometrics ready`() = runTest {
        val viewModel = createViewModel()

        viewModel.effect.test {
            assertEquals(BiometricEffect.ShowPrompt(UnlockMethodUi.BIOMETRIC), awaitItem())
        }
    }

    @Test
    fun `Should report no lock screen When device has no credentials`() = runTest {
        availability = BiometricAvailabilityUi.NONE
        val viewModel = createViewModel()

        advanceUntilIdle()

        assertEquals(BiometricAvailabilityUi.NONE, viewModel.uiState.value.availability)
    }

    @Test
    fun `Should offer the device PIN When use PIN clicked`() = runTest {
        val viewModel = createViewModel(BiometricUiState(availability = BiometricAvailabilityUi.BIOMETRIC_READY))

        viewModel.effect.test {
            awaitItem() // auto prompt on load
            viewModel.onAction(BiometricAction.UsePinClicked)
            assertEquals(BiometricEffect.ShowPrompt(UnlockMethodUi.DEVICE_CREDENTIAL), awaitItem())
        }
    }

    @Test
    fun `Should fall back to PIN When unlock clicked without biometrics`() = runTest {
        availability = BiometricAvailabilityUi.DEVICE_CREDENTIAL_ONLY
        val viewModel = createViewModel()

        viewModel.effect.test {
            advanceUntilIdle()
            viewModel.onAction(BiometricAction.UnlockClicked)
            assertEquals(BiometricEffect.ShowPrompt(UnlockMethodUi.DEVICE_CREDENTIAL), awaitItem())
        }
    }

    @Test
    fun `Should record last login When unlocked`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(BiometricAction.AuthenticationSucceeded)
        advanceUntilIdle()

        coVerify { recordSuccessfulLogin() }
    }

    @Test
    fun `Should navigate to dashboard When unlocked`() = runTest {
        availability = BiometricAvailabilityUi.NONE
        val viewModel = createViewModel()

        viewModel.effect.test {
            viewModel.onAction(BiometricAction.AuthenticationSucceeded)
            assertEquals(BiometricEffect.NavigateToDashboard, awaitItem())
        }
    }

    @Test
    fun `Should show lockout message When locked out`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(BiometricAction.AuthenticationFailed(UnlockFailureUi.LOCKED_OUT))
        advanceUntilIdle()

        assertEquals(UnlockFailureUi.LOCKED_OUT, viewModel.uiState.value.failure)
    }

    @Test
    fun `Should not show an error When the prompt is cancelled`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(BiometricAction.AuthenticationFailed(UnlockFailureUi.CANCELLED))
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.failure)
    }

    @Test
    fun `Should sign out and go to connect bank When signing in differently`() = runTest {
        availability = BiometricAvailabilityUi.NONE
        val viewModel = createViewModel()

        viewModel.effect.test {
            viewModel.onAction(BiometricAction.SignInDifferentlyClicked)
            assertEquals(BiometricEffect.NavigateToConnectBank, awaitItem())
        }
        coVerify { authRepository.signOut() }
    }

    @Test
    fun `Should not reload When state was restored`() = runTest {
        createViewModel(savedStateHandle = SavedStateHandle(mapOf("ui_state" to BiometricUiState())))

        advanceUntilIdle()

        coVerify(exactly = 0) { getLoginProfile() }
    }
}
