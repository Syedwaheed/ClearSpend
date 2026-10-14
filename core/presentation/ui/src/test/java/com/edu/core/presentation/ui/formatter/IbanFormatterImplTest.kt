package com.edu.core.presentation.ui.formatter

import org.junit.Assert.assertEquals
import org.junit.Test

class IbanFormatterImplTest {

    private val formatter = IbanFormatterImpl()

    @Test
    fun `mask keeps only last 4 characters of a long IBAN`() {
        assertEquals("•••• 6789", formatter.mask("GB29 NWBK 6016 1331 9268 6789"))
    }

    @Test
    fun `mask returns value unchanged When 4 characters or fewer`() {
        assertEquals("6789", formatter.mask("6789"))
    }

    @Test
    fun `formatSortCode groups digits in pairs`() {
        assertEquals("12-34-56", formatter.formatSortCode("123456"))
    }

    @Test
    fun `maskAccountNumber keeps only last 4 digits`() {
        assertEquals("•••• 5678", formatter.maskAccountNumber("12345678"))
    }
}
