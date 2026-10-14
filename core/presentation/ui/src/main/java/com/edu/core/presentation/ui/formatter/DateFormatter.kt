package com.edu.core.presentation.ui.formatter

import java.time.Instant

interface DateFormatter {
    /** UK date format, e.g. "16 Sep 2026". */
    fun formatDate(instant: Instant): String

    /** 24h time, e.g. "14:32". */
    fun formatTime(instant: Instant): String
}
