package com.edu.feature.transactions.domain.usecase

import com.edu.core.domain.model.Transaction
import com.edu.core.domain.model.TransactionType
import com.edu.feature.transactions.domain.model.MerchantInsight
import java.math.BigDecimal
import java.time.Clock
import java.time.Duration
import java.time.YearMonth

/**
 * Merchant insight (DESIGN_SPEC §4 · 14-16): Σ + count of payments to the same
 * merchant (`merchant_name ?: description`, case-insensitive) in the
 * transaction's calendar month.
 *
 * - "First time" when there is no other payment to the merchant in the
 *   [LOOKBACK_DAYS] days before this one, nor later in the same month.
 * - "Top merchant" when the merchant is among the month's [TOP_MERCHANTS]
 *   biggest DEBIT merchants by Σ.
 *
 * [history] is the account's settled transactions; pending rows in it are
 * ignored (they may still change), but [transaction] itself always counts.
 */
class GetMerchantInsightUseCase(
    private val clock: Clock,
) {

    operator fun invoke(transaction: Transaction, history: List<Transaction>): MerchantInsight {
        val merchant = transaction.merchantLabel()
        val key = merchant.merchantKey()
        val month = YearMonth.from(transaction.timestamp.atZone(clock.zone))
        val pool = (history.filter { !it.isPending } + transaction).distinctBy { it.transactionId }
        val sameMerchant = pool.filter { it.merchantLabel().merchantKey() == key }
        val inMonth = sameMerchant.filter { it.month() == month }

        val lookbackStart = transaction.timestamp.minus(Duration.ofDays(LOOKBACK_DAYS.toLong()))
        val earlier = sameMerchant.filter {
            it.transactionId != transaction.transactionId && it.timestamp >= lookbackStart && it.timestamp <= transaction.timestamp
        }
        if (earlier.isEmpty() && inMonth.size <= 1) {
            return MerchantInsight.FirstTime(merchant = merchant, lookbackDays = LOOKBACK_DAYS)
        }

        val topKeys = pool
            .filter { it.transactionType == TransactionType.DEBIT && it.month() == month }
            .groupBy { it.merchantLabel().merchantKey() }
            .mapValues { (_, items) -> items.sumAbs() }
            .entries
            .sortedByDescending { it.value }
            .take(TOP_MERCHANTS)
            .map { it.key }

        return MerchantInsight.MonthlyTotal(
            merchant = merchant,
            month = month,
            total = inMonth.sumAbs(),
            count = inMonth.size,
            isTopMerchant = transaction.transactionType == TransactionType.DEBIT && key in topKeys,
        )
    }

    private fun Transaction.month(): YearMonth = YearMonth.from(timestamp.atZone(clock.zone))

    private fun Transaction.merchantLabel(): String = merchantName?.takeIf { it.isNotBlank() }?.trim() ?: description.trim()

    private fun String.merchantKey(): String = lowercase()

    private fun List<Transaction>.sumAbs(): BigDecimal = fold(BigDecimal.ZERO) { sum, t -> sum + t.amount.abs() }

    companion object {
        const val LOOKBACK_DAYS = 90
        const val TOP_MERCHANTS = 5
    }
}
