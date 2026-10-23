package com.edu.feature.auth.presentation.bankselection

sealed interface BankSelectionAction {
    data object LoadBanks : BankSelectionAction
    data class QueryChanged(val query: String) : BankSelectionAction
    data object ClearQueryClicked : BankSelectionAction
    data class BankSelected(val providerId: String) : BankSelectionAction
    /** "Don't see your bank?" — open the auth link with every provider. */
    data object OtherBankClicked : BankSelectionAction
    data object RetryClicked : BankSelectionAction
    data object BackClicked : BankSelectionAction
    /** The screen could not open a browser for the auth link. */
    data object BrowserUnavailable : BankSelectionAction
}
