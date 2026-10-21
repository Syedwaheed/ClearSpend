package com.edu.feature.auth.presentation.mapper

import com.edu.feature.auth.domain.model.BankProvider
import com.edu.feature.auth.domain.model.BankSegment
import com.edu.feature.auth.presentation.model.BankUi
import org.junit.Assert.assertEquals
import org.junit.Test

class BankUiMapperImplTest {

    private val mapper = BankUiMapperImpl()

    @Test
    fun `Should map a known bank with its brand colour and initial`() {
        assertEquals(
            BankUi("ob-barclays", "Barclays", "B", 0xFF00AEEF, "Personal"),
            mapper.map(BankProvider("ob-barclays", "Barclays")),
        )
    }

    @Test
    fun `Should use two-letter initials where the design does`() {
        assertEquals("St", mapper.map(BankProvider("ob-starling", "Starling")).initial)
        assertEquals("FD", mapper.map(BankProvider("ob-first-direct", "First Direct")).initial)
    }

    @Test
    fun `Should fall back to first letter and grey for an unknown bank`() {
        val ui = mapper.map(BankProvider("ob-new", "nova bank"))
        assertEquals("N", ui.initial)
        assertEquals(0xFF6B7684, ui.brandColorArgb)
    }

    @Test
    fun `Should label business banks`() {
        assertEquals("Business", mapper.map(BankProvider("ob-x", "X", BankSegment.BUSINESS)).segmentLabel)
    }
}
