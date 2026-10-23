package com.edu.feature.transactions.presentation.list

import com.edu.feature.transactions.presentation.model.AccountOptionUi
import com.edu.feature.transactions.presentation.model.CategoryChipUi
import com.edu.feature.transactions.presentation.model.PeriodSelectionUi
import com.edu.feature.transactions.presentation.model.TransactionsContentUi

sealed interface TransactionsPartialState {
    /** A new account/period: drop what was shown and load its first window. */
    data object LoadStarted : TransactionsPartialState

    data class PageLoaded(
        val accounts: List<AccountOptionUi>,
        val content: TransactionsContentUi,
        val hasMore: Boolean,
        val oldestLoadedEpochDay: Long,
    ) : TransactionsPartialState

    data object LoadFailed : TransactionsPartialState
    data object LoadMoreStarted : TransactionsPartialState
    data object LoadMoreFailed : TransactionsPartialState

    /** Search or category changed: same rows, filtered again. */
    data class ContentChanged(val content: TransactionsContentUi) : TransactionsPartialState

    data class AccountChanged(val accountId: String?) : TransactionsPartialState
    data class PeriodChanged(val period: PeriodSelectionUi) : TransactionsPartialState
    data class QueryChanged(val query: String) : TransactionsPartialState
    data class CategoryChanged(val category: CategoryChipUi) : TransactionsPartialState
    data object FiltersCleared : TransactionsPartialState
    data class DateRangePickerChanged(val visible: Boolean) : TransactionsPartialState
}
