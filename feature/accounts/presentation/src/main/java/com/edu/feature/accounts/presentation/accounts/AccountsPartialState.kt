package com.edu.feature.accounts.presentation.accounts

import com.edu.feature.accounts.presentation.model.AccountFilterUi
import com.edu.feature.accounts.presentation.model.AccountsContentUi

sealed interface AccountsPartialState {
    data class LoadingChanged(val isLoading: Boolean) : AccountsPartialState
    data class RefreshingChanged(val isRefreshing: Boolean) : AccountsPartialState
    data class ContentLoaded(val content: AccountsContentUi) : AccountsPartialState
    data object LoadFailed : AccountsPartialState
    data class FilterChanged(val filter: AccountFilterUi) : AccountsPartialState
}
