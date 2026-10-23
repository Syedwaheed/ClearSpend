package com.edu.feature.transactions.domain.usecase

import com.edu.core.domain.model.Transaction
import com.edu.core.domain.model.TransactionType
import com.edu.feature.transactions.domain.model.DailySpend
import com.edu.feature.transactions.domain.model.TransactionDayGroup
import com.edu.feature.transactions.domain.model.TransactionsSummary
import java.math.BigDecimal

/**
 * Summary card (DESIGN_SPEC §4 · 12): Money in = Σ CREDIT, Money out = Σ DEBIT
 * (both positive), Net = in − out, over the filtered settled list; plus the
 * daily spending bars = Σ DEBIT per day for the newest [MAX_BARS] days that
 * have transactions, oldest first.
 */
class GetTransactionsSummaryUseCase {

    operator fun invoke(groups: List<TransactionDayGroup>): TransactionsSummary {
        val transactions = groups.flatMap { it.transactions }
        val moneyIn = transactions.filter { it.transactionType == TransactionType.CREDIT }.sumAbs()
        val moneyOut = transactions.filter { it.transactionType == TransactionType.DEBIT }.sumAbs()
        return TransactionsSummary(
            moneyIn = moneyIn,
            moneyOut = moneyOut,
            net = moneyIn - moneyOut,
            count = transactions.size,
            dailySpending = groups
                .sortedByDescending { it.date }
                .take(MAX_BARS)
                .reversed()
                .map { group ->
                    DailySpend(
                        date = group.date,
                        total = group.transactions.filter { it.transactionType == TransactionType.DEBIT }.sumAbs(),
                    )
                },
        )
    }

    private fun List<Transaction>.sumAbs(): BigDecimal =
        fold(BigDecimal.ZERO) { sum, t -> sum + t.amount.abs() }

    companion object {
        const val MAX_BARS = 12
    }
}
