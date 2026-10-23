package com.edu.feature.transactions.domain.usecase

import com.edu.core.domain.model.Transaction
import com.edu.feature.transactions.domain.model.CategoryFilter
import com.edu.feature.transactions.domain.model.TransactionPeriod
import com.edu.feature.transactions.domain.model.TransactionsOverview
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.LocalDate

/**
 * Turns the loaded rows into what the Transactions list shows: keeps the
 * settled rows inside the period, applies search + category, groups by day,
 * and derives the summary. Pending rows are shown only when the period
 * includes today (they have not happened on any earlier day).
 */
class GetTransactionsOverviewUseCase(
    private val getPeriodBounds: GetPeriodBoundsUseCase,
    private val getFilteredTransactions: GetFilteredTransactionsUseCase,
    private val getDayGroups: GetTransactionDayGroupsUseCase,
    private val getSummary: GetTransactionsSummaryUseCase,
    private val clock: Clock,
) {

    operator fun invoke(
        settled: List<Transaction>,
        pending: List<Transaction>,
        period: TransactionPeriod,
        query: String,
        category: CategoryFilter,
        offlineSince: Instant?,
    ): TransactionsOverview {
        val bounds = getPeriodBounds(period)
        val inPeriod = settled.filter { it.timestamp.atZone(clock.zone).toLocalDate() in bounds }
        val groups = getDayGroups(getFilteredTransactions(inPeriod, query, category))
        val visiblePending = if (LocalDate.now(clock) in bounds) {
            getFilteredTransactions(pending, query, category).sortedByDescending { it.timestamp }
        } else {
            emptyList()
        }
        return TransactionsOverview(
            period = period,
            bounds = bounds,
            summary = getSummary(groups),
            pending = visiblePending,
            pendingTotal = visiblePending.fold(BigDecimal.ZERO) { sum, t -> sum + t.amount },
            groups = groups,
            offlineSince = offlineSince,
        )
    }
}
