package com.edu.feature.dashboard.presentation.mapper

import com.edu.core.presentation.ui.formatter.AmountFormatter
import com.edu.feature.dashboard.domain.model.SafeToSpend
import com.edu.feature.dashboard.presentation.model.SafeToSpendUi

class SafeToSpendUiMapperImpl(
    private val amountFormatter: AmountFormatter,
) : SafeToSpendUiMapper {
    override fun map(domain: SafeToSpend): SafeToSpendUi = SafeToSpendUi(
        formattedAmount = amountFormatter.format(domain.amount),
        formattedBillsDue = amountFormatter.format(domain.billsDue),
        formattedSpent = amountFormatter.format(domain.spentThisMonth),
        billsFraction = domain.billsShare.toFloat(),
        spentFraction = domain.spentShare.toFloat(),
        yoursFraction = domain.yoursShare.toFloat(),
    )
}
