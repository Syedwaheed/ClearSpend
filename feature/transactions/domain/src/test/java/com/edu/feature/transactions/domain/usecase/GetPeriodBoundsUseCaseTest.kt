package com.edu.feature.transactions.domain.usecase

import com.edu.feature.transactions.domain.fixture.CLOCK
import com.edu.feature.transactions.domain.fixture.TODAY
import com.edu.feature.transactions.domain.fixture.clockAt
import com.edu.feature.transactions.domain.model.PeriodBounds
import com.edu.feature.transactions.domain.model.TransactionPeriod
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class GetPeriodBoundsUseCaseTest {

    private val useCase = GetPeriodBoundsUseCase(CLOCK)

    @Test
    fun `Should reach back the maximum history for All`() {
        assertEquals(PeriodBounds(TODAY.minusMonths(24), TODAY), useCase(TransactionPeriod.All))
    }

    @Test
    fun `Should run from the first of the month to today for This Month`() {
        assertEquals(PeriodBounds(LocalDate.of(2026, 6, 1), TODAY), useCase(TransactionPeriod.ThisMonth))
    }

    @Test
    fun `Should cover the whole previous month for Last Month`() {
        assertEquals(
            PeriodBounds(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31)),
            useCase(TransactionPeriod.LastMonth),
        )
    }

    @Test
    fun `Should cross the year boundary for Last Month in January`() {
        val january = GetPeriodBoundsUseCase(clockAt("2027-01-10T10:00:00Z"))

        assertEquals(
            PeriodBounds(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31)),
            january(TransactionPeriod.LastMonth),
        )
    }

    @Test
    fun `Should keep a custom range as picked`() {
        val range = TransactionPeriod.Custom(LocalDate.of(2026, 5, 21), LocalDate.of(2026, 6, 1))

        assertEquals(PeriodBounds(LocalDate.of(2026, 5, 21), LocalDate.of(2026, 6, 1)), useCase(range))
    }

    @Test
    fun `Should swap a custom range picked backwards`() {
        val range = TransactionPeriod.Custom(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 5, 21))

        assertEquals(PeriodBounds(LocalDate.of(2026, 5, 21), LocalDate.of(2026, 6, 1)), useCase(range))
    }

    @Test
    fun `Should clip a custom range that ends in the future to today`() {
        val range = TransactionPeriod.Custom(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 7, 1))

        assertEquals(PeriodBounds(LocalDate.of(2026, 6, 1), TODAY), useCase(range))
    }

    @Test
    fun `Should collapse a custom range entirely in the future to today`() {
        val range = TransactionPeriod.Custom(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 5))

        assertEquals(PeriodBounds(TODAY, TODAY), useCase(range))
    }
}
