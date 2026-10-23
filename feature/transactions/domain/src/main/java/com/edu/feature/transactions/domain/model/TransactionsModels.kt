package com.edu.feature.transactions.domain.model

import com.edu.core.domain.model.Account
import com.edu.core.domain.model.Transaction
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

/** Period chips on Transactions (12): set the `from`/`to` of the transactions query. */
sealed interface TransactionPeriod {
    data object All : TransactionPeriod
    data object ThisMonth : TransactionPeriod
    data object LastMonth : TransactionPeriod

    /** Material 3 DateRangePicker selection, both ends inclusive. */
    data class Custom(val from: LocalDate, val to: LocalDate) : TransactionPeriod
}

/**
 * Category chips (DESIGN_SPEC §4 · 12): one per `transaction_category`.
 * [CASH] also matches `ATM`.
 */
enum class CategoryFilter { ALL, PURCHASE, DIRECT_DEBIT, BILL_PAYMENT, TRANSFER, CASH, CREDIT }

/** Inclusive calendar-date window, as sent in `?from=&to=`. */
data class DateWindow(val from: LocalDate, val to: LocalDate)

/** The outer limits of a period. Pages are loaded inside it, newest first. */
data class PeriodBounds(val earliest: LocalDate, val latest: LocalDate) {
    operator fun contains(date: LocalDate): Boolean = date in earliest..latest
}

/** One page window plus whether an older page exists inside the period. */
data class PageWindow(val window: DateWindow, val hasMore: Boolean)

/** One fetch of the Transactions screen: settled rows of one window + the pending snapshot. */
data class TransactionsPage(
    /** Every connected account (feeds the account switcher). */
    val accounts: List<Account>,
    /** Settled rows of the requested account(s), merged and sorted newest first. */
    val settled: List<Transaction>,
    /** Pending rows; empty when they were not requested. */
    val pending: List<Transaction>,
    val offlineSince: Instant?,
)

/** A date-group header (DESIGN_SPEC §4 · 12): the day and Σ of its signed amounts. */
data class TransactionDayGroup(
    val date: LocalDate,
    val net: BigDecimal,
    /** Newest first. */
    val transactions: List<Transaction>,
)

/** Σ DEBIT of one day (positive), for the daily spending bars. */
data class DailySpend(val date: LocalDate, val total: BigDecimal)

/** Summary card In / Out / Net, derived from the filtered settled list. */
data class TransactionsSummary(
    /** Σ CREDIT, positive. */
    val moneyIn: BigDecimal,
    /** Σ DEBIT, positive. */
    val moneyOut: BigDecimal,
    /** [moneyIn] − [moneyOut]. */
    val net: BigDecimal,
    val count: Int,
    /** Oldest first, at most [com.edu.feature.transactions.domain.usecase.GetTransactionsSummaryUseCase.MAX_BARS]. */
    val dailySpending: List<DailySpend>,
)

/** Everything the Transactions list (12/13) shows after period, search and category filtering. */
data class TransactionsOverview(
    val period: TransactionPeriod,
    val bounds: PeriodBounds,
    val summary: TransactionsSummary,
    /** Pending rows that pass the filters; empty when the period does not include today. */
    val pending: List<Transaction>,
    /** Σ signed amounts of [pending]. */
    val pendingTotal: BigDecimal,
    /** Newest day first. */
    val groups: List<TransactionDayGroup>,
    val offlineSince: Instant?,
) {
    val isEmpty: Boolean get() = pending.isEmpty() && groups.isEmpty()
}

/** Merchant insight on Transaction Detail (14-16). */
sealed interface MerchantInsight {
    /** No other payment to this merchant in the [lookbackDays] before this one. */
    data class FirstTime(val merchant: String, val lookbackDays: Int) : MerchantInsight

    /** Σ and count of payments to this merchant in [month] (the transaction's month). */
    data class MonthlyTotal(
        val merchant: String,
        val month: YearMonth,
        /** Σ of absolute amounts, positive. */
        val total: BigDecimal,
        val count: Int,
        /** Among the month's top merchants by spend. */
        val isTopMerchant: Boolean,
    ) : MerchantInsight
}

/** Transaction Detail (14-16). */
data class TransactionDetail(
    val transaction: Transaction,
    /** The account it was fetched from; null when it could not be loaded. */
    val account: Account?,
    val insight: MerchantInsight,
    val offlineSince: Instant?,
)
