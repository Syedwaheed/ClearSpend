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

class DateFormatterImplRelativeTest {

    private val now = Instant.parse("2026-09-16T14:32:00Z")
    private val formatter = DateFormatterImpl(Locale.UK, Clock.fixed(now, ZoneOffset.UTC))

    @Test
    fun `formatDate renders a calendar date`() {
        assertEquals("28 Sep 2026", formatter.formatDate(java.time.LocalDate.of(2026, 9, 28)))
    }

    @Test
    fun `formatLongDate renders weekday and full month`() {
        assertEquals("Wednesday, 16 September 2026", formatter.formatLongDate(java.time.LocalDate.of(2026, 9, 16)))
    }

    @Test
    fun `formatRelativeDay says Today for today`() {
        assertEquals("Today", formatter.formatRelativeDay(Instant.parse("2026-09-16T00:01:00Z")))
    }

    @Test
    fun `formatRelativeDay says Yesterday for yesterday`() {
        assertEquals("Yesterday", formatter.formatRelativeDay(Instant.parse("2026-09-15T23:59:00Z")))
    }

    @Test
    fun `formatRelativeDay drops the year within the same year`() {
        assertEquals("28 May", formatter.formatRelativeDay(Instant.parse("2026-05-28T10:00:00Z")))
    }

    @Test
    fun `formatRelativeDay keeps the year for another year`() {
        assertEquals("28 Dec 2025", formatter.formatRelativeDay(Instant.parse("2025-12-28T10:00:00Z")))
    }

    @Test
    fun `formatTimeAgo says just now under a minute`() {
        assertEquals("just now", formatter.formatTimeAgo(now.minusSeconds(30)))
    }

    @Test
    fun `formatTimeAgo counts minutes`() {
        assertEquals("2 min ago", formatter.formatTimeAgo(now.minusSeconds(150)))
    }

    @Test
    fun `formatTimeAgo counts hours`() {
        assertEquals("3 h ago", formatter.formatTimeAgo(now.minusSeconds(3 * 3600 + 5)))
    }

    @Test
    fun `formatTimeAgo falls back to the date after a day`() {
        assertEquals("14 Sep 2026", formatter.formatTimeAgo(Instant.parse("2026-09-14T10:00:00Z")))
    }

    @Test
    fun `formatMonthShort uses three letters`() {
        assertEquals("Sep", formatter.formatMonthShort(java.time.YearMonth.of(2026, 9)))
    }

    @Test
    fun `formatSyncTime shows only the time today`() {
        assertEquals("14:02", formatter.formatSyncTime(Instant.parse("2026-09-16T14:02:00Z")))
    }

    @Test
    fun `formatSyncTime shows date and time on an earlier day`() {
        assertEquals("15 Sep 2026, 09:05", formatter.formatSyncTime(Instant.parse("2026-09-15T09:05:00Z")))
    }
}

class DateFormatterImplTransactionsTest {

    // Wednesday 16 September 2026
    private val formatter = DateFormatterImpl(Locale.UK, Clock.fixed(Instant.parse("2026-09-16T14:32:00Z"), ZoneOffset.UTC))

    @Test
    fun `formatDayHeader says Today with the full date`() {
        assertEquals("Today — 16 September 2026", formatter.formatDayHeader(java.time.LocalDate.of(2026, 9, 16)))
    }

    @Test
    fun `formatDayHeader says Yesterday with the full date`() {
        assertEquals("Yesterday — 15 September 2026", formatter.formatDayHeader(java.time.LocalDate.of(2026, 9, 15)))
    }

    @Test
    fun `formatDayHeader names the weekday for older days`() {
        assertEquals("Thursday — 10 September 2026", formatter.formatDayHeader(java.time.LocalDate.of(2026, 9, 10)))
    }

    @Test
    fun `formatMonthYear spells the month out`() {
        assertEquals("June 2026", formatter.formatMonthYear(java.time.YearMonth.of(2026, 6)))
    }

    @Test
    fun `formatDateRange shows the year once within a year`() {
        assertEquals(
            "21 May – 1 Jun 2026",
            formatter.formatDateRange(java.time.LocalDate.of(2026, 5, 21), java.time.LocalDate.of(2026, 6, 1)),
        )
    }

    @Test
    fun `formatDateRange shows both years across a year boundary`() {
        assertEquals(
            "28 Dec 2025 – 3 Jan 2026",
            formatter.formatDateRange(java.time.LocalDate.of(2025, 12, 28), java.time.LocalDate.of(2026, 1, 3)),
        )
    }

    @Test
    fun `formatDateRange shows a single day once`() {
        assertEquals("1 Jun 2026", formatter.formatDateRange(java.time.LocalDate.of(2026, 6, 1), java.time.LocalDate.of(2026, 6, 1)))
    }
}
