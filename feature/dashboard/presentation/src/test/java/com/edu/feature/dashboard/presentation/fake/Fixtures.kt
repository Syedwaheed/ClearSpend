package com.edu.feature.dashboard.presentation.fake

import com.edu.core.domain.model.Account
import com.edu.core.domain.model.AccountNumber
import com.edu.core.domain.model.AccountType
import com.edu.core.domain.model.AccountWithBalance
import com.edu.core.domain.model.Balance
import com.edu.core.domain.model.Provider
import com.edu.core.presentation.ui.formatter.AmountFormatterImpl
import com.edu.core.presentation.ui.formatter.DateFormatterImpl
import com.edu.feature.dashboard.domain.model.Dashboard
import com.edu.feature.dashboard.domain.model.Greeting
import com.edu.feature.dashboard.domain.model.GreetingPeriod
import com.edu.feature.dashboard.domain.model.PendingSummary
import com.edu.feature.dashboard.presentation.mapper.DashboardContentUiMapper
import com.edu.feature.dashboard.presentation.model.DashboardContentUi
import com.edu.feature.dashboard.presentation.model.DashboardHeaderUi
import com.edu.feature.dashboard.presentation.model.GreetingPeriodUi
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Locale

val NOW: Instant = Instant.parse("2026-09-16T09:30:00Z")
val CLOCK: Clock = Clock.fixed(NOW, ZoneOffset.UTC)
val amountFormatter = AmountFormatterImpl(Locale.UK)
val dateFormatter = DateFormatterImpl(Locale.UK, CLOCK)

fun account(id: String) = Account(id, AccountType.TRANSACTION, id, provider = Provider("p", "Bank"), accountNumber = AccountNumber())

fun dashboard(accountIds: List<String> = listOf("a")) = Dashboard(
    today = LocalDate.of(2026, 9, 16),
    greeting = Greeting(GreetingPeriod.MORNING, "Waheed"),
    accounts = accountIds.map { AccountWithBalance(account(it), Balance(it, BigDecimal.ONE, BigDecimal.ONE, updateTimestamp = NOW)) },
    primaryAccount = accountIds.firstOrNull()?.let(::account),
    primaryBalance = null,
    safeToSpend = null,
    payday = null,
    cashFlow = null,
    pending = PendingSummary(0, BigDecimal.ZERO),
    upcomingPayments = emptyList(),
    spendingByCategory = emptyList(),
    card = null,
    recentTransactions = emptyList(),
    offlineSince = null,
)

/** Deterministic: no formatting, just echoes how many accounts the domain had. */
class FakeDashboardContentUiMapper : DashboardContentUiMapper {
    override fun map(domain: Dashboard) = fakeContent(domain.accounts.size)
}

fun fakeContent(accountCount: Int = 1) = DashboardContentUi(
    header = DashboardHeaderUi(GreetingPeriodUi.MORNING, "Waheed", "W", "date"),
    accounts = List(accountCount) { com.edu.feature.dashboard.presentation.home.DashboardPreviewData.content.accounts.first() },
)
