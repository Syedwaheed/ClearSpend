package com.edu.feature.transactions.presentation.list

import com.edu.feature.transactions.presentation.model.CategoryChipUi
import com.edu.feature.transactions.presentation.model.PeriodChipUi

sealed interface TransactionsAction {
    data object LoadData : TransactionsAction
    data object RetryClicked : TransactionsAction

    /** The end of the list scrolled into view (or "Try again" after a failed page). */
    data object LoadMore : TransactionsAction

    /** Null = "All accounts". */
    data class AccountSelected(val accountId: String?) : TransactionsAction

    /** CUSTOM_RANGE opens the DateRangePicker instead of applying straight away. */
    data class PeriodSelected(val chip: PeriodChipUi) : TransactionsAction

    /** DateRangePicker confirm: UTC-midnight millis of the first and last picked day. */
    data class CustomRangeSelected(val startUtcMillis: Long, val endUtcMillis: Long?) : TransactionsAction
    data object DateRangePickerDismissed : TransactionsAction

    data class QueryChanged(val query: String) : TransactionsAction
    data class CategorySelected(val category: CategoryChipUi) : TransactionsAction
    data object ClearFiltersClicked : TransactionsAction
    data class TransactionSelected(val transactionId: String) : TransactionsAction
}
