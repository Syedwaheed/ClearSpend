package com.edu.core.presentation.ui.formatter

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.Locale

class DateFormatterImplTest {

    private val fixedClock = Clock.fixed(Instant.parse("2026-09-16T00:00:00Z"), ZoneOffset.UTC)
    private val formatter = DateFormatterImpl(Locale.UK, fixedClock)

    @Test
    fun `formatDate renders UK date format`() {
        val instant = Instant.parse("2026-09-16T10:00:00Z")

        assertEquals("16 Sep 2026", formatter.formatDate(instant))
    }

    @Test
    fun `formatTime renders 24h time`() {
        val instant = Instant.parse("2026-09-16T14:32:00Z")

        assertEquals("14:32", formatter.formatTime(instant))
    }

    @Test
    fun `formatTime pads single digit minutes and hour`() {
        val instant = Instant.parse("2026-09-16T09:05:00Z")

        assertEquals("09:05", formatter.formatTime(instant))
    }
}
