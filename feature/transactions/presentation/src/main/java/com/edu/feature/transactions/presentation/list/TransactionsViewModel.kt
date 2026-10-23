package com.edu.feature.transactions.presentation.list

import androidx.lifecycle.SavedStateHandle
import com.edu.core.domain.model.Transaction
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.core.presentation.ui.mapper.mapList
import com.edu.core.presentation.ui.state.MviViewModel
import com.edu.feature.transactions.domain.model.DateWindow
import com.edu.feature.transactions.domain.model.TransactionsPage
import com.edu.feature.transactions.domain.usecase.GetPageWindowUseCase
import com.edu.feature.transactions.domain.usecase.GetPeriodBoundsUseCase
import com.edu.feature.transactions.domain.usecase.GetTransactionsOverviewUseCase
import com.edu.feature.transactions.domain.usecase.GetTransactionsPageUseCase
import com.edu.feature.transactions.presentation.mapper.AccountOptionUiMapper
import com.edu.feature.transactions.presentation.mapper.TransactionFilterMapper
import com.edu.feature.transactions.presentation.mapper.TransactionsContentUiMapper
import com.edu.feature.transactions.presentation.model.CategoryChipUi
import com.edu.feature.transactions.presentation.model.PeriodChipUi
import com.edu.feature.transactions.presentation.model.PeriodSelectionUi
import com.edu.feature.transactions.presentation.model.TransactionsContentUi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import java.time.Instant
import java.time.LocalDate

/**
 * Transactions (12/13).
 *
 * Search and category are client-side (DESIGN_SPEC §4 · 12), so the rows of
 * the loaded windows are kept in [loaded] — a plain in-memory data cache,
 * NOT screen state: it is only read and written inside handleAction flows,
 * which MviViewModel runs one at a time, and everything shown on screen still
 * goes through the UiStateMachine. After process death the restored UiState
 * keeps the filters and the last content; [loaded] is rebuilt on demand from
 * the repository (Room-backed) the first time a filter needs the raw rows.
 */
