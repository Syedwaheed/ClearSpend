package com.edu.core.presentation.ui.formatter

import java.math.BigDecimal

interface AmountFormatter {
    /** Plain currency format, e.g. "£1,234.56" or "-£46.82". */
    fun format(amount: BigDecimal): String

    /** Same as [format] but always carries an explicit sign, e.g. "+£420.00" / "-£46.82". */
    fun formatSigned(amount: BigDecimal): String
}
