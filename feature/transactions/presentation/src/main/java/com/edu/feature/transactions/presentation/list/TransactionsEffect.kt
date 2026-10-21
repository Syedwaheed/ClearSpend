package com.edu.feature.transactions.presentation.list

sealed interface TransactionsEffect {
    data class NavigateToTransaction(val transactionId: String) : TransactionsEffect

    /** 401 after a failed refresh: consent expired (DESIGN_SPEC §7). */
    data object NavigateToConnectBank : TransactionsEffect
}
