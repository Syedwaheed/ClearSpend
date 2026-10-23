package com.edu.feature.dashboard.domain.usecase

import com.edu.core.domain.model.Transaction
import com.edu.core.domain.model.TransactionType
import com.edu.feature.dashboard.domain.model.Payday
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Clock
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Days to payday (DESIGN_SPEC §4 · 09): find CREDITs that recur (same payer,
 * at least twice, roughly weekly / fortnightly / monthly), take the group with
 * the largest average amount (the salary), and project its next date from
 * the last occurrence. Null when no recurring credit is found.
 */
class GetPaydayUseCase(
    private val clock: Clock,
) {

    operator fun invoke(transactions: List<Transaction>): Payday? {
        val today = LocalDate.now(clock)
        val zone = clock.zone
        return transactions
            .asSequence()
            .filter { !it.isPending && it.transactionType == TransactionType.CREDIT && it.amount.signum() > 0 }
            .groupBy { payerKey(it) }
            .values
            .mapNotNull { group ->
                val dates = group.map { it.timestamp.atZone(zone).toLocalDate() }.distinct().sorted()
                if (dates.size < 2) return@mapNotNull null
                val interval = intervalOf(dates) ?: return@mapNotNull null
                val average = group.fold(BigDecimal.ZERO) { sum, t -> sum + t.amount }
                    .divide(BigDecimal(group.size), 2, RoundingMode.HALF_UP)
                Candidate(average, dates.last(), interval)
            }
            .maxWithOrNull(compareBy<Candidate> { it.averageAmount }.thenBy { it.lastDate })
            ?.let { candidate ->
                var next = candidate.interval.next(candidate.lastDate)
                while (next.isBefore(today)) next = candidate.interval.next(next)
                Payday(
                    nextDate = next,
                    daysUntil = ChronoUnit.DAYS.between(today, next).toInt(),
                    expectedAmount = candidate.averageAmount,
                )
            }
    }

    private data class Candidate(val averageAmount: BigDecimal, val lastDate: LocalDate, val interval: Interval)

    private sealed interface Interval {
        fun next(from: LocalDate): LocalDate

        data object Monthly : Interval { override fun next(from: LocalDate): LocalDate = from.plusMonths(1) }
        data class Days(val days: Long) : Interval { override fun next(from: LocalDate): LocalDate = from.plusDays(days) }
    }

    /** Median gap decides the cadence; anything more frequent than weekly isn't a salary. */
    private fun intervalOf(dates: List<LocalDate>): Interval? {
        val gaps = dates.zipWithNext { a, b -> ChronoUnit.DAYS.between(a, b) }.sorted()
        val median = gaps[gaps.size / 2]
        return when (median) {
            in 25L..35L -> Interval.Monthly
            in 6L..8L -> Interval.Days(7)
            in 13L..15L -> Interval.Days(14)
            in 9L..60L -> Interval.Days(median)
            else -> null
        }
    }

    /** Same payer even when the reference digits change ("ACME LTD SAL 0925"). */
    private fun payerKey(transaction: Transaction): String =
        (transaction.merchantName ?: transaction.description)
            .lowercase()
            .replace(Regex("[0-9]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
}
