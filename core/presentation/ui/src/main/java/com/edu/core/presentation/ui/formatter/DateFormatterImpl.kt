package com.edu.core.presentation.ui.formatter

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

// Hard-coded so "Sep" never becomes "Sept" (newer CLDR data for en-GB).
private val MONTH_ABBREVIATIONS = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
)

class DateFormatterImpl(
    private val locale: Locale,
    private val clock: Clock,
) : DateFormatter {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", locale)
    private val longDateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", locale)

    private fun today(): LocalDate = LocalDate.now(clock)

    private fun Instant.localDate(): LocalDate = atZone(clock.zone).toLocalDate()

    override fun formatDate(instant: Instant): String = formatDate(instant.localDate())

    override fun formatDate(date: LocalDate): String =
        "${date.dayOfMonth} ${MONTH_ABBREVIATIONS[date.monthValue - 1]} ${date.year}"

    override fun formatLongDate(date: LocalDate): String = longDateFormatter.format(date)

    override fun formatTime(instant: Instant): String =
        timeFormatter.format(instant.atZone(clock.zone))

    override fun formatRelativeDay(instant: Instant): String {
        val date = instant.localDate()
        val today = today()
        return when {
            date == today -> "Today"
            date == today.minusDays(1) -> "Yesterday"
            date.year == today.year -> "${date.dayOfMonth} ${MONTH_ABBREVIATIONS[date.monthValue - 1]}"
            else -> formatDate(date)
        }
    }

    override fun formatTimeAgo(instant: Instant): String {
        val elapsed = Duration.between(instant, clock.instant())
        return when {
            elapsed.isNegative || elapsed.toMinutes() < 1 -> "just now"
            elapsed.toMinutes() < 60 -> "${elapsed.toMinutes()} min ago"
            elapsed.toHours() < 24 -> "${elapsed.toHours()} h ago"
            else -> formatDate(instant)
        }
    }

    override fun formatMonthShort(month: YearMonth): String = MONTH_ABBREVIATIONS[month.monthValue - 1]

    override fun formatSyncTime(instant: Instant): String =
        if (instant.localDate() == today()) formatTime(instant) else "${formatDate(instant)}, ${formatTime(instant)}"
}
