package com.edu.feature.dashboard.presentation.mapper

import androidx.compose.ui.graphics.toArgb
import com.edu.core.presentation.designsystem.Blue
import com.edu.core.presentation.designsystem.CategoryBillPayment
import com.edu.core.presentation.designsystem.CategoryTransfer
import com.edu.core.presentation.designsystem.Mint
import com.edu.core.presentation.designsystem.Navy
import com.edu.core.presentation.designsystem.Warning
import com.edu.core.presentation.ui.formatter.AmountFormatter
import com.edu.feature.dashboard.domain.model.UpcomingPayment
import com.edu.feature.dashboard.domain.model.UpcomingPaymentType
import com.edu.feature.dashboard.presentation.model.UpcomingPaymentTypeUi
import com.edu.feature.dashboard.presentation.model.UpcomingPaymentUi
import kotlin.math.absoluteValue

class UpcomingPaymentUiMapperImpl(
    private val amountFormatter: AmountFormatter,
) : UpcomingPaymentUiMapper {

    override fun map(domain: UpcomingPayment): UpcomingPaymentUi = UpcomingPaymentUi(
        id = domain.id,
        name = domain.name,
        initial = domain.name.firstOrNull { it.isLetterOrDigit() }?.uppercase() ?: "•",
        type = when (domain.type) {
            UpcomingPaymentType.DIRECT_DEBIT -> UpcomingPaymentTypeUi.DIRECT_DEBIT
            UpcomingPaymentType.STANDING_ORDER -> UpcomingPaymentTypeUi.STANDING_ORDER
        },
        formattedAmount = amountFormatter.format(domain.amount),
        daysUntil = domain.daysUntil,
        // Payee badge colour: stable per payee name (the API has no brand colour).
        colorArgb = PALETTE[domain.name.lowercase().hashCode().absoluteValue % PALETTE.size],
    )

    private companion object {
        val PALETTE = listOf(Warning, Navy, Blue, Mint, CategoryTransfer, CategoryBillPayment).map { it.toArgb() }
    }
}
