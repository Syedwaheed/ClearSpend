package com.edu.feature.transactions.domain.usecase

import com.edu.feature.transactions.domain.fixture.CLOCK
import com.edu.feature.transactions.domain.fixture.clockAt
import com.edu.feature.transactions.domain.fixture.tx
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZoneId

class GetTransactionDayGroupsUseCaseTest {

    private val useCase = GetTransactionDayGroupsUseCase(CLOCK)

    private val june1Morning = tx("a", "2026-06-01T06:00:00Z", "-39.99")
    private val june1Evening = tx("b", "2026-06-01T20:00:00Z", "420.00")
    private val may28 = tx("c", "2026-05-28T06:00:00Z", "-24.50")

    @Test
    fun `Should put the newest day first`() {
        val groups = useCase(listOf(may28, june1Morning, june1Evening))

        assertEquals(listOf(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 5, 28)), groups.map { it.date })
    }

    @Test
    fun `Should sum the signed amounts of a day`() {
        val groups = useCase(listOf(june1Morning, june1Evening))

        assertEquals(BigDecimal("380.01"), groups.single().net)
    }

    @Test
    fun `Should give a negative net for a day of debits`() {
        assertEquals(BigDecimal("-24.50"), useCase(listOf(may28)).single().net)
    }

    @Test
    fun `Should order a day's transactions newest first`() {
        val groups = useCase(listOf(june1Morning, june1Evening))

        assertEquals(listOf("b", "a"), groups.single().transactions.map { it.transactionId })
    }

    @Test
    fun `Should group by the local calendar date`() {
        val london = GetTransactionDayGroupsUseCase(clockAt("2026-06-15T09:30:00Z", ZoneId.of("Europe/London")))

        // 23:30 UTC on 31 May is 00:30 BST on 1 June.
        val groups = london(listOf(tx("late", "2026-05-31T23:30:00Z")))

        assertEquals(LocalDate.of(2026, 6, 1), groups.single().date)
    }

    @Test
    fun `Should return no groups for no transactions`() {
        assertEquals(emptyList<Any>(), useCase(emptyList()))
    }
}
