package com.edu.feature.transactions.presentation.mapper

import com.edu.core.domain.model.TransactionCategory
import com.edu.core.domain.model.TransactionType
import com.edu.core.presentation.ui.formatter.AmountFormatter
import com.edu.core.presentation.ui.formatter.DateFormatter
import com.edu.core.presentation.ui.mapper.SyncTimeUiMapper
import com.edu.core.presentation.ui.mapper.TransactionUiMapper
import com.edu.feature.transactions.domain.model.MerchantInsight
import com.edu.feature.transactions.domain.model.TransactionDetail
import com.edu.feature.transactions.presentation.model.MerchantInsightUi
import com.edu.feature.transactions.presentation.model.TransactionDetailUi

class TransactionDetailUiMapperImpl(
    private val transactionMapper: TransactionUiMapper,
    private val amountFormatter: AmountFormatter,
    private val dateFormatter: DateFormatter,
    private val syncTimeMapper: SyncTimeUiMapper,
) : TransactionDetailUiMapper {

    override fun map(domain: TransactionDetail): TransactionDetailUi {
        val transaction = domain.transaction
        val row = transactionMapper.map(transaction)
        return TransactionDetailUi(
            transactionId = transaction.transactionId,
            title = row.title,
            initial = row.initial,
            categoryColorArgb = row.categoryColorArgb,
            formattedAmount = row.formattedAmount,
            isCredit = transaction.transactionType == TransactionType.CREDIT,
            isPending = transaction.isPending,
            dateTime = "${dateFormatter.formatDate(transaction.timestamp)} · ${dateFormatter.formatTime(transaction.timestamp)}",
            classifications = transaction.classification.ifEmpty { listOf(row.categoryLabel) },
            accountLabel = domain.account?.let { "${it.displayName} · ${it.provider.displayName}" },
            paymentMethod = row.paymentMethod,
            reference = transaction.providerReference ?: transaction.description,
            // A pending row has no running balance yet (DESIGN_SPEC §4 · 12).
            formattedBalanceAfter = if (transaction.isPending) null else transaction.runningBalance?.let(amountFormatter::format),
            insight = insight(domain.insight),
            showManageInBills = transaction.transactionCategory == TransactionCategory.DIRECT_DEBIT,
            offlineSyncTime = domain.offlineSince?.let(syncTimeMapper::map),
        )
    }

    private fun insight(insight: MerchantInsight): MerchantInsightUi = when (insight) {
        is MerchantInsight.FirstTime -> MerchantInsightUi.FirstTime(insight.merchant, insight.lookbackDays)
        is MerchantInsight.MonthlyTotal -> MerchantInsightUi.MonthlyTotal(
            merchant = insight.merchant,
            formattedTotal = amountFormatter.format(insight.total),
            count = insight.count,
            isTopMerchant = insight.isTopMerchant,
        )
    }
}
