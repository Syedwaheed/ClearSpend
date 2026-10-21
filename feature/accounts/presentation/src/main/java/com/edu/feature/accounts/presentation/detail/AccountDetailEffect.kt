package com.edu.feature.accounts.presentation.detail

sealed interface AccountDetailEffect {
    data object NavigateBack : AccountDetailEffect
    /** The Screen writes [value] to the system clipboard (ClipboardManager). */
    data class CopyToClipboard(val label: String, val value: String) : AccountDetailEffect
    data class NavigateToTransaction(val transactionId: String) : AccountDetailEffect
    data object NavigateToTransactions : AccountDetailEffect
    data object NavigateToConnectBank : AccountDetailEffect
}
