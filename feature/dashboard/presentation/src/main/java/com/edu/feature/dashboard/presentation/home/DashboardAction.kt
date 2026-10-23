package com.edu.feature.dashboard.presentation.home

sealed interface DashboardAction {
    data object LoadData : DashboardAction
    data object RetryClicked : DashboardAction
    data object RefreshData : DashboardAction
    data object AvatarClicked : DashboardAction
    data object NotificationsClicked : DashboardAction
    data class AccountSelected(val accountId: String) : DashboardAction
    data object ConnectAnotherBankClicked : DashboardAction
    data object SendClicked : DashboardAction
    data object RequestClicked : DashboardAction
    data object AnalyticsClicked : DashboardAction
    data object StatementsClicked : DashboardAction
    data object PendingClicked : DashboardAction
    data object UpcomingPaymentsClicked : DashboardAction
    data object CardClicked : DashboardAction
    data object SeeAllTransactionsClicked : DashboardAction
    data class TransactionSelected(val transactionId: String) : DashboardAction
}
