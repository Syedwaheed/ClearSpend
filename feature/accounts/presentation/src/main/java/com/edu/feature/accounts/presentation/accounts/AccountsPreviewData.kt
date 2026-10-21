package com.edu.feature.accounts.presentation.accounts

import com.edu.core.presentation.ui.model.AccountUi
import com.edu.core.presentation.ui.model.CardSummaryUi
import com.edu.feature.accounts.presentation.model.AccountFilterUi
import com.edu.feature.accounts.presentation.model.AccountRowUi
import com.edu.feature.accounts.presentation.model.AccountsContentUi
import com.edu.feature.accounts.presentation.model.ConsentUi
import com.edu.feature.accounts.presentation.model.NetPositionUi
import com.edu.feature.accounts.presentation.model.ProviderGroupUi
import com.edu.feature.accounts.presentation.model.ThirdStatUi

/** @Preview / UI-test placeholder ONLY — generic values, never the screenshot sample data. */
object AccountsPreviewData {
    private fun row(id: String, name: String, badge: String, color: Long, icon: String, filter: AccountFilterUi, third: ThirdStatUi) = AccountRowUi(
        account = AccountUi(id, name, "GBP", null, "•••• 1234", "11-22-33", "Bank", null, badge, color.toInt(), icon),
        filter = filter,
        formattedAvailable = "£100.00",
        formattedCurrent = "£90.00",
        thirdStat = third,
        thirdStatValue = if (third == ThirdStatUi.OVERDRAFT) "£50.00" else "—",
        formattedThirtyDayChange = "+£10.00",
        isThirtyDayChangePositive = true,
    )

    val content = AccountsContentUi(
        netPosition = NetPositionUi("£1,000.00", 3, "+£20.00", true, listOf(0.6f, 0.7f, 0.65f, 0.8f, 0.85f, 0.8f, 0.9f, 1f), "£700.00", "£400.00", "£100.00"),
        consent = ConsentUi(42, "just now"),
        groups = listOf(
            ProviderGroupUi(
                "bank-a", "Bank A", null, "A", 0xFF2D6EF5.toInt(), "£200.00",
                listOf(
                    row("a1", "Current account", "CURRENT", 0xFF2D6EF5, "house", AccountFilterUi.CURRENT, ThirdStatUi.OVERDRAFT),
                    row("a2", "Savings account", "SAVINGS", 0xFF00A886, "star", AccountFilterUi.SAVINGS, ThirdStatUi.INTEREST_AER),
                ),
            ),
            ProviderGroupUi(
                "bank-b", "Bank B", null, "B", 0xFF00D4AA.toInt(), "£100.00",
                listOf(row("b1", "Business account", "BUSINESS", 0xFF7A4FD8, "briefcase", AccountFilterUi.BUSINESS, ThirdStatUi.OVERDRAFT)),
            ),
        ),
        cards = listOf(
            CardSummaryUi(
                "c", "Credit card", "•••• 9999", "••••  ••••  ••••  9999", "VISA", "A N Other",
                "£900.00", "£100.00", "£1,000.00", "£120.00", "£25.00", "1 Feb 2029", "10%", 0.1f, 0xFF00C853.toInt(),
            ),
        ),
        formattedCardsOwed = "£100.00",
    )
}
