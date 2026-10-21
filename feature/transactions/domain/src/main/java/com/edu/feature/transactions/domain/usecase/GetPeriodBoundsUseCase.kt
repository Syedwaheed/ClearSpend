package com.edu.feature.transactions.domain.usecase

import com.edu.feature.transactions.domain.model.PeriodBounds
import com.edu.feature.transactions.domain.model.TransactionPeriod
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth

/**
 * Period chip → the `from`/`to` limits of the query (DESIGN_SPEC §4 · 12).
 * Nothing ever extends past today. "All" reaches back [MAX_HISTORY_MONTHS]
 * months, the most history TrueLayer providers serve.
 */
class GetPeriodBoundsUseCase(
    private val clock: Clock,
) {

    operator fun invoke(period: TransactionPeriod): PeriodBounds {
        val today = LocalDate.now(clock)
        return when (period) {
            TransactionPeriod.All -> PeriodBounds(today.minusMonths(MAX_HISTORY_MONTHS), today)
            TransactionPeriod.ThisMonth -> PeriodBounds(today.withDayOfMonth(1), today)
            TransactionPeriod.LastMonth -> {
                val lastMonth = YearMonth.from(today).minusMonths(1)
                PeriodBounds(lastMonth.atDay(1), lastMonth.atEndOfMonth())
            }
            is TransactionPeriod.Custom -> {
                val start = minOf(period.from, period.to)
                val end = maxOf(period.from, period.to)
                val latest = minOf(end, today)
                PeriodBounds(minOf(start, latest), latest)
            }
        }
    }

    companion object {
        const val MAX_HISTORY_MONTHS = 24L
    }
}
