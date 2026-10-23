package com.edu.feature.accounts.presentation.detail

import com.edu.feature.accounts.presentation.model.CopiedFieldUi

sealed interface AccountDetailAction {
    data object LoadData : AccountDetailAction
    data object RetryClicked : AccountDetailAction
    data object BackClicked : AccountDetailAction
    data object CopySortCodeClicked : AccountDetailAction
    data object CopyAccountNumberClicked : AccountDetailAction
    /** Sent by the Screen 1.2 s after "Copied!" appeared. */
    data class CopiedFeedbackElapsed(val field: CopiedFieldUi) : AccountDetailAction
    data class TransactionSelected(val transactionId: String) : AccountDetailAction
    data object ViewAllTransactionsClicked : AccountDetailAction
}
