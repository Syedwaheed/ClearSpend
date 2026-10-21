package com.edu.feature.dashboard.domain.usecase

import com.edu.core.domain.model.DirectDebitStatus
import com.edu.core.domain.model.StandingOrderFrequency
import com.edu.feature.dashboard.domain.fixture.CLOCK
import com.edu.feature.dashboard.domain.fixture.directDebit
import com.edu.feature.dashboard.domain.fixture.standingOrder
import com.edu.feature.dashboard.domain.model.UpcomingPaymentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class GetUpcomingPaymentsUseCaseTest {

    private val useCase = GetUpcomingPaymentsUseCase(CLOCK)

    @Test
    fun `Should derive a direct debit's next date as previous plus one month`() {
        val upcoming = useCase(listOf(directDebit("EDF", previous = "2026-08-17")), emptyList())

        assertEquals(LocalDate.of(2026, 9, 17), upcoming.single().dueDate)
    }

    @Test
    fun `Should roll a direct debit forward until it is not in the past`() {
        val upcoming = useCase(listOf(directDebit("EDF", previous = "2026-06-10")), emptyList())

        assertEquals(LocalDate.of(2026, 10, 10), upcoming.single().dueDate)
    }

    @Test
    fun `Should handle a month-end direct debit`() {
        val upcoming = useCase(listOf(directDebit("Gym", previous = "2026-08-31")), emptyList())

        assertEquals(LocalDate.of(2026, 9, 30), upcoming.single().dueDate)
    }

    @Test
    fun `Should skip inactive direct debits`() {
        val upcoming = useCase(listOf(directDebit("Old", "2026-08-17", status = DirectDebitStatus.INACTIVE)), emptyList())

        assertTrue(upcoming.isEmpty())
    }

    @Test
    fun `Should skip direct debits that never paid`() {
        assertTrue(useCase(listOf(directDebit("New", previous = null)), emptyList()).isEmpty())
    }

    @Test
    fun `Should use a standing order's next payment date`() {
        val upcoming = useCase(emptyList(), listOf(standingOrder("Rent", next = "2026-09-19")))

        assertEquals(3, upcoming.single().daysUntil)
    }

    @Test
    fun `Should roll a stale weekly standing order forward`() {
        val upcoming = useCase(emptyList(), listOf(standingOrder("Pocket", "2026-09-09", frequency = StandingOrderFrequency.WEEKLY)))

        assertEquals(LocalDate.of(2026, 9, 16), upcoming.single().dueDate)
    }

    @Test
    fun `Should sort by due date and take the limit`() {
        val upcoming = useCase(
            listOf(directDebit("EDF", "2026-08-17"), directDebit("AA", "2026-08-21")),
            listOf(standingOrder("Rent", "2026-09-19"), standingOrder("Later", "2026-10-01")),
            limit = 3,
        )

        assertEquals(listOf("EDF", "Rent", "AA"), upcoming.map { it.name })
    }

    @Test
    fun `Should label the payment type`() {
        assertEquals(UpcomingPaymentType.STANDING_ORDER, useCase(emptyList(), listOf(standingOrder("Rent", "2026-09-19"))).single().type)
    }
}