class TransactionsViewModel(
    savedStateHandle: SavedStateHandle,
    initialState: TransactionsUiState,
    private val getTransactionsPage: GetTransactionsPageUseCase,
    private val getTransactionsOverview: GetTransactionsOverviewUseCase,
    private val getPeriodBounds: GetPeriodBoundsUseCase,
    private val getPageWindow: GetPageWindowUseCase,
    private val filterMapper: TransactionFilterMapper,
    private val contentMapper: TransactionsContentUiMapper,
    private val accountOptionMapper: AccountOptionUiMapper,
) : MviViewModel<TransactionsUiState, TransactionsPartialState, TransactionsAction>(
    savedStateHandle = savedStateHandle,
    initialState = initialState,
) {

    private val _effect = Channel<TransactionsEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private data class Filters(
        val accountId: String?,
        val period: PeriodSelectionUi,
        val query: String,
        val category: CategoryChipUi,
    )

    private class LoadedRows(
        val settled: List<Transaction>,
        val pending: List<Transaction>,
        val offlineSince: Instant?,
    )

    private var loaded: LoadedRows? = null

    init {
        // Skip the initial load after process death unless there was nothing to restore.
        if (!isStateRestored || uiState.value.content == null) onAction(TransactionsAction.LoadData)
    }

    override fun handleAction(action: TransactionsAction): Flow<TransactionsPartialState> = when (action) {
        TransactionsAction.LoadData, TransactionsAction.RetryClicked -> reload(currentFilters())
        TransactionsAction.LoadMore -> loadMore()
        is TransactionsAction.AccountSelected -> selectAccount(action.accountId)
        is TransactionsAction.PeriodSelected -> selectPeriod(action.chip)
        is TransactionsAction.CustomRangeSelected -> flow {
            val period = PeriodSelectionUi(PeriodChipUi.CUSTOM_RANGE, action.startUtcMillis, action.endUtcMillis ?: action.startUtcMillis)
            emit(TransactionsPartialState.DateRangePickerChanged(visible = false))
            emit(TransactionsPartialState.PeriodChanged(period))
            emitReload(currentFilters().copy(period = period))
        }
        TransactionsAction.DateRangePickerDismissed -> flowOf(TransactionsPartialState.DateRangePickerChanged(visible = false))
        is TransactionsAction.QueryChanged -> flow {
            emit(TransactionsPartialState.QueryChanged(action.query))
            emitRefilter(currentFilters().copy(query = action.query))
        }
        is TransactionsAction.CategorySelected -> flow {
            emit(TransactionsPartialState.CategoryChanged(action.category))
            emitRefilter(currentFilters().copy(category = action.category))
        }
        TransactionsAction.ClearFiltersClicked -> clearFilters()
        is TransactionsAction.TransactionSelected -> flow {
            _effect.send(TransactionsEffect.NavigateToTransaction(action.transactionId))
        }
    }

    override fun reduceUiState(
        previousState: TransactionsUiState,
        partialState: TransactionsPartialState,
    ): TransactionsUiState = when (partialState) {
        TransactionsPartialState.LoadStarted -> previousState.copy(
            isLoading = true,
            isLoadingMore = false,
            hasError = false,
            loadMoreFailed = false,
            content = null,
            hasMore = false,
            oldestLoadedEpochDay = null,
        )
        is TransactionsPartialState.PageLoaded -> previousState.copy(
            isLoading = false,
            isLoadingMore = false,
            hasError = false,
            loadMoreFailed = false,
            accounts = partialState.accounts,
            content = partialState.content,
            hasMore = partialState.hasMore,
            oldestLoadedEpochDay = partialState.oldestLoadedEpochDay,
        )
        TransactionsPartialState.LoadFailed -> previousState.copy(isLoading = false, hasError = previousState.content == null)
        TransactionsPartialState.LoadMoreStarted -> previousState.copy(isLoadingMore = true, loadMoreFailed = false)
        TransactionsPartialState.LoadMoreFailed -> previousState.copy(isLoadingMore = false, loadMoreFailed = true)
        is TransactionsPartialState.ContentChanged -> previousState.copy(content = partialState.content)
        is TransactionsPartialState.AccountChanged -> previousState.copy(selectedAccountId = partialState.accountId)
        is TransactionsPartialState.PeriodChanged -> previousState.copy(period = partialState.period)
        is TransactionsPartialState.QueryChanged -> previousState.copy(query = partialState.query)
        is TransactionsPartialState.CategoryChanged -> previousState.copy(category = partialState.category)
        TransactionsPartialState.FiltersCleared -> previousState.copy(
            selectedAccountId = null,
            period = PeriodSelectionUi(),
            query = "",
            category = CategoryChipUi.ALL,
        )
        is TransactionsPartialState.DateRangePickerChanged -> previousState.copy(showDateRangePicker = partialState.visible)
    }

    // --- Filters -------------------------------------------------------------

    private fun currentFilters(): Filters = uiState.value.let { Filters(it.selectedAccountId, it.period, it.query, it.category) }

    private fun selectAccount(accountId: String?): Flow<TransactionsPartialState> {
        if (accountId == uiState.value.selectedAccountId) return emptyFlow()
        return flow {
            emit(TransactionsPartialState.AccountChanged(accountId))
            emitReload(currentFilters().copy(accountId = accountId))
        }
    }

    private fun selectPeriod(chip: PeriodChipUi): Flow<TransactionsPartialState> = when {
        chip == PeriodChipUi.CUSTOM_RANGE -> flowOf(TransactionsPartialState.DateRangePickerChanged(visible = true))
        chip == uiState.value.period.chip -> emptyFlow()
        else -> flow {
            val period = PeriodSelectionUi(chip)
            emit(TransactionsPartialState.PeriodChanged(period))
            emitReload(currentFilters().copy(period = period))
        }
    }

    private fun clearFilters(): Flow<TransactionsPartialState> = flow {
        val before = currentFilters()
        emit(TransactionsPartialState.FiltersCleared)
        val cleared = Filters(accountId = null, period = PeriodSelectionUi(), query = "", category = CategoryChipUi.ALL)
        // A different account or period means different rows; search and category only re-filter.
        if (before.accountId != cleared.accountId || before.period != cleared.period) {
            emitReload(cleared)
        } else {
            emitRefilter(cleared)
        }
    }

    // --- Loading -------------------------------------------------------------

    private fun reload(filters: Filters): Flow<TransactionsPartialState> = flow { emitReload(filters) }

    private suspend fun FlowCollector<TransactionsPartialState>.emitReload(filters: Filters) {
        emit(TransactionsPartialState.LoadStarted)
        loaded = null
        val bounds = getPeriodBounds(filterMapper.toPeriod(filters.period))
        val page = getPageWindow(bounds, loadedFrom = null) ?: return
        when (val result = getTransactionsPage(filters.accountId, page.window, includePending = true)) {
            is Result.Success -> {
                val rows = LoadedRows(result.data.settled, result.data.pending, result.data.offlineSince)
                loaded = rows
                emit(pageLoaded(result.data, rows, filters, page.hasMore, page.window.from))
            }
            is Result.Error -> {
                onLoadError(result.error)
                emit(TransactionsPartialState.LoadFailed)
            }
        }
    }

    private fun loadMore(): Flow<TransactionsPartialState> = flow {
        val state = uiState.value
        val oldest = state.oldestLoadedEpochDay ?: return@flow
        if (!state.hasMore || state.isLoading) return@flow
        val filters = currentFilters()
        val rows = rowsFor(filters, state) ?: return@flow
        val bounds = getPeriodBounds(filterMapper.toPeriod(filters.period))
        val page = getPageWindow(bounds, loadedFrom = LocalDate.ofEpochDay(oldest)) ?: return@flow

        emit(TransactionsPartialState.LoadMoreStarted)
        when (val result = getTransactionsPage(filters.accountId, page.window, includePending = false)) {
            is Result.Success -> {
                val merged = LoadedRows(
                    settled = (rows.settled + result.data.settled).distinctBy { it.transactionId }.sortedByDescending { it.timestamp },
                    pending = rows.pending,
                    offlineSince = result.data.offlineSince ?: rows.offlineSince,
                )
                loaded = merged
                emit(pageLoaded(result.data, merged, filters, page.hasMore, page.window.from))
            }
            is Result.Error -> {
                onLoadError(result.error)
                emit(TransactionsPartialState.LoadMoreFailed)
            }
        }
    }

    /** Search/category: filter the loaded rows again — no network. */
    private suspend fun FlowCollector<TransactionsPartialState>.emitRefilter(filters: Filters) {
        val rows = rowsFor(filters, uiState.value) ?: return
        emit(TransactionsPartialState.ContentChanged(content(rows, filters)))
    }

    /**
     * The loaded rows; after process death they are fetched again for the
     * restored window in one call (served from Room when offline).
     */
    private suspend fun rowsFor(filters: Filters, state: TransactionsUiState): LoadedRows? {
        loaded?.let { return it }
        val oldest = state.oldestLoadedEpochDay ?: return null
        val bounds = getPeriodBounds(filterMapper.toPeriod(filters.period))
        val window = DateWindow(LocalDate.ofEpochDay(oldest), bounds.latest)
        return when (val result = getTransactionsPage(filters.accountId, window, includePending = true)) {
            is Result.Success -> LoadedRows(result.data.settled, result.data.pending, result.data.offlineSince).also { loaded = it }
            is Result.Error -> {
                onLoadError(result.error)
                null
            }
        }
    }

    private fun pageLoaded(
        page: TransactionsPage,
        rows: LoadedRows,
        filters: Filters,
        hasMore: Boolean,
        oldest: LocalDate,
    ) = TransactionsPartialState.PageLoaded(
        accounts = accountOptionMapper.mapList(page.accounts),
        content = content(rows, filters),
        hasMore = hasMore,
        oldestLoadedEpochDay = oldest.toEpochDay(),
    )

    private fun content(rows: LoadedRows, filters: Filters): TransactionsContentUi = contentMapper.map(
        getTransactionsOverview(
            settled = rows.settled,
            pending = rows.pending,
            period = filterMapper.toPeriod(filters.period),
            query = filters.query,
            category = filterMapper.toCategory(filters.category),
            offlineSince = rows.offlineSince,
        ),
    )

    private suspend fun onLoadError(error: DataError) {
        if (error == DataError.NetworkError.UNAUTHORIZED) _effect.send(TransactionsEffect.NavigateToConnectBank)
    }
}
