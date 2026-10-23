package com.edu.feature.dashboard.presentation.mapper

import com.edu.core.presentation.ui.formatter.AmountFormatter
import com.edu.feature.dashboard.domain.model.PendingSummary
import com.edu.feature.dashboard.presentation.model.PendingUi

class PendingUiMapperImpl(
    private val amountFormatter: AmountFormatter,
) : PendingUiMapper {
    override fun map(domain: PendingSummary): PendingUi =
        PendingUi(count = domain.count, formattedTotal = amountFormatter.format(domain.total))
}
