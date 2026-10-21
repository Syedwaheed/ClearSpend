package com.edu.feature.dashboard.presentation.home

import com.edu.core.presentation.ui.model.AccountUi
import com.edu.core.presentation.ui.model.BalanceUi
import com.edu.core.presentation.ui.model.CardSummaryUi
import com.edu.core.presentation.ui.model.TransactionUi
import com.edu.feature.dashboard.presentation.model.CashFlowUi
import com.edu.feature.dashboard.presentation.model.CategorySpendUi
import com.edu.feature.dashboard.presentation.model.DashboardAccountUi
import com.edu.feature.dashboard.presentation.model.DashboardContentUi
import com.edu.feature.dashboard.presentation.model.DashboardHeaderUi
import com.edu.feature.dashboard.presentation.model.GreetingPeriodUi
import com.edu.feature.dashboard.presentation.model.PendingUi
import com.edu.feature.dashboard.presentation.model.SafeToSpendUi
import com.edu.feature.dashboard.presentation.model.UpcomingPaymentTypeUi
import com.edu.feature.dashboard.presentation.model.UpcomingPaymentUi

/**
 * Placeholder content for @Preview and Compose UI tests ONLY — never used at
 * runtime. Values are deliberately generic, not the screenshot's sample data.
 */
object DashboardPreviewData {
    private fun account(id: String, name: String) =
        AccountUi(id, name, "GBP", null, "•••• 1234", "11-22-33", "Bank", null, "CURRENT", 0xFF2D6EF5.toInt())

    private fun tx(id: String, title: String, amount: String, debit: Boolean) = TransactionUi(
        transactionId = id, title = title, subtitle = "", formattedAmount = amount,
        amountColorArgb = if (debit) 0xFFE03B3B.toInt() else 0xFF00A043.toInt(), isDebit = debit,
        formattedDate = "", categoryIconId = "", categoryColorArgb = 0xFF2D6EF5.toInt(), formattedRunningBalance = null,
        isPending = false, initial = title.take(1), compactSubtitle = "Category · Today",
    )

    val content = DashboardContentUi(
        header = DashboardHeaderUi(GreetingPeriodUi.MORNING, "Alex", "A", "Monday, 1 January 2029"),
        balance = BalanceUi("a", "£100.00", "£90.00", "£50.00", true, "", "£50.00", "just now"),
        accounts = listOf(
            DashboardAccountUi(account("a", "Current account"), BalanceUi("a", "£100.00", "£90.00", null, false, "")),
            DashboardAccountUi(account("b", "Savings account"), BalanceUi("b", "£500.00", "£500.00", null, false, "")),
        ),
        safeToSpend = SafeToSpendUi("£80.00", "£40.00", "£60.00", 0.22f, 0.34f, 0.44f, daysToPayday = 9),
        cashFlow = CashFlowUi("£1,000.00", "£600.00", "Jan", "Dec", 10, isSpendingUp = false),
        pending = PendingUi(2, "£12.00"),
        upcomingPayments = listOf(
            UpcomingPaymentUi("1", "Energy supplier", "E", UpcomingPaymentTypeUi.DIRECT_DEBIT, "£30.00", 1, 0xFFFFB020.toInt()),
            UpcomingPaymentUi("2", "Landlord", "L", UpcomingPaymentTypeUi.STANDING_ORDER, "£700.00", 4, 0xFF0A1628.toInt()),
        ),
        categories = listOf(
            CategorySpendUi("Shopping", "£200.00", 1f, 0xFF2D6EF5.toInt()),
            CategorySpendUi("Transfers", "£150.00", 0.75f, 0xFF00D4AA.toInt()),
        ),
        card = CardSummaryUi(
            "c", "Credit card", "•••• 9999", "••••  ••••  ••••  9999", "VISA", "A N Other",
            "£900.00", "£100.00", "£1,000.00", "£120.00", "£25.00", "1 Feb 2029", "10%", 0.1f, 0xFF00C853.toInt(),
        ),
        recentTransactions = listOf(tx("t1", "Coffee shop", "-£3.20", true), tx("t2", "Refund", "+£10.00", false)),
    )
}
