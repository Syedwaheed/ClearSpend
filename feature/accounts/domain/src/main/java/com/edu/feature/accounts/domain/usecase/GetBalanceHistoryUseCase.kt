package com.edu.feature.accounts.domain.usecase

import com.edu.core.domain.model.AccountWithBalance
import com.edu.core.domain.model.Transaction
import com.edu.feature.accounts.domain.model.BalanceHistory
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

/**
 * 8-week sparkline, "+£X this month" and each account's 30-day change
 * (DESIGN_SPEC §4 · 10), reconstructed from transaction `running_balance`.
 *
 * Balance at time T for one account:
 *  1. the `running_balance` of the latest settled transaction at or before T;
 *  2. else, from the first transaction after T: its `running_balance` − its amount;
 *  3. else (running_balance is optional in the API): today's `current` minus
 *     every transaction after T.
 * Cards have no history, so the card debt is held constant across the weeks.
 */
class GetBalanceHistoryUseCase(
    private val clock: Clock,
) {

    operator fun invoke(
        accounts: List<AccountWithBalance>,
        transactionsByAccount: Map<String, List<Transaction>>,
        cardOwed: BigDecimal,
    ): BalanceHistory {
        val now = clock.instant()
        val today = LocalDate.now(clock)
        val zone = clock.zone
        fun endOfDay(date: LocalDate): Instant = date.plusDays(1).atStartOfDay(zone).toInstant().minusMillis(1)

        val weekEnds = (WEEKS - 1 downTo 1).map { endOfDay(today.minusWeeks(it.toLong())) } + now
        val endOfLastMonth = endOfDay(YearMonth.from(today).minusMonths(1).atEndOfMonth())
        val thirtyDaysAgo = now.minusSeconds(THIRTY_DAYS_SECONDS)

        fun netAt(time: Instant): BigDecimal =
            accounts.fold(BigDecimal.ZERO) { sum, item -> sum + balanceAt(item, transactionsByAccount[item.account.accountId], time) } - cardOwed

        val thirtyDayChanges = accounts.mapNotNull { item ->
            val history = transactionsByAccount[item.account.accountId] ?: return@mapNotNull null
            if (item.balance == null && history.none { it.runningBalance != null }) return@mapNotNull null
            item.account.accountId to (balanceAt(item, history, now) - balanceAt(item, history, thirtyDaysAgo))
        }.toMap()

        return BalanceHistory(
            weeklyNetBalances = weekEnds.map(::netAt),
            changeThisMonth = netAt(now) - netAt(endOfLastMonth),
            thirtyDayChangeByAccount = thirtyDayChanges,
        )
    }

    private fun balanceAt(item: AccountWithBalance, transactions: List<Transaction>?, time: Instant): BigDecimal {
        val current = item.balance?.current ?: BigDecimal.ZERO
        val settled = transactions.orEmpty().filter { !it.isPending }.sortedBy { it.timestamp }
        if (settled.isEmpty()) return current

        val latestBefore = settled.lastOrNull { !it.timestamp.isAfter(time) }
        latestBefore?.runningBalance?.let { return it }

        val after = settled.filter { it.timestamp.isAfter(time) }
        after.firstOrNull()?.runningBalance?.let { return it - after.first().amount }

        return after.fold(current) { balance, t -> balance - t.amount }
    }

    companion object {
        const val WEEKS = 8
        /** Days of history the overview fetches: covers 8 weeks, 30 days and the month start. */
        const val HISTORY_DAYS = 56L
        private const val THIRTY_DAYS_SECONDS = 30L * 24 * 60 * 60
    }
}
