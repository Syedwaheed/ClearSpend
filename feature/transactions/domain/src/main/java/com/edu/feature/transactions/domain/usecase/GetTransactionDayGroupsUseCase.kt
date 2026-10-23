package com.edu.feature.transactions.domain.usecase

import com.edu.core.domain.model.Transaction
import com.edu.feature.transactions.domain.model.TransactionDayGroup
import java.math.BigDecimal
import java.time.Clock

/**
 * Groups transactions by the local calendar date of their `timestamp`
 * (DESIGN_SPEC §4 · 12). Each group's net is Σ of the signed amounts (debits
 * are negative in the API). Newest day first, newest transaction first.
 */
class GetTransactionDayGroupsUseCase(
    private val clock: Clock,
) {

    operator fun invoke(transactions: List<Transaction>): List<TransactionDayGroup> =
        transactions
            .sortedByDescending { it.timestamp }
            .groupBy { it.timestamp.atZone(clock.zone).toLocalDate() }
            .map { (date, items) ->
                TransactionDayGroup(
                    date = date,
                    net = items.fold(BigDecimal.ZERO) { sum, t -> sum + t.amount },
                    transactions = items,
                )
            }
            .sortedByDescending { it.date }
}
