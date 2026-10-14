package com.edu.core.presentation.ui.formatter

interface IbanFormatter {
    /** Masks an IBAN, keeping only the last 4 characters visible, e.g. "•••• 4567". */
    fun mask(iban: String): String

    /** Formats a 6-digit sort code as "12-34-56". */
    fun formatSortCode(sortCode: String): String

    /** Masks an account number, keeping only the last 4 digits visible, e.g. "•••• 1234". */
    fun maskAccountNumber(number: String): String
}
