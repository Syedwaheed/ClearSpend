package com.edu.clearspend.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** Full route set from CLAUDE.md §10. Features never reference these. */
sealed interface Route : NavKey {
    // Start-up
    @Serializable data object Splash : Route
    @Serializable data object Onboarding : Route

    // Auth
    @Serializable data object ConnectBank : Route
    @Serializable data object BankSelection : Route
    @Serializable data object Connecting : Route
    @Serializable data object BiometricLogin : Route

    // Main (with bottom nav)
    @Serializable data object Dashboard : Route
    @Serializable data object Accounts : Route
    @Serializable data class AccountDetail(val accountId: String) : Route
    @Serializable data object Card : Route
    @Serializable data object SendMoney : Route // bottom sheet
    @Serializable data object Transactions : Route
    @Serializable data class TransactionDetail(val transactionId: String) : Route
    @Serializable data object Analytics : Route
    @Serializable data object Bills : Route
    @Serializable data object Notifications : Route
    @Serializable data object Settings : Route
}

/** Bottom-nav slots in ClearSpendBottomNav order; index 2 is the Send FAB. */
internal val bottomNavTabs: List<Route> = listOf(
    Route.Dashboard,
    Route.Accounts,
    Route.SendMoney,
    Route.Transactions,
    Route.Analytics,
)

/** Routes that show the bottom navigation (top-level tabs only). */
internal val bottomNavRoutes: Set<Route> = setOf(
    Route.Dashboard,
    Route.Accounts,
    Route.Transactions,
    Route.Analytics,
)
