package com.edu.core.presentation.ui.formatter

class IbanFormatterImpl : IbanFormatter {

    override fun mask(iban: String): String = maskKeepingLastFour(iban.replace(" ", ""))

    override fun formatSortCode(sortCode: String): String {
        val digits = sortCode.filter { it.isDigit() }
        return digits.chunked(2).joinToString("-")
    }

    override fun maskAccountNumber(number: String): String = maskKeepingLastFour(number)

    private fun maskKeepingLastFour(value: String): String {
        if (value.length <= 4) return value
        return "•••• ${value.takeLast(4)}"
    }
}
