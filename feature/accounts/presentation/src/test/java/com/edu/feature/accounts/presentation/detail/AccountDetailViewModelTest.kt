package com.edu.feature.accounts.presentation.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.feature.accounts.domain.model.AccountDetail
import com.edu.feature.accounts.domain.usecase.GetAccountDetailUseCase
import com.edu.feature.accounts.presentation.fake.FakeAccountDetailUiMapper
import com.edu.feature.accounts.presentation.fake.account
import com.edu.feature.accounts.presentation.fake.fakeDetailUi
import com.edu.feature.accounts.presentation.model.CopiedFieldUi
import com.edu.feature.accounts.presentation.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AccountDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getAccountDetail: GetAccountDetailUseCase = mockk()

    @Before
    fun setUp() {
        coEvery { getAccountDetail("acc-1") } returns Result.Success(AccountDetail(account("acc-1"), null, emptyList(), null))
    }

    private fun createViewModel(
        initialState: AccountDetailUiState = AccountDetailUiState(),
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
    ) = AccountDetailViewModel(savedStateHandle, initialState, "acc-1", getAccountDetail, FakeAccountDetailUiMapper())

    private val loaded = AccountDetailUiState(detail = fakeDetailUi("acc-1"))

    @Test
    fun `Should load the account it was opened for`() = runTest {
        val viewModel = createViewModel()

        advanceUntilIdle()

        assertEquals(fakeDetailUi("acc-1"), viewModel.uiState.value.detail)
    }

    @Test
    fun `Should show the error state When loading fails`() = runTest {
        coEvery { getAccountDetail("acc-1") } returns Result.Error(DataError.NetworkError.NOT_FOUND)
        val viewModel = createViewModel()

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasError)
    }

    @Test
    fun `Should copy the sort code to the clipboard`() = runTest {
        val viewModel = createViewModel(initialState = loaded)

        viewModel.effect.test {
            viewModel.onAction(AccountDetailAction.CopySortCodeClicked)
            assertEquals(AccountDetailEffect.CopyToClipboard("Sort code", "01-21-31"), awaitItem())
        }
    }

    @Test
    fun `Should show Copied on the account number row`() = runTest {
        val viewModel = createViewModel(initialState = loaded)

        viewModel.onAction(AccountDetailAction.CopyAccountNumberClicked)
        advanceUntilIdle()

        assertEquals(CopiedFieldUi.ACCOUNT_NUMBER, viewModel.uiState.value.copiedField)
    }

    @Test
    fun `Should clear Copied When the feedback time elapses`() = runTest {
        val viewModel = createViewModel(initialState = loaded.copy(copiedField = CopiedFieldUi.SORT_CODE))

        viewModel.onAction(AccountDetailAction.CopiedFeedbackElapsed(CopiedFieldUi.SORT_CODE))
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.copiedField)
    }

    @Test
    fun `Should keep a newer Copied When an older timer elapses`() = runTest {
        val viewModel = createViewModel(initialState = loaded.copy(copiedField = CopiedFieldUi.ACCOUNT_NUMBER))

        viewModel.onAction(AccountDetailAction.CopiedFeedbackElapsed(CopiedFieldUi.SORT_CODE))
        advanceUntilIdle()

        assertEquals(CopiedFieldUi.ACCOUNT_NUMBER, viewModel.uiState.value.copiedField)
    }

    @Test
    fun `Should navigate back`() = runTest {
        val viewModel = createViewModel(initialState = loaded)

        viewModel.effect.test {
            viewModel.onAction(AccountDetailAction.BackClicked)
            assertEquals(AccountDetailEffect.NavigateBack, awaitItem())
        }
    }

    @Test
    fun `Should route to Connect Bank When unauthorized`() = runTest {
        coEvery { getAccountDetail("acc-1") } returns Result.Error(DataError.NetworkError.UNAUTHORIZED)
        val viewModel = createViewModel()

        viewModel.effect.test {
            assertEquals(AccountDetailEffect.NavigateToConnectBank, awaitItem())
        }
    }

    @Test
    fun `Should skip the initial load When state is restored`() = runTest {
        createViewModel(savedStateHandle = SavedStateHandle(mapOf("ui_state" to loaded)))

        advanceUntilIdle()

        coVerify(exactly = 0) { getAccountDetail(any()) }
    }
}
