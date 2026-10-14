package com.edu.core.presentation.ui.formatter

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

class AmountFormatterImpl(
    private val locale: Locale,
) : AmountFormatter {

    private val currencyFormat: NumberFormat
        get() = NumberFormat.getCurrencyInstance(locale).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }

    override fun format(amount: BigDecimal): String = currencyFormat.format(amount)

    override fun formatSigned(amount: BigDecimal): String {
        val formatted = currencyFormat.format(amount)
        return if (amount.signum() >= 0) "+$formatted" else formatted
    }
}
