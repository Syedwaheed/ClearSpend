package com.edu.feature.transactions.presentation.detail

sealed interface TransactionDetailAction {
    data object LoadData : TransactionDetailAction
    data object RetryClicked : TransactionDetailAction
    data object BackClicked : TransactionDetailAction
    data object CopyIdClicked : TransactionDetailAction

    /** Sent by the Screen 1.2 s after "Copied!" appeared. */
    data object CopiedFeedbackElapsed : TransactionDetailAction
    data object ManageInBillsClicked : TransactionDetailAction
    data object ReportIssueClicked : TransactionDetailAction
}
