package com.edu.clearspend.navigation

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.edu.clearspend.deeplink.PendingAuthRedirect
import com.edu.clearspend.placeholder.PlaceholderScreen
import com.edu.clearspend.splash.SplashScreen
import com.edu.core.presentation.ui.ClearSpendBottomNav
import com.edu.feature.accounts.presentation.accounts.AccountsScreen
import com.edu.feature.accounts.presentation.card.CardScreen
import com.edu.feature.accounts.presentation.detail.AccountDetailScreen
import com.edu.feature.auth.presentation.bankselection.BankSelectionScreen
import com.edu.feature.auth.presentation.biometric.BiometricScreen
import com.edu.feature.auth.presentation.connectbank.ConnectBankScreen
import com.edu.feature.auth.presentation.connecting.AuthRedirectParams
import com.edu.feature.auth.presentation.connecting.ConnectingScreen
import com.edu.feature.auth.presentation.onboarding.OnboardingScreen
import com.edu.feature.dashboard.presentation.home.DashboardScreen

/**
 * Navigation3 host. Feature screens expose callbacks; this is the only place
 * that maps them to [Route]s (CLAUDE.md §10).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClearSpendNavHost(
    pendingRedirect: PendingAuthRedirect?,
    onRedirectConsumed: () -> Unit,
) {
    val backStack = rememberNavBackStack(Route.Splash)
    val activity = LocalActivity.current
    val bottomSheetStrategy = remember { BottomSheetSceneStrategy<NavKey>() }

    // Each bottom-nav tab keeps its own back stack; these survive config changes and process death.
    val homeHistory = rememberNavBackStack(Route.Dashboard)
    val accountsHistory = rememberNavBackStack(Route.Accounts)
    val transactionsHistory = rememberNavBackStack(Route.Transactions)
    val analyticsHistory = rememberNavBackStack(Route.Analytics)
    val tabHistories: Map<Route, MutableList<NavKey>> = mapOf(
        Route.Dashboard to homeHistory,
        Route.Accounts to accountsHistory,
        Route.Transactions to transactionsHistory,
        Route.Analytics to analyticsHistory,
    )

    // A TrueLayer redirect always lands on Connecting (07).
    LaunchedEffect(pendingRedirect) {
        if (pendingRedirect != null && backStack.lastOrNull() != Route.Connecting) {
            backStack.add(Route.Connecting)
        }
    }

    // Consent expired / refresh failed (401): start the auth flow again (DESIGN_SPEC §7).
    val toConnectBank: () -> Unit = {
        tabHistories.forEach { (tab, history) -> history.replaceAll(tab) }
        backStack.replaceAll(Route.ConnectBank)
    }
    val openSendSheet: () -> Unit = { if (backStack.lastOrNull() != Route.SendMoney) backStack.add(Route.SendMoney) }
    val goBack: () -> Unit = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) }
    val switchTab: (Route) -> Unit = { tab -> backStack.switchTab(tab, tabHistories) }

    Box(Modifier.fillMaxSize()) {
        NavDisplay(
            backStack = backStack,
            onBack = {
                if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) else activity?.finish()
            },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            sceneStrategies = listOf(bottomSheetStrategy),
            entryProvider = entryProvider {
                // --- Start-up -----------------------------------------------------
                entry<Route.Splash> {
                    SplashScreen(
                        onNavigateToOnboarding = { backStack.replaceAll(Route.Onboarding) },
                        onNavigateToConnectBank = { backStack.replaceAll(Route.ConnectBank) },
                        onNavigateToBiometric = { backStack.replaceAll(Route.BiometricLogin) },
                        onNavigateToDashboard = { backStack.replaceAll(Route.Dashboard) },
                    )
                }
                entry<Route.Onboarding> {
                    OnboardingScreen(
                        onNavigateToConnectBank = { backStack.replaceAll(Route.ConnectBank) },
                        // Signing in = granting bank consent again (no app account exists).
                        onNavigateToSignIn = { backStack.replaceAll(Route.ConnectBank) },
                    )
                }

                // --- Auth -----------------------------------------------------------
                entry<Route.ConnectBank> {
                    ConnectBankScreen(onNavigateToBankSelection = { backStack.add(Route.BankSelection) })
                }
                entry<Route.BankSelection> {
                    // Reached from Connect Bank (05) or, when already connected, from
                    // "Connect another bank" / "Renew" on Home and Accounts: back returns there.
                    BankSelectionScreen(
                        onNavigateBack = {
                            if (Route.ConnectBank in backStack) backStack.popTo(Route.ConnectBank) else goBack()
                        },
                    )
                }
                entry<Route.Connecting> {
                    ConnectingScreen(
                        redirect = pendingRedirect?.let { AuthRedirectParams(it.code, it.state, it.error) },
                        onRedirectConsumed = onRedirectConsumed,
                        onNavigateToDashboard = { backStack.replaceAll(Route.Dashboard) },
                        onNavigateToConnectBank = { backStack.popTo(Route.ConnectBank) },
                    )
                }
                entry<Route.BiometricLogin> {
                    BiometricScreen(
                        onNavigateToDashboard = { backStack.replaceAll(Route.Dashboard) },
                        onNavigateToConnectBank = { backStack.replaceAll(Route.ConnectBank) },
                    )
                }

                // --- Main (Phase 3) -------------------------------------------------
                entry<Route.Dashboard> {
                    DashboardScreen(
                        onNavigateToSettings = { backStack.add(Route.Settings) },
                        onNavigateToNotifications = { backStack.add(Route.Notifications) },
                        onNavigateToAccount = { accountId -> backStack.add(Route.AccountDetail(accountId)) },
                        onNavigateToBankSelection = { backStack.add(Route.BankSelection) },
                        onOpenSendMoney = openSendSheet,
                        onNavigateToAnalytics = { switchTab(Route.Analytics) },
                        onNavigateToTransactions = { switchTab(Route.Transactions) },
                        onNavigateToBills = { backStack.add(Route.Bills) },
                        onNavigateToCard = { backStack.add(Route.Card) },
                        onNavigateToTransaction = { transactionId -> backStack.add(Route.TransactionDetail(transactionId)) },
                        onNavigateToConnectBank = toConnectBank,
                    )
                }
                entry<Route.Accounts> {
                    AccountsScreen(
                        onNavigateToAccount = { accountId -> backStack.add(Route.AccountDetail(accountId)) },
                        onNavigateToCard = { backStack.add(Route.Card) },
                        onNavigateToBankSelection = { backStack.add(Route.BankSelection) },
                        onNavigateToConnectBank = toConnectBank,
                    )
                }
                entry<Route.AccountDetail> { route ->
                    AccountDetailScreen(
                        accountId = route.accountId,
                        onNavigateBack = goBack,
                        onNavigateToTransaction = { transactionId -> backStack.add(Route.TransactionDetail(transactionId)) },
                        onNavigateToTransactions = { switchTab(Route.Transactions) },
                        onNavigateToConnectBank = toConnectBank,
                    )
                }
                entry<Route.Card> {
                    CardScreen(onNavigateBack = goBack, onNavigateToConnectBank = toConnectBank)
                }
                entry<Route.SendMoney>(metadata = BottomSheetSceneStrategy.bottomSheet()) {
                    PlaceholderScreen("Send money", "Built in Phase 5.")
                }

                // --- Later phases: placeholders --------------------------------------
                entry<Route.Transactions> { PlaceholderScreen("Transactions", "Built in Phase 4.") }
                entry<Route.TransactionDetail> { PlaceholderScreen("Transaction", "Built in Phase 4.") }
                entry<Route.Analytics> { PlaceholderScreen("Analytics", "Built in Phase 6.") }
                entry<Route.Bills> { PlaceholderScreen("Bills & subscriptions", "Built in Phase 6.") }
                entry<Route.Notifications> { PlaceholderScreen("Notifications", "Built in Phase 7.") }
                entry<Route.Settings> { PlaceholderScreen("Profile & settings", "Built in Phase 7.") }
            },
        )

        if (backStack.showsBottomNav()) {
            ClearSpendBottomNav(
                selectedTab = backStack.currentTabIndex(),
                onTabSelected = { index ->
                    when (val tab = bottomNavTabs[index]) {
                        Route.SendMoney -> openSendSheet()
                        else -> switchTab(tab)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding(),
            )
        }
    }
}
