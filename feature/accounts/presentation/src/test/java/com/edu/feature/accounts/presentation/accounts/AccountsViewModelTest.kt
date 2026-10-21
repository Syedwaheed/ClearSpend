package com.edu.feature.accounts.presentation.accounts

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.feature.accounts.domain.usecase.GetAccountsOverviewUseCase
import com.edu.feature.accounts.presentation.fake.FakeAccountsContentUiMapper
import com.edu.feature.accounts.presentation.fake.fakeNetPositionUi
import com.edu.feature.accounts.presentation.fake.group
import com.edu.feature.accounts.presentation.fake.item
import com.edu.feature.accounts.presentation.fake.overview
import com.edu.feature.accounts.presentation.model.AccountFilterUi
import com.edu.feature.accounts.presentation.model.AccountsContentUi
import com.edu.feature.accounts.presentation.util.MainDispatcherRule
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

class AccountsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getAccountsOverview: GetAccountsOverviewUseCase = mockk()
    private val loadedContent = AccountsContentUi(netPosition = fakeNetPositionUi())

    @Before
    fun setUp() {
        coEvery { getAccountsOverview() } returns Result.Success(overview(groups = listOf(group(item("a")))))
    }

    private fun createViewModel(
        initialState: AccountsUiState = AccountsUiState(),
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
    ) = AccountsViewModel(savedStateHandle, initialState, getAccountsOverview, FakeAccountsContentUiMapper())

    @Test
    fun `Should show content When the overview loads`() = runTest {
        val viewModel = createViewModel()

        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.content?.netPosition?.accountCount)
    }

    @Test
    fun `Should show the error state When loading fails`() = runTest {
        coEvery { getAccountsOverview() } returns Result.Error(DataError.NetworkError.SERVER_ERROR)
        val viewModel = createViewModel()

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasError)
    }

    @Test
    fun `Should show Syncing While refreshing`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiState.test {
            awaitItem()
            viewModel.onAction(AccountsAction.RefreshClicked)
            assertTrue(awaitItem().isRefreshing)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Should stop syncing When the refresh finishes`() = runTest {
        val viewModel = createViewModel(initialState = AccountsUiState(content = loadedContent))

        viewModel.onAction(AccountsAction.RefreshClicked)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isRefreshing)
    }

    @Test
    fun `Should keep content When a refresh fails offline`() = runTest {
        coEvery { getAccountsOverview() } returns Result.Error(DataError.NetworkError.NO_INTERNET)
        val viewModel = createViewModel(initialState = AccountsUiState(content = loadedContent))

        viewModel.onAction(AccountsAction.RefreshClicked)
        advanceUntilIdle()

        assertEquals(loadedContent, viewModel.uiState.value.content)
    }

    @Test
    fun `Should select a filter chip`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(AccountsAction.FilterSelected(AccountFilterUi.SAVINGS))
        advanceUntilIdle()

        assertEquals(AccountFilterUi.SAVINGS, viewModel.uiState.value.selectedFilter)
    }

    @Test
    fun `Should route to Connect Bank When unauthorized`() = runTest {
        coEvery { getAccountsOverview() } returns Result.Error(DataError.NetworkError.UNAUTHORIZED)
        val viewModel = createViewModel()

        viewModel.effect.test {
            assertEquals(AccountsEffect.NavigateToConnectBank, awaitItem())
        }
    }

    @Test
    fun `Should re-run auth When Renew is tapped`() = runTest {
        val viewModel = createViewModel()

        viewModel.effect.test {
            viewModel.onAction(AccountsAction.RenewConsentClicked)
            assertEquals(AccountsEffect.NavigateToBankSelection, awaitItem())
        }
    }

    @Test
    fun `Should open an account When its row is tapped`() = runTest {
        val viewModel = createViewModel()

        viewModel.effect.test {
            viewModel.onAction(AccountsAction.AccountSelected("a"))
            assertEquals(AccountsEffect.NavigateToAccount("a"), awaitItem())
        }
    }

    @Test
    fun `Should skip the initial load When state is restored`() = runTest {
        createViewModel(savedStateHandle = SavedStateHandle(mapOf("ui_state" to AccountsUiState(content = loadedContent))))

        advanceUntilIdle()

        coVerify(exactly = 0) { getAccountsOverview() }
    }
}
