package com.edu.core.presentation.ui.formatter

import java.time.Clock
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale

private val MONTH_ABBREVIATIONS = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
)

class DateFormatterImpl(
    private val locale: Locale,
    private val clock: Clock,
) : DateFormatter {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", locale)

    override fun formatDate(instant: Instant): String {
        val date = instant.atZone(clock.zone).toLocalDate()
        val month = MONTH_ABBREVIATIONS[date.monthValue - 1]
        return "${date.dayOfMonth} $month ${date.year}"
    }

    override fun formatTime(instant: Instant): String =
        timeFormatter.format(instant.atZone(clock.zone))
}
