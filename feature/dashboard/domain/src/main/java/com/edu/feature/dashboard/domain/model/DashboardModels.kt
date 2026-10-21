package com.edu.feature.dashboard.domain.model

import com.edu.core.domain.model.Account
import com.edu.core.domain.model.AccountWithBalance
import com.edu.core.domain.model.Balance
import com.edu.core.domain.model.CardSummary
import com.edu.core.domain.model.Transaction
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

enum class GreetingPeriod { MORNING, AFTERNOON, EVENING }

/** "Good morning, Waheed": period from device time, first word of `full_name`. */
data class Greeting(
    val period: GreetingPeriod,
    val firstName: String?,
)

/** Next expected date of the largest recurring CREDIT (salary). */
data class Payday(
    val nextDate: LocalDate,
    val daysUntil: Int,
    val expectedAmount: BigDecimal,
)

enum class UpcomingPaymentType { DIRECT_DEBIT, STANDING_ORDER }

data class UpcomingPayment(
    val id: String,
    val name: String,
    val type: UpcomingPaymentType,
    /** Positive amount that will leave the account. */
    val amount: BigDecimal,
    val dueDate: LocalDate,
    val daysUntil: Int,
)

/**
 * Safe to spend = Σ current-account available + overdraft − bills due before
 * payday, never below zero. The three shares drive the Bills due / Spent /
 * Yours bar and sum to 1 (or are all 0 when there is nothing to show).
 */
data class SafeToSpend(
    val amount: BigDecimal,
    val billsDue: BigDecimal,
    val spentThisMonth: BigDecimal,
    val billsShare: BigDecimal,
    val spentShare: BigDecimal,
    val yoursShare: BigDecimal,
)

data class MonthlyCashFlow(
    val month: YearMonth,
    val moneyIn: BigDecimal,
    /** Positive total of the month's debits. */
    val moneyOut: BigDecimal,
    val previousMonth: YearMonth,
    val previousMoneyOut: BigDecimal,
    /** Rounded % change of money out vs the previous month; null when last month had none. */
    val moneyOutChangePercent: Int?,
)

data class PendingSummary(
    val count: Int,
    /** Absolute value of the pending total (what is "not yet settled"). */
    val total: BigDecimal,
)

data class CategorySpend(
    val category: String,
    val amount: BigDecimal,
    /** amount ÷ largest category, for the mini bar. */
    val ratioToLargest: BigDecimal,
)

/** Everything Home (09) shows, already derived. */
data class Dashboard(
    val today: LocalDate,
    val greeting: Greeting,
    val accounts: List<AccountWithBalance>,
    val primaryAccount: Account?,
    val primaryBalance: Balance?,
    val safeToSpend: SafeToSpend?,
    val payday: Payday?,
    /** Null when the primary account's transactions could not be loaded. */
    val cashFlow: MonthlyCashFlow?,
    val pending: PendingSummary,
    val upcomingPayments: List<UpcomingPayment>,
    val spendingByCategory: List<CategorySpend>,
    val card: CardSummary?,
    val recentTransactions: List<Transaction>,
    /** Last sync time when (some of) this came from the offline cache. */
    val offlineSince: Instant?,
)
