package com.edu.feature.accounts.presentation.accounts

import com.edu.feature.accounts.presentation.model.AccountFilterUi

sealed interface AccountsAction {
    data object LoadData : AccountsAction
    data object RetryClicked : AccountsAction
    data object RefreshClicked : AccountsAction
    data class FilterSelected(val filter: AccountFilterUi) : AccountsAction
    data class AccountSelected(val accountId: String) : AccountsAction
    data object CardSelected : AccountsAction
    data object RenewConsentClicked : AccountsAction
    data object ConnectAnotherBankClicked : AccountsAction
}
