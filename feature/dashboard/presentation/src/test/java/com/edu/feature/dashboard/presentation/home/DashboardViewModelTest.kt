package com.edu.feature.dashboard.presentation.home

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.feature.dashboard.domain.usecase.GetDashboardUseCase
import com.edu.feature.dashboard.presentation.fake.FakeDashboardContentUiMapper
import com.edu.feature.dashboard.presentation.fake.dashboard
import com.edu.feature.dashboard.presentation.fake.fakeContent
import com.edu.feature.dashboard.presentation.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getDashboard: GetDashboardUseCase = mockk()

    @Before
    fun setUp() {
        coEvery { getDashboard() } returns Result.Success(dashboard())
    }

    private fun createViewModel(
        initialState: DashboardUiState = DashboardUiState(),
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
    ) = DashboardViewModel(savedStateHandle, initialState, getDashboard, FakeDashboardContentUiMapper())

    @Test
    fun `Should show content When the dashboard loads`() = runTest {
        val viewModel = createViewModel()

        advanceUntilIdle()

        assertEquals(fakeContent(1), viewModel.uiState.value.content)
    }

    @Test
    fun `Should stop loading When the dashboard loads`() = runTest {
        val viewModel = createViewModel()

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `Should show the skeleton While loading`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertFalse(awaitItem().isLoading)
            assertTrue(awaitItem().isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Should show the error state When loading fails`() = runTest {
        coEvery { getDashboard() } returns Result.Error(DataError.NetworkError.NO_INTERNET)
        val viewModel = createViewModel()

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasError)
    }

    @Test
    fun `Should show the empty state When there are no accounts`() = runTest {
        coEvery { getDashboard() } returns Result.Success(dashboard(accountIds = emptyList()))
        val viewModel = createViewModel()

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isEmpty)
    }

    @Test
    fun `Should route to Connect Bank When consent has expired`() = runTest {
        coEvery { getDashboard() } returns Result.Error(DataError.NetworkError.UNAUTHORIZED)
        val viewModel = createViewModel()

        viewModel.effect.test {
            assertEquals(DashboardEffect.NavigateToConnectBank, awaitItem())
        }
    }

    @Test
    fun `Should keep content When a refresh fails`() = runTest {
        coEvery { getDashboard() } returns Result.Error(DataError.NetworkError.NO_INTERNET)
        val viewModel = createViewModel(initialState = DashboardUiState(content = fakeContent(2)))

        viewModel.onAction(DashboardAction.RefreshData)
        advanceUntilIdle()

        assertEquals(fakeContent(2), viewModel.uiState.value.content)
    }

    @Test
    fun `Should load again When retry is clicked`() = runTest {
        val viewModel = createViewModel(initialState = DashboardUiState(hasError = true))

        viewModel.onAction(DashboardAction.RetryClicked)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hasError)
    }

    @Test
    fun `Should skip the initial load When state is restored`() = runTest {
        val restored = SavedStateHandle(mapOf("ui_state" to DashboardUiState(content = fakeContent(3))))
        createViewModel(savedStateHandle = restored)

        advanceUntilIdle()

        coVerify(exactly = 0) { getDashboard() }
    }

    @Test
    fun `Should open an account When its card is tapped`() = runTest {
        val viewModel = createViewModel()

        viewModel.effect.test {
            viewModel.onAction(DashboardAction.AccountSelected("acc-1"))
            assertEquals(DashboardEffect.NavigateToAccount("acc-1"), awaitItem())
        }
    }

    @Test
    fun `Should open a transaction When its row is tapped`() = runTest {
        val viewModel = createViewModel()

        viewModel.effect.test {
            viewModel.onAction(DashboardAction.TransactionSelected("tx-1"))
            assertEquals(DashboardEffect.NavigateToTransaction("tx-1"), awaitItem())
        }
    }

    @Test
    fun `Should say coming soon When Request is tapped`() = runTest {
        val viewModel = createViewModel()

        viewModel.effect.test {
            viewModel.onAction(DashboardAction.RequestClicked)
            assertEquals(DashboardEffect.ShowComingSoon, awaitItem())
        }
    }

    @Test
    fun `Should open Bills When upcoming payments are tapped`() = runTest {
        val viewModel = createViewModel()

        viewModel.effect.test {
            viewModel.onAction(DashboardAction.UpcomingPaymentsClicked)
            assertEquals(DashboardEffect.NavigateToBills, awaitItem())
        }
    }

    @Test
    fun `Should open the send sheet When Send is tapped`() = runTest {
        val viewModel = createViewModel()

        viewModel.effect.test {
            viewModel.onAction(DashboardAction.SendClicked)
            assertEquals(DashboardEffect.OpenSendMoney, awaitItem())
        }
    }
}
