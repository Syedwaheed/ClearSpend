package com.edu.feature.accounts.presentation.accounts

import android.os.Parcelable
import com.edu.feature.accounts.presentation.model.AccountFilterUi
import com.edu.feature.accounts.presentation.model.AccountsContentUi
import com.edu.feature.accounts.presentation.model.ProviderGroupUi
import kotlinx.parcelize.Parcelize

@Parcelize
data class AccountsUiState(
    val isLoading: Boolean = false,
    /** Refresh button pressed: shows "Syncing…" over the existing content. */
    val isRefreshing: Boolean = false,
    val hasError: Boolean = false,
    val content: AccountsContentUi? = null,
    val selectedFilter: AccountFilterUi = AccountFilterUi.ALL,
) : Parcelable {

    val isEmpty: Boolean get() = !isLoading && !hasError && content != null && content.groups.isEmpty() && content.cards.isEmpty()

    /** Provider groups narrowed to the selected chip; groups left empty are dropped. */
    val visibleGroups: List<ProviderGroupUi>
        get() = content?.groups.orEmpty().mapNotNull { group ->
            if (selectedFilter == AccountFilterUi.ALL) return@mapNotNull group
            group.copy(accounts = group.accounts.filter { it.filter == selectedFilter }).takeIf { it.accounts.isNotEmpty() }
        }

    /** Credit cards are not an account type, so they only show under "All". */
    val showsCards: Boolean get() = selectedFilter == AccountFilterUi.ALL && content?.cards?.isNotEmpty() == true
}
