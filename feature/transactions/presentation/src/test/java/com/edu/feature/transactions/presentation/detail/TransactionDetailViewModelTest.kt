package com.edu.feature.transactions.presentation.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.edu.core.domain.model.TransactionCategory
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.feature.transactions.domain.model.MerchantInsight
import com.edu.feature.transactions.domain.model.TransactionDetail
import com.edu.feature.transactions.domain.usecase.GetTransactionDetailUseCase
import com.edu.feature.transactions.presentation.fake.FakeTransactionDetailUiMapper
import com.edu.feature.transactions.presentation.fake.fakeDetailUi
import com.edu.feature.transactions.presentation.fake.tx
import com.edu.feature.transactions.presentation.util.MainDispatcherRule
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

class TransactionDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getTransactionDetail: GetTransactionDetailUseCase = mockk()

    private fun detail(category: TransactionCategory = TransactionCategory.PURCHASE) = Result.Success(
        TransactionDetail(tx("tx-1", category = category), account = null, insight = MerchantInsight.FirstTime("Tesco", 90), offlineSince = null),
    )

    @Before
    fun setUp() {
        coEvery { getTransactionDetail("tx-1") } returns detail()
    }

    private fun createViewModel(
        initialState: TransactionDetailUiState = TransactionDetailUiState(),
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
    ) = TransactionDetailViewModel(savedStateHandle, initialState, "tx-1", getTransactionDetail, FakeTransactionDetailUiMapper())

    private val loaded = TransactionDetailUiState(detail = fakeDetailUi("tx-1"))

    @Test
    fun `Should load the transaction it was opened for`() = runTest {
        val viewModel = createViewModel()

        advanceUntilIdle()

        assertEquals(fakeDetailUi("tx-1"), viewModel.uiState.value.detail)
    }

    @Test
    fun `Should show the error state When the transaction cannot be loaded`() = runTest {
        coEvery { getTransactionDetail("tx-1") } returns Result.Error(DataError.NetworkError.NOT_FOUND)
        val viewModel = createViewModel()

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasError)
    }

    @Test
    fun `Should load again When Try again is tapped`() = runTest {
        coEvery { getTransactionDetail("tx-1") } returns Result.Error(DataError.NetworkError.NO_INTERNET)
        val viewModel = createViewModel()
        advanceUntilIdle()
        coEvery { getTransactionDetail("tx-1") } returns detail()

        viewModel.onAction(TransactionDetailAction.RetryClicked)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hasError)
    }

    @Test
    fun `Should route to Connect Bank When unauthorized`() = runTest {
        coEvery { getTransactionDetail("tx-1") } returns Result.Error(DataError.NetworkError.UNAUTHORIZED)
        val viewModel = createViewModel()

        viewModel.effect.test {
            assertEquals(TransactionDetailEffect.NavigateToConnectBank, awaitItem())
        }
    }

    @Test
    fun `Should copy the transaction ID to the clipboard`() = runTest {
        val viewModel = createViewModel(initialState = loaded)

        viewModel.effect.test {
            viewModel.onAction(TransactionDetailAction.CopyIdClicked)
            assertEquals(TransactionDetailEffect.CopyToClipboard("Transaction ID", "tx-1"), awaitItem())
        }
    }

    @Test
    fun `Should show Copied after copying the ID`() = runTest {
        val viewModel = createViewModel(initialState = loaded)

        viewModel.onAction(TransactionDetailAction.CopyIdClicked)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isIdCopied)
    }

    @Test
    fun `Should clear Copied When the feedback time elapses`() = runTest {
        val viewModel = createViewModel(initialState = loaded.copy(isIdCopied = true))

        viewModel.onAction(TransactionDetailAction.CopiedFeedbackElapsed)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isIdCopied)
    }

    @Test
    fun `Should open Bills for a direct debit`() = runTest {
        coEvery { getTransactionDetail("tx-1") } returns detail(TransactionCategory.DIRECT_DEBIT)
        val viewModel = createViewModel()

        viewModel.effect.test {
            viewModel.onAction(TransactionDetailAction.ManageInBillsClicked)
            assertEquals(TransactionDetailEffect.NavigateToBills, awaitItem())
        }
    }

    @Test
    fun `Should explain that reporting is not available yet`() = runTest {
        val viewModel = createViewModel(initialState = loaded)

        viewModel.effect.test {
            viewModel.onAction(TransactionDetailAction.ReportIssueClicked)
            assertEquals(TransactionDetailEffect.ShowReportIssueUnavailable, awaitItem())
        }
    }

    @Test
    fun `Should navigate back`() = runTest {
        val viewModel = createViewModel(initialState = loaded)

        viewModel.effect.test {
            viewModel.onAction(TransactionDetailAction.BackClicked)
            assertEquals(TransactionDetailEffect.NavigateBack, awaitItem())
        }
    }

    @Test
    fun `Should skip the initial load When state is restored`() = runTest {
        createViewModel(savedStateHandle = SavedStateHandle(mapOf("ui_state" to loaded)))

        advanceUntilIdle()

        coVerify(exactly = 0) { getTransactionDetail(any()) }
    }
}
