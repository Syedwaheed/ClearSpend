package com.edu.feature.transactions.domain.usecase

import com.edu.feature.transactions.domain.model.DateWindow
import com.edu.feature.transactions.domain.model.PageWindow
import com.edu.feature.transactions.domain.model.PeriodBounds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class GetPageWindowUseCaseTest {

    private val useCase = GetPageWindowUseCase()
    private val longPeriod = PeriodBounds(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 15))

    @Test
    fun `Should load the newest 30 days first`() {
        assertEquals(
            PageWindow(DateWindow(LocalDate.of(2026, 5, 17), LocalDate.of(2026, 6, 15)), hasMore = true),
            useCase(longPeriod, loadedFrom = null),
        )
    }

    @Test
    fun `Should load a short period in one page`() {
        val thisMonth = PeriodBounds(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 15))

        assertEquals(PageWindow(DateWindow(thisMonth.earliest, thisMonth.latest), hasMore = false), useCase(thisMonth, null))
    }

    @Test
    fun `Should continue with the 30 days before the oldest loaded day`() {
        assertEquals(
            PageWindow(DateWindow(LocalDate.of(2026, 4, 17), LocalDate.of(2026, 5, 16)), hasMore = true),
            useCase(longPeriod, loadedFrom = LocalDate.of(2026, 5, 17)),
        )
    }

    @Test
    fun `Should clip the last page to the start of the period`() {
        assertEquals(
            PageWindow(DateWindow(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 10)), hasMore = false),
            useCase(longPeriod, loadedFrom = LocalDate.of(2026, 1, 11)),
        )
    }

    @Test
    fun `Should return null When the period is fully loaded`() {
        assertNull(useCase(longPeriod, loadedFrom = LocalDate.of(2026, 1, 1)))
    }

    @Test
    fun `Should load a single-day period`() {
        val day = LocalDate.of(2026, 6, 15)

        assertEquals(PageWindow(DateWindow(day, day), hasMore = false), useCase(PeriodBounds(day, day), null))
    }
}
