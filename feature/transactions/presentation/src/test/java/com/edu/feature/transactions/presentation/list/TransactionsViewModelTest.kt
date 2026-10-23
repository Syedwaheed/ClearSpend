package com.edu.feature.transactions.presentation.list

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.feature.transactions.domain.model.DateWindow
import com.edu.feature.transactions.domain.model.TransactionsPage
import com.edu.feature.transactions.domain.usecase.GetFilteredTransactionsUseCase
import com.edu.feature.transactions.domain.usecase.GetPageWindowUseCase
import com.edu.feature.transactions.domain.usecase.GetPeriodBoundsUseCase
import com.edu.feature.transactions.domain.usecase.GetTransactionDayGroupsUseCase
import com.edu.feature.transactions.domain.usecase.GetTransactionsOverviewUseCase
import com.edu.feature.transactions.domain.usecase.GetTransactionsPageUseCase
import com.edu.feature.transactions.domain.usecase.GetTransactionsSummaryUseCase
import com.edu.feature.transactions.presentation.fake.CLOCK
import com.edu.feature.transactions.presentation.fake.FakeAccountOptionUiMapper
import com.edu.feature.transactions.presentation.fake.FakeTransactionsContentUiMapper
import com.edu.feature.transactions.presentation.fake.TODAY
import com.edu.feature.transactions.presentation.fake.account
import com.edu.feature.transactions.presentation.fake.settledIds
import com.edu.feature.transactions.presentation.fake.tx
import com.edu.feature.transactions.presentation.mapper.TransactionFilterMapperImpl
import com.edu.feature.transactions.presentation.model.CategoryChipUi
import com.edu.feature.transactions.presentation.model.PeriodChipUi
import com.edu.feature.transactions.presentation.model.PeriodSelectionUi
import com.edu.feature.transactions.presentation.model.TransactionsContentUi
import com.edu.feature.transactions.presentation.model.TransactionsSummaryUi
import com.edu.feature.transactions.presentation.util.MainDispatcherRule
import com.edu.core.domain.model.TransactionCategory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class TransactionsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getTransactionsPage: GetTransactionsPageUseCase = mockk()

    private val firstWindow = DateWindow(TODAY.minusDays(29), TODAY)
    private val secondWindow = DateWindow(TODAY.minusDays(59), TODAY.minusDays(30))

    private val tesco = tx("tesco", "2026-06-10T06:00:00Z", merchant = "Tesco")
    private val edf = tx("edf", "2026-06-05T09:14:00Z", "-24.50", merchant = "EDF Energy", category = TransactionCategory.DIRECT_DEBIT)
    private val older = tx("older", "2026-05-01T10:00:00Z", merchant = "Amazon")
    private val coffee = tx("coffee", "2026-06-15T08:41:00Z", "-4.65", merchant = "Costa Coffee", pending = true, runningBalance = null)

    private fun page(vararg settled: com.edu.core.domain.model.Transaction, pending: List<com.edu.core.domain.model.Transaction> = emptyList()) =
        Result.Success(TransactionsPage(listOf(account("acc-1"), account("acc-2")), settled.toList(), pending, null))

    @Before
    fun setUp() {
        coEvery { getTransactionsPage(any(), any(), any()) } returns page()
        coEvery { getTransactionsPage(null, firstWindow, true) } returns page(tesco, edf, pending = listOf(coffee))
        coEvery { getTransactionsPage(null, secondWindow, false) } returns page(older)
    }

    private fun createViewModel(
        initialState: TransactionsUiState = TransactionsUiState(),
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
    ) = TransactionsViewModel(
        savedStateHandle = savedStateHandle,
        initialState = initialState,
        getTransactionsPage = getTransactionsPage,
        getTransactionsOverview = GetTransactionsOverviewUseCase(
            getPeriodBounds = GetPeriodBoundsUseCase(CLOCK),
            getFilteredTransactions = GetFilteredTransactionsUseCase(),
            getDayGroups = GetTransactionDayGroupsUseCase(CLOCK),
            getSummary = GetTransactionsSummaryUseCase(),
            clock = CLOCK,
        ),
        getPeriodBounds = GetPeriodBoundsUseCase(CLOCK),
        getPageWindow = GetPageWindowUseCase(),
        filterMapper = TransactionFilterMapperImpl(),
        contentMapper = FakeTransactionsContentUiMapper(),
        accountOptionMapper = FakeAccountOptionUiMapper(),
    )

    private fun restoredContent() = TransactionsContentUi(
        summary = TransactionsSummaryUi(true, "", 0, "", "", "", emptyList()),
        pending = emptyList(),
        formattedPendingTotal = "",
        groups = emptyList(),
        offlineSyncTime = null,
    )

    private fun utcMillis(date: LocalDate) = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    @Test
    fun `Should show the newest transactions When the screen opens`() = runTest {
        val viewModel = createViewModel()

        advanceUntilIdle()

        assertEquals(listOf("tesco", "edf"), viewModel.uiState.value.content?.settledIds)
    }

    @Test
    fun `Should load the newest 30 days of all accounts with pending first`() = runTest {
        createViewModel()

        advanceUntilIdle()

        coVerify { getTransactionsPage(null, firstWindow, true) }
    }

    @Test
    fun `Should show the pending section`() = runTest {
        val viewModel = createViewModel()

        advanceUntilIdle()

        assertEquals(listOf("coffee"), viewModel.uiState.value.content?.pending?.map { it.transactionId })
    }

    @Test
    fun `Should fill the account switcher`() = runTest {
        val viewModel = createViewModel()

        advanceUntilIdle()

        assertEquals(listOf("acc-1", "acc-2"), viewModel.uiState.value.accounts.map { it.accountId })
    }

    @Test
    fun `Should allow older pages for All`() = runTest {
        val viewModel = createViewModel()

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasMore)
    }

    @Test
    fun `Should show the error state When loading fails`() = runTest {
        coEvery { getTransactionsPage(null, firstWindow, true) } returns Result.Error(DataError.NetworkError.SERVER_ERROR)
        val viewModel = createViewModel()

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasError)
    }

    @Test
    fun `Should route to Connect Bank When unauthorized`() = runTest {
        coEvery { getTransactionsPage(null, firstWindow, true) } returns Result.Error(DataError.NetworkError.UNAUTHORIZED)
        val viewModel = createViewModel()

        viewModel.effect.test {
            assertEquals(TransactionsEffect.NavigateToConnectBank, awaitItem())
        }
    }

    @Test
    fun `Should load again When Try again is tapped`() = runTest {
        coEvery { getTransactionsPage(null, firstWindow, true) } returns Result.Error(DataError.NetworkError.SERVER_ERROR)
        val viewModel = createViewModel()
        advanceUntilIdle()
        coEvery { getTransactionsPage(null, firstWindow, true) } returns page(tesco)

        viewModel.onAction(TransactionsAction.RetryClicked)
        advanceUntilIdle()

        assertEquals(listOf("tesco"), viewModel.uiState.value.content?.settledIds)
    }

    @Test
    fun `Should skip the initial load When state is restored`() = runTest {
        val restored = TransactionsUiState(content = restoredContent(), oldestLoadedEpochDay = firstWindow.from.toEpochDay())
        createViewModel(savedStateHandle = SavedStateHandle(mapOf("ui_state" to restored)))

        advanceUntilIdle()

        coVerify(exactly = 0) { getTransactionsPage(any(), any(), any()) }
    }

    @Test
    fun `Should load When the restored state had no content yet`() = runTest {
        createViewModel(savedStateHandle = SavedStateHandle(mapOf("ui_state" to TransactionsUiState(isLoading = true))))

        advanceUntilIdle()

        coVerify { getTransactionsPage(null, firstWindow, true) }
    }

    @Test
    fun `Should search the loaded rows`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(TransactionsAction.QueryChanged("edf"))
        advanceUntilIdle()

        assertEquals(listOf("edf"), viewModel.uiState.value.content?.settledIds)
    }

    @Test
    fun `Should not call the network When searching`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(TransactionsAction.QueryChanged("edf"))
        advanceUntilIdle()

        coVerify(exactly = 1) { getTransactionsPage(any(), any(), any()) }
    }

    @Test
    fun `Should keep the query in the state`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(TransactionsAction.QueryChanged("tes"))
        advanceUntilIdle()

        assertEquals("tes", viewModel.uiState.value.query)
    }

    @Test
    fun `Should filter by category`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(TransactionsAction.CategorySelected(CategoryChipUi.DIRECT_DEBIT))
        advanceUntilIdle()

        assertEquals(listOf("edf"), viewModel.uiState.value.content?.settledIds)
    }

    @Test
    fun `Should load only the selected account`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(TransactionsAction.AccountSelected("acc-2"))
        advanceUntilIdle()

        coVerify { getTransactionsPage("acc-2", firstWindow, true) }
    }

    @Test
    fun `Should load This Month in one page`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(TransactionsAction.PeriodSelected(PeriodChipUi.THIS_MONTH))
        advanceUntilIdle()

        coVerify { getTransactionsPage(null, DateWindow(LocalDate.of(2026, 6, 1), TODAY), true) }
    }

    @Test
    fun `Should have no older page for This Month`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(TransactionsAction.PeriodSelected(PeriodChipUi.THIS_MONTH))
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hasMore)
    }

    @Test
    fun `Should open the date range picker for Custom Range`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(TransactionsAction.PeriodSelected(PeriodChipUi.CUSTOM_RANGE))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showDateRangePicker)
    }

    @Test
    fun `Should load the picked custom range`() = runTest {
        val viewModel = createViewModel(initialState = TransactionsUiState(showDateRangePicker = true))
        advanceUntilIdle()

        viewModel.onAction(
            TransactionsAction.CustomRangeSelected(utcMillis(LocalDate.of(2026, 5, 21)), utcMillis(LocalDate.of(2026, 6, 1))),
        )
        advanceUntilIdle()

        coVerify { getTransactionsPage(null, DateWindow(LocalDate.of(2026, 5, 21), LocalDate.of(2026, 6, 1)), true) }
    }

    @Test
    fun `Should close the picker When a range is picked`() = runTest {
        val viewModel = createViewModel(initialState = TransactionsUiState(showDateRangePicker = true))

        viewModel.onAction(TransactionsAction.CustomRangeSelected(utcMillis(TODAY), null))
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showDateRangePicker)
    }

    @Test
    fun `Should close the picker When it is dismissed`() = runTest {
        val viewModel = createViewModel(initialState = TransactionsUiState(showDateRangePicker = true))

        viewModel.onAction(TransactionsAction.DateRangePickerDismissed)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showDateRangePicker)
    }

    @Test
    fun `Should load the previous 30 days When scrolled to the end`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(TransactionsAction.LoadMore)
        advanceUntilIdle()

        coVerify { getTransactionsPage(null, secondWindow, false) }
    }

    @Test
    fun `Should append the older page`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(TransactionsAction.LoadMore)
        advanceUntilIdle()

        assertEquals(listOf("tesco", "edf", "older"), viewModel.uiState.value.content?.settledIds)
    }

    @Test
    fun `Should remember the oldest loaded day`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(TransactionsAction.LoadMore)
        advanceUntilIdle()

        assertEquals(secondWindow.from.toEpochDay(), viewModel.uiState.value.oldestLoadedEpochDay)
    }

    @Test
    fun `Should offer a retry When an older page fails`() = runTest {
        coEvery { getTransactionsPage(null, secondWindow, false) } returns Result.Error(DataError.NetworkError.NO_INTERNET)
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(TransactionsAction.LoadMore)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.loadMoreFailed)
    }

    @Test
    fun `Should not load more When the period is fully loaded`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(TransactionsAction.PeriodSelected(PeriodChipUi.THIS_MONTH))
        advanceUntilIdle()

        viewModel.onAction(TransactionsAction.LoadMore)
        advanceUntilIdle()

        coVerify(exactly = 0) { getTransactionsPage(any(), any(), false) }
    }

    @Test
    fun `Should clear the search When Clear filters is tapped`() = runTest {
        val viewModel = createViewModel(initialState = TransactionsUiState(query = "zzzz"))

        viewModel.onAction(TransactionsAction.ClearFiltersClicked)
        advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.query)
    }

    @Test
    fun `Should show every row again When Clear filters is tapped`() = runTest {
        val viewModel = createViewModel(initialState = TransactionsUiState(query = "zzzz", category = CategoryChipUi.CASH))
        advanceUntilIdle()

        viewModel.onAction(TransactionsAction.ClearFiltersClicked)
        advanceUntilIdle()

        assertEquals(listOf("tesco", "edf"), viewModel.uiState.value.content?.settledIds)
    }

    @Test
    fun `Should reload all accounts When filters are cleared from one account`() = runTest {
        val viewModel = createViewModel(initialState = TransactionsUiState(selectedAccountId = "acc-2"))
        advanceUntilIdle()

        viewModel.onAction(TransactionsAction.ClearFiltersClicked)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.selectedAccountId)
    }

    @Test
    fun `Should open a transaction`() = runTest {
        val viewModel = createViewModel()

        viewModel.effect.test {
            viewModel.onAction(TransactionsAction.TransactionSelected("tesco"))
            assertEquals(TransactionsEffect.NavigateToTransaction("tesco"), awaitItem())
        }
    }

    @Test
    fun `Should reload the restored window When searching after process death`() = runTest {
        val restored = TransactionsUiState(
            period = PeriodSelectionUi(PeriodChipUi.ALL),
            content = restoredContent(),
            oldestLoadedEpochDay = secondWindow.from.toEpochDay(),
            hasMore = true,
        )
        val viewModel = createViewModel(savedStateHandle = SavedStateHandle(mapOf("ui_state" to restored)))

        viewModel.onAction(TransactionsAction.QueryChanged("tesco"))
        advanceUntilIdle()

        coVerify { getTransactionsPage(null, DateWindow(secondWindow.from, TODAY), true) }
    }
}
