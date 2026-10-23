package com.edu.feature.auth.presentation.connecting

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.feature.auth.domain.model.AuthError
import com.edu.feature.auth.domain.model.AuthRedirect
import com.edu.feature.auth.domain.usecase.ConnectBankUseCase
import com.edu.feature.auth.domain.usecase.ValidateAuthRedirectUseCase
import com.edu.feature.auth.presentation.fake.FakeConnectionErrorUiMapper
import com.edu.feature.auth.presentation.model.ConnectionErrorUi
import com.edu.feature.auth.presentation.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ConnectingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val validate: ValidateAuthRedirectUseCase = mockk()
    private val connectBank: ConnectBankUseCase = mockk()
    private val redirect = ConnectingAction.RedirectReceived(code = "code", state = "state", error = null)

    private fun createViewModel(
        initialState: ConnectingUiState = ConnectingUiState(),
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
    ) = ConnectingViewModel(savedStateHandle, initialState, validate, connectBank, FakeConnectionErrorUiMapper())

    private fun givenValidRedirect() {
        coEvery { validate(AuthRedirect("code", "state", null)) } returns Result.Success("code")
    }

    @Test
    fun `Should navigate to dashboard When exchange succeeds`() = runTest {
        givenValidRedirect()
        coEvery { connectBank("code") } returns Result.Success(Unit)
        val viewModel = createViewModel()

        viewModel.effect.test {
            viewModel.onAction(redirect)
            assertEquals(ConnectingEffect.NavigateToDashboard, awaitItem())
        }
    }

    @Test
    fun `Should forget the code When connected`() = runTest {
        givenValidRedirect()
        coEvery { connectBank("code") } returns Result.Success(Unit)
        val viewModel = createViewModel()

        viewModel.onAction(redirect)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.code)
    }

    @Test
    fun `Should fail without exchanging When state mismatches`() = runTest {
        coEvery { validate(any()) } returns Result.Error(AuthError.STATE_MISMATCH)
        val viewModel = createViewModel()

        viewModel.onAction(redirect)
        advanceUntilIdle()

        coVerify(exactly = 0) { connectBank(any()) }
    }

    @Test
    fun `Should show security error When state mismatches`() = runTest {
        coEvery { validate(any()) } returns Result.Error(AuthError.STATE_MISMATCH)
        val viewModel = createViewModel()

        viewModel.onAction(redirect)
        advanceUntilIdle()

        assertEquals(ConnectionErrorUi.SECURITY_CHECK_FAILED, viewModel.uiState.value.error)
    }

    @Test
    fun `Should offer retry and keep the code When offline`() = runTest {
        givenValidRedirect()
        coEvery { connectBank("code") } returns Result.Error(DataError.NetworkError.NO_INTERNET)
        val viewModel = createViewModel()

        viewModel.onAction(redirect)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.canRetry)
    }

    @Test
    fun `Should discard the code When exchange is rejected`() = runTest {
        givenValidRedirect()
        coEvery { connectBank("code") } returns Result.Error(DataError.NetworkError.UNAUTHORIZED)
        val viewModel = createViewModel()

        viewModel.onAction(redirect)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.code)
    }

    @Test
    fun `Should exchange the stored code again When retry clicked`() = runTest {
        coEvery { connectBank("code") } returns Result.Success(Unit)
        val viewModel = createViewModel(
            ConnectingUiState(code = "code", status = ConnectingStatus.FAILED, error = ConnectionErrorUi.NO_INTERNET),
        )

        viewModel.onAction(ConnectingAction.RetryClicked)
        advanceUntilIdle()

        assertEquals(ConnectingStatus.CONNECTED, viewModel.uiState.value.status)
    }

    @Test
    fun `Should resume the exchange After process death mid-exchange`() = runTest {
        coEvery { connectBank("code") } returns Result.Success(Unit)
        val restored = ConnectingUiState(code = "code", state = "state", status = ConnectingStatus.EXCHANGING)
        val viewModel = createViewModel(savedStateHandle = SavedStateHandle(mapOf("ui_state" to restored)))

        advanceUntilIdle()

        assertEquals(ConnectingStatus.CONNECTED, viewModel.uiState.value.status)
    }

    @Test
    fun `Should ignore a duplicate redirect While exchanging`() = runTest {
        val viewModel = createViewModel(ConnectingUiState(code = "code", status = ConnectingStatus.EXCHANGING))

        viewModel.onAction(redirect)
        advanceUntilIdle()

        coVerify(exactly = 0) { validate(any()) }
    }

    @Test
    fun `Should go back to connect bank When start again clicked`() = runTest {
        val viewModel = createViewModel()

        viewModel.effect.test {
            viewModel.onAction(ConnectingAction.StartAgainClicked)
            assertEquals(ConnectingEffect.NavigateToConnectBank, awaitItem())
        }
    }
}
