package com.edu.feature.transactions.presentation.list

import android.os.Parcelable
import com.edu.feature.transactions.presentation.model.AccountOptionUi
import com.edu.feature.transactions.presentation.model.CategoryChipUi
import com.edu.feature.transactions.presentation.model.PeriodChipUi
import com.edu.feature.transactions.presentation.model.PeriodSelectionUi
import com.edu.feature.transactions.presentation.model.TransactionsContentUi
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class TransactionsUiState(
    /** First page of the current account/period is loading (skeleton). */
    val isLoading: Boolean = false,
    /** An older 30-day window is loading (spinner at the end of the list). */
    val isLoadingMore: Boolean = false,
    val hasError: Boolean = false,
    val loadMoreFailed: Boolean = false,
    val accounts: List<AccountOptionUi> = emptyList(),
    /** Null = "All accounts". */
    val selectedAccountId: String? = null,
    val period: PeriodSelectionUi = PeriodSelectionUi(),
    val query: String = "",
    val category: CategoryChipUi = CategoryChipUi.ALL,
    val showDateRangePicker: Boolean = false,
    val content: TransactionsContentUi? = null,
    /** An older window exists inside the period ("extend on scroll"). */
    val hasMore: Boolean = false,
    /** Oldest calendar day loaded so far (epoch day); null before the first page. */
    val oldestLoadedEpochDay: Long? = null,
) : Parcelable {

    @IgnoredOnParcel
    val selectedAccount: AccountOptionUi? = accounts.firstOrNull { it.accountId == selectedAccountId }

    @IgnoredOnParcel
    val hasActiveFilters: Boolean =
        query.isNotBlank() || category != CategoryChipUi.ALL || period.chip != PeriodChipUi.ALL || selectedAccountId != null
}
