package com.edu.feature.dashboard.domain.usecase

import com.edu.core.domain.model.Transaction
import com.edu.core.domain.model.TransactionType
import com.edu.feature.dashboard.domain.model.CategorySpend
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Clock
import java.time.YearMonth

/**
 * "Where your money went" (DESIGN_SPEC §4 · 09): this month's settled DEBITs
 * grouped by `transaction_classification[0]`, summed, sorted descending.
 */
class GetSpendingByCategoryUseCase(
    private val clock: Clock,
) {

    operator fun invoke(transactions: List<Transaction>): List<CategorySpend> {
        val month = YearMonth.now(clock)
        val totals = transactions
            .filter { !it.isPending && it.transactionType == TransactionType.DEBIT }
            .filter { YearMonth.from(it.timestamp.atZone(clock.zone)) == month }
            .groupBy { it.classification.firstOrNull()?.trim()?.takeIf(String::isNotEmpty) ?: OTHER }
            .map { (category, items) -> category to items.fold(BigDecimal.ZERO) { sum, t -> sum + t.amount.abs() } }
            .filter { (_, amount) -> amount.signum() > 0 }
            .sortedByDescending { (_, amount) -> amount }
        val largest = totals.firstOrNull()?.second ?: return emptyList()
        return totals.map { (category, amount) ->
            CategorySpend(category, amount, amount.divide(largest, 4, RoundingMode.HALF_UP))
        }
    }

    private companion object {
        const val OTHER = "Other"
    }
}
