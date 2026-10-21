package com.edu.feature.accounts.presentation.accounts

sealed interface AccountsEffect {
    data class NavigateToAccount(val accountId: String) : AccountsEffect
    data object NavigateToCard : AccountsEffect
    /** Renew consent and "Connect another bank" both re-run the auth flow from Bank Selection (06). */
    data object NavigateToBankSelection : AccountsEffect
    data object NavigateToConnectBank : AccountsEffect
}
