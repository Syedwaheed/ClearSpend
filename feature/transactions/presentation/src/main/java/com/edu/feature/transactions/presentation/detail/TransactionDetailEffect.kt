package com.edu.feature.transactions.presentation.detail

sealed interface TransactionDetailEffect {
    data object NavigateBack : TransactionDetailEffect

    /** The Screen writes [value] to the system clipboard. */
    data class CopyToClipboard(val label: String, val value: String) : TransactionDetailEffect
    data object NavigateToBills : TransactionDetailEffect
    data object NavigateToConnectBank : TransactionDetailEffect

    /** No dispute flow exists yet; the Screen says so in a toast. */
    data object ShowReportIssueUnavailable : TransactionDetailEffect
}
