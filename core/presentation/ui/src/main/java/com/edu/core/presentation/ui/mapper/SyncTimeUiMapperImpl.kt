package com.edu.core.presentation.ui.mapper

import com.edu.core.presentation.ui.formatter.DateFormatter
import java.time.Instant

class SyncTimeUiMapperImpl(
    private val dateFormatter: DateFormatter,
) : SyncTimeUiMapper {
    override fun map(domain: Instant): String = dateFormatter.formatSyncTime(domain)
}
