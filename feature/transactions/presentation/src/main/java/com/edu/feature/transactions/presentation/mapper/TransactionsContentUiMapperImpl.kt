package com.edu.feature.transactions.presentation.mapper

import com.edu.core.presentation.ui.formatter.AmountFormatter
import com.edu.core.presentation.ui.formatter.DateFormatter
import com.edu.core.presentation.ui.mapper.SyncTimeUiMapper
import com.edu.core.presentation.ui.mapper.TransactionUiMapper
import com.edu.core.presentation.ui.mapper.mapList
import com.edu.feature.transactions.domain.model.DailySpend
import com.edu.feature.transactions.domain.model.TransactionDayGroup
import com.edu.feature.transactions.domain.model.TransactionPeriod
import com.edu.feature.transactions.domain.model.TransactionsOverview
import com.edu.feature.transactions.presentation.model.DailyBarUi
import com.edu.feature.transactions.presentation.model.TransactionDayGroupUi
import com.edu.feature.transactions.presentation.model.TransactionsContentUi
import com.edu.feature.transactions.presentation.model.TransactionsSummaryUi
import java.math.RoundingMode
import java.time.YearMonth
import java.util.Locale

class TransactionsContentUiMapperImpl(
    private val transactionMapper: TransactionUiMapper,
    private val amountFormatter: AmountFormatter,
    private val dateFormatter: DateFormatter,
    private val syncTimeMapper: SyncTimeUiMapper,
    private val locale: Locale,
) : TransactionsContentUiMapper {

    override fun map(domain: TransactionsOverview): TransactionsContentUi = TransactionsContentUi(
        summary = TransactionsSummaryUi(
            isAllTime = domain.period == TransactionPeriod.All,
            periodLabel = periodLabel(domain),
            transactionCount = domain.summary.count,
            formattedMoneyIn = amountFormatter.format(domain.summary.moneyIn),
            formattedMoneyOut = amountFormatter.format(domain.summary.moneyOut),
            formattedNet = amountFormatter.formatSigned(domain.summary.net),
            dailyBars = bars(domain.summary.dailySpending),
        ),
        pending = transactionMapper.mapList(domain.pending),
        formattedPendingTotal = amountFormatter.formatSigned(domain.pendingTotal),
        groups = domain.groups.map(::group),
        offlineSyncTime = domain.offlineSince?.let(syncTimeMapper::map),
    )

    private fun periodLabel(domain: TransactionsOverview): String = when (domain.period) {
        TransactionPeriod.All -> ""
        TransactionPeriod.ThisMonth -> dateFormatter.formatMonthYear(YearMonth.from(domain.bounds.latest))
        TransactionPeriod.LastMonth -> dateFormatter.formatMonthYear(YearMonth.from(domain.bounds.earliest))
        is TransactionPeriod.Custom -> dateFormatter.formatDateRange(domain.bounds.earliest, domain.bounds.latest)
    }

    private fun group(group: TransactionDayGroup) = TransactionDayGroupUi(
        key = group.date.toString(),
        // The design shows the header in capitals ("TODAY — 1 JUNE 2026").
        label = dateFormatter.formatDayHeader(group.date).uppercase(locale),
        formattedNet = amountFormatter.formatSigned(group.net),
        isNetPositive = group.net.signum() >= 0,
        transactions = transactionMapper.mapList(group.transactions),
    )

    private fun bars(days: List<DailySpend>): List<DailyBarUi> {
        val max = days.maxOfOrNull { it.total } ?: return emptyList()
        return days.mapIndexed { index, day ->
            DailyBarUi(
                heightFraction = if (max.signum() == 0) 0f else day.total.divide(max, 4, RoundingMode.HALF_UP).toFloat(),
                isLatest = index == days.lastIndex,
            )
        }
    }
}
