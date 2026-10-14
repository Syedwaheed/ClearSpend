package com.edu.core.presentation.ui.mapper

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.edu.core.domain.model.Transaction
import com.edu.core.domain.model.TransactionCategory
import com.edu.core.domain.model.TransactionType
import com.edu.core.presentation.designsystem.CategoryBillPayment
import com.edu.core.presentation.designsystem.CategoryCashAtm
import com.edu.core.presentation.designsystem.CategoryCredit
import com.edu.core.presentation.designsystem.CategoryDirectDebit
import com.edu.core.presentation.designsystem.CategoryOther
import com.edu.core.presentation.designsystem.CategoryPurchase
import com.edu.core.presentation.designsystem.CategoryTransfer
import com.edu.core.presentation.designsystem.ErrorOnWhite
import com.edu.core.presentation.designsystem.SuccessOnWhite
import com.edu.core.presentation.ui.formatter.AmountFormatter
import com.edu.core.presentation.ui.formatter.DateFormatter
import com.edu.core.presentation.ui.model.TransactionUi

class TransactionUiMapperImpl(
    private val amountFormatter: AmountFormatter,
    private val dateFormatter: DateFormatter,
) : TransactionUiMapper {

    override fun map(domain: Transaction): TransactionUi {
        val method = paymentMethodFor(domain.transactionCategory)
        val classificationLabel = domain.classification.lastOrNull()
        val time = dateFormatter.formatTime(domain.timestamp)
        val subtitle = listOfNotNull(classificationLabel, time, method.short).joinToString(" · ")
        val isDebit = domain.transactionType == TransactionType.DEBIT

        return TransactionUi(
            transactionId = domain.transactionId,
            title = domain.merchantName ?: domain.description,
            subtitle = subtitle,
            formattedAmount = amountFormatter.formatSigned(domain.amount),
            amountColorArgb = (if (isDebit) ErrorOnWhite else SuccessOnWhite).toArgb(),
            isDebit = isDebit,
            formattedDate = dateFormatter.formatDate(domain.timestamp),
            categoryIconId = iconIdFor(domain.transactionCategory),
            categoryColorArgb = colorFor(domain.transactionCategory).toArgb(),
            formattedRunningBalance = domain.runningBalance?.let(amountFormatter::format),
            isPending = domain.isPending,
        )
    }

    private data class PaymentMethod(val label: String, val short: String)

    private fun paymentMethodFor(category: TransactionCategory): PaymentMethod = when (category) {
        TransactionCategory.PURCHASE -> PaymentMethod("Debit card", "Card")
        TransactionCategory.TRANSFER -> PaymentMethod("Faster Payment", "Transfer")
        TransactionCategory.DIRECT_DEBIT -> PaymentMethod("Direct Debit", "DD")
        TransactionCategory.BILL_PAYMENT -> PaymentMethod("Bill payment", "Bill")
        TransactionCategory.CASH, TransactionCategory.ATM -> PaymentMethod("Cash / ATM withdrawal", "Cash / ATM")
        TransactionCategory.CREDIT -> PaymentMethod("Bank credit (BACS)", "BACS")
        TransactionCategory.OTHER -> PaymentMethod("Bank transaction", "Bank")
    }

    private fun colorFor(category: TransactionCategory): Color = when (category) {
        TransactionCategory.PURCHASE -> CategoryPurchase
        TransactionCategory.TRANSFER -> CategoryTransfer
        TransactionCategory.DIRECT_DEBIT -> CategoryDirectDebit
        TransactionCategory.BILL_PAYMENT -> CategoryBillPayment
        TransactionCategory.CASH, TransactionCategory.ATM -> CategoryCashAtm
        TransactionCategory.CREDIT -> CategoryCredit
        TransactionCategory.OTHER -> CategoryOther
    }

    private fun iconIdFor(category: TransactionCategory): String = when (category) {
        TransactionCategory.PURCHASE -> "shopping_cart"
        TransactionCategory.TRANSFER -> "swap_horiz"
        TransactionCategory.DIRECT_DEBIT -> "repeat"
        TransactionCategory.BILL_PAYMENT -> "receipt"
        TransactionCategory.CASH, TransactionCategory.ATM -> "local_atm"
        TransactionCategory.CREDIT -> "arrow_downward"
        TransactionCategory.OTHER -> "help_outline"
    }
}
