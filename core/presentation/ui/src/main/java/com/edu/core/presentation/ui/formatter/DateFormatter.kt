package com.edu.core.presentation.ui.formatter

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

interface DateFormatter {
    /** UK date format, e.g. "16 Sep 2026". */
    fun formatDate(instant: Instant): String

    /** UK date format for a calendar date, e.g. "28 Sep 2026". */
    fun formatDate(date: LocalDate): String

    /** Long UK date with weekday, e.g. "Wednesday, 16 September 2026" (Home header). */
    fun formatLongDate(date: LocalDate): String

    /** 24h time, e.g. "14:32". */
    fun formatTime(instant: Instant): String

    /** "Today", "Yesterday", otherwise "28 May" (plus the year when it differs from this year). */
    fun formatRelativeDay(instant: Instant): String

    /** "just now", "5 min ago", "3 h ago", otherwise the UK date. */
    fun formatTimeAgo(instant: Instant): String

    /** Three-letter month, e.g. "Aug". */
    fun formatMonthShort(month: YearMonth): String

    /** "14:32" when [instant] is today, otherwise "16 Sep 2026, 14:32" (offline banner). */
    fun formatSyncTime(instant: Instant): String
}
