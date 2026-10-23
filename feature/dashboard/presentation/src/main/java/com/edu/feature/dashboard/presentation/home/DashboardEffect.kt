package com.edu.feature.dashboard.presentation.home

sealed interface DashboardEffect {
    data object NavigateToSettings : DashboardEffect
    data object NavigateToNotifications : DashboardEffect
    data class NavigateToAccount(val accountId: String) : DashboardEffect
    data object NavigateToBankSelection : DashboardEffect
    data object OpenSendMoney : DashboardEffect
    data object NavigateToAnalytics : DashboardEffect
    data object NavigateToTransactions : DashboardEffect
    data object NavigateToBills : DashboardEffect
    data object NavigateToCard : DashboardEffect
    data class NavigateToTransaction(val transactionId: String) : DashboardEffect
    /** 401 after a failed refresh: consent expired → Connect Bank (DESIGN_SPEC §7). */
    data object NavigateToConnectBank : DashboardEffect
    /** "Request" and "Statements" have no spec yet. */
    data object ShowComingSoon : DashboardEffect
}
