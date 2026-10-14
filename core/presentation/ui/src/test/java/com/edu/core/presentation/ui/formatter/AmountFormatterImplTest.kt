package com.edu.core.presentation.ui.formatter

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.util.Locale

class AmountFormatterImplTest {

    private val formatter = AmountFormatterImpl(Locale.UK)

    @Test
    fun `format renders positive amount with pound sign and 2dp`() {
        assertEquals("£420.00", formatter.format(BigDecimal("420")))
    }

    @Test
    fun `format renders negative amount with leading minus`() {
        assertEquals("-£46.82", formatter.format(BigDecimal("-46.82")))
    }

    @Test
    fun `format renders zero`() {
        assertEquals("£0.00", formatter.format(BigDecimal.ZERO))
    }

    @Test
    fun `format renders large amount with thousands separator`() {
        assertEquals("£12,345.67", formatter.format(BigDecimal("12345.67")))
    }

    @Test
    fun `formatSigned prefixes positive amount with plus`() {
        assertEquals("+£420.00", formatter.formatSigned(BigDecimal("420")))
    }

    @Test
    fun `formatSigned keeps minus for negative amount`() {
        assertEquals("-£46.82", formatter.formatSigned(BigDecimal("-46.82")))
    }

    @Test
    fun `formatSigned prefixes zero with plus`() {
        assertEquals("+£0.00", formatter.formatSigned(BigDecimal.ZERO))
    }
}
