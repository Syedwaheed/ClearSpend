package com.edu.feature.dashboard.domain.usecase

import com.edu.core.domain.model.Transaction
import com.edu.core.domain.model.TransactionType
import com.edu.feature.dashboard.domain.model.MonthlyCashFlow
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Clock
import java.time.YearMonth

/**
 * Money in / out for the current calendar month (device time zone) and the
 * % change of money out vs last month (DESIGN_SPEC §4 · 09). Pending
 * transactions are excluded.
 */
class GetMonthlyCashFlowUseCase(
    private val clock: Clock,
) {

    operator fun invoke(transactions: List<Transaction>): MonthlyCashFlow {
        val month = YearMonth.now(clock)
        val previous = month.minusMonths(1)
        val settled = transactions.filter { !it.isPending }

        fun inMonth(t: Transaction, m: YearMonth) = YearMonth.from(t.timestamp.atZone(clock.zone)) == m
        fun sumOf(type: TransactionType, m: YearMonth) = settled
            .filter { it.transactionType == type && inMonth(it, m) }
            .fold(BigDecimal.ZERO) { sum, t -> sum + t.amount.abs() }

        val moneyOut = sumOf(TransactionType.DEBIT, month)
        val previousOut = sumOf(TransactionType.DEBIT, previous)
        val change = if (previousOut.signum() == 0) {
            null
        } else {
            (moneyOut - previousOut).multiply(BigDecimal(100))
                .divide(previousOut, 0, RoundingMode.HALF_UP)
                .toInt()
        }
        return MonthlyCashFlow(
            month = month,
            moneyIn = sumOf(TransactionType.CREDIT, month),
            moneyOut = moneyOut,
            previousMonth = previous,
            previousMoneyOut = previousOut,
            moneyOutChangePercent = change,
        )
    }
}
