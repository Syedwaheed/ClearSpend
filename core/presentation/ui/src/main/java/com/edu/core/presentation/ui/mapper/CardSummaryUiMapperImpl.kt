package com.edu.core.presentation.ui.mapper

import androidx.compose.ui.graphics.toArgb
import com.edu.core.domain.model.CardSummary
import com.edu.core.domain.model.UtilisationBand
import com.edu.core.presentation.designsystem.Success
import com.edu.core.presentation.designsystem.Warning
import com.edu.core.presentation.designsystem.Error
import com.edu.core.presentation.ui.formatter.AmountFormatter
import com.edu.core.presentation.ui.formatter.DateFormatter
import com.edu.core.presentation.ui.model.CardSummaryUi
import java.math.BigDecimal

class CardSummaryUiMapperImpl(
    private val amountFormatter: AmountFormatter,
    private val dateFormatter: DateFormatter,
) : CardSummaryUiMapper {

    override fun map(domain: CardSummary): CardSummaryUi {
        val last4 = domain.card.partialCardNumber.filter { it.isDigit() }.takeLast(4)
        val balance = domain.balance
        val utilisation = domain.utilisation
        val paymentDue = balance?.paymentDue?.takeIf { it > BigDecimal.ZERO }
        return CardSummaryUi(
            cardId = domain.card.cardId,
            displayName = domain.card.displayName,
            maskedNumber = "•••• $last4",
            cardFaceNumber = "••••  ••••  ••••  $last4",
            network = domain.card.cardNetwork.uppercase(),
            nameOnCard = domain.card.nameOnCard,
            formattedAvailable = balance?.available?.let(amountFormatter::format),
            formattedCurrent = balance?.current?.let(amountFormatter::format),
            formattedCreditLimit = balance?.creditLimit?.let(amountFormatter::format),
            formattedLastStatementBalance = balance?.lastStatementBalance?.let(amountFormatter::format),
            formattedPaymentDue = paymentDue?.let(amountFormatter::format),
            formattedPaymentDueDate = balance?.paymentDueDate?.takeIf { paymentDue != null }?.let(dateFormatter::formatDate),
            utilisationLabel = utilisation?.let { "${it.percent}%" },
            utilisationFraction = utilisation?.ratio?.toFloat() ?: 0f,
            utilisationColorArgb = when (utilisation?.band) {
                UtilisationBand.LOW, null -> Success
                UtilisationBand.MEDIUM -> Warning
                UtilisationBand.HIGH -> Error
            }.toArgb(),
        )
    }
}
