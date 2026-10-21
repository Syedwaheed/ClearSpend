package com.edu.core.presentation.ui.mapper

import com.edu.core.presentation.ui.formatter.DateFormatterImpl
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.Locale

class SyncTimeUiMapperImplTest {

    private val mapper = SyncTimeUiMapperImpl(
        DateFormatterImpl(Locale.UK, Clock.fixed(Instant.parse("2026-09-16T18:00:00Z"), ZoneOffset.UTC)),
    )

    @Test
    fun `map shows the time for a sync earlier today`() {
        assertEquals("14:32", mapper.map(Instant.parse("2026-09-16T14:32:00Z")))
    }
}
