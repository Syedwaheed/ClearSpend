package com.edu.feature.dashboard.presentation.mapper

import com.edu.core.presentation.ui.formatter.AmountFormatter
import com.edu.core.presentation.ui.formatter.DateFormatter
import com.edu.feature.dashboard.domain.model.MonthlyCashFlow
import com.edu.feature.dashboard.presentation.model.CashFlowUi
import kotlin.math.absoluteValue

class CashFlowUiMapperImpl(
    private val amountFormatter: AmountFormatter,
    private val dateFormatter: DateFormatter,
) : CashFlowUiMapper {
    override fun map(domain: MonthlyCashFlow): CashFlowUi = CashFlowUi(
        formattedMoneyIn = amountFormatter.format(domain.moneyIn),
        formattedMoneyOut = amountFormatter.format(domain.moneyOut),
        monthLabel = dateFormatter.formatMonthShort(domain.month),
        previousMonthLabel = dateFormatter.formatMonthShort(domain.previousMonth),
        changePercent = domain.moneyOutChangePercent?.absoluteValue,
        isSpendingUp = (domain.moneyOutChangePercent ?: 0) > 0,
    )
}
