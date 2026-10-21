package com.edu.feature.dashboard.domain.usecase

import com.edu.core.domain.model.TransactionCategory
import com.edu.feature.dashboard.domain.fixture.CLOCK
import com.edu.feature.dashboard.domain.fixture.salary
import com.edu.feature.dashboard.domain.fixture.tx
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class GetPaydayUseCaseTest {

    private val useCase = GetPaydayUseCase(CLOCK)

    @Test
    fun `Should project the next monthly salary`() {
        val payday = useCase(listOf(salary("2026-07-28"), salary("2026-08-28")))

        assertEquals(LocalDate.of(2026, 9, 28), payday?.nextDate)
    }

    @Test
    fun `Should count the days until payday`() {
        assertEquals(12, useCase(listOf(salary("2026-07-28"), salary("2026-08-28")))?.daysUntil)
    }

    @Test
    fun `Should pick the largest recurring credit`() {
        val transactions = listOf(
            salary("2026-07-28"), salary("2026-08-28"),
            tx("r1", "2026-08-01", "20.00", TransactionCategory.CREDIT, merchant = "Cashback"),
            tx("r2", "2026-09-01", "20.00", TransactionCategory.CREDIT, merchant = "Cashback"),
        )

        assertEquals(LocalDate.of(2026, 9, 28), useCase(transactions)?.nextDate)
    }

    @Test
    fun `Should ignore a large one-off credit`() {
        val transactions = listOf(
            salary("2026-07-28"), salary("2026-08-28"),
            tx("bonus", "2026-09-02", "9000.00", TransactionCategory.CREDIT, merchant = "Lottery"),
        )

        assertEquals(LocalDate.of(2026, 9, 28), useCase(transactions)?.nextDate)
    }

    @Test
    fun `Should group payers whose reference digits change`() {
        val transactions = listOf(
            tx("a", "2026-07-28", "2000", TransactionCategory.CREDIT, description = "ACME SAL 0726"),
            tx("b", "2026-08-28", "2000", TransactionCategory.CREDIT, description = "ACME SAL 0826"),
        )

        assertEquals(LocalDate.of(2026, 9, 28), useCase(transactions)?.nextDate)
    }

    @Test
    fun `Should roll forward past missed paydays`() {
        val payday = useCase(listOf(salary("2026-06-01"), salary("2026-07-01")))

        assertEquals(LocalDate.of(2026, 10, 1), payday?.nextDate)
    }

    @Test
    fun `Should say zero days When payday is today`() {
        assertEquals(0, useCase(listOf(salary("2026-07-16"), salary("2026-08-16")))?.daysUntil)
    }

    @Test
    fun `Should detect a weekly wage`() {
        val payday = useCase(listOf(salary("2026-09-02", "400"), salary("2026-09-09", "400")))

        assertEquals(LocalDate.of(2026, 9, 16), payday?.nextDate)
    }

    @Test
    fun `Should return null When no salary is found`() {
        assertNull(useCase(listOf(tx("d", "2026-09-01", "-10.00"))))
    }

    @Test
    fun `Should return null When a credit happened only once`() {
        assertNull(useCase(listOf(salary("2026-08-28"))))
    }

    @Test
    fun `Should ignore pending credits`() {
        val transactions = listOf(salary("2026-07-28"), salary("2026-08-28").copy(isPending = true))

        assertNull(useCase(transactions))
    }

    @Test
    fun `Should ignore daily repeats`() {
        assertNull(useCase(listOf(salary("2026-09-14", "5"), salary("2026-09-15", "5"), salary("2026-09-16", "5"))))
    }
}
