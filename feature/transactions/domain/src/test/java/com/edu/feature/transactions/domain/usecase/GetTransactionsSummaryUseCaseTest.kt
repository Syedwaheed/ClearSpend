package com.edu.feature.transactions.domain.usecase

import com.edu.feature.transactions.domain.fixture.CLOCK
import com.edu.feature.transactions.domain.fixture.tx
import com.edu.feature.transactions.domain.model.DailySpend
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

class GetTransactionsSummaryUseCaseTest {

    private val useCase = GetTransactionsSummaryUseCase()
    private val groupBy = GetTransactionDayGroupsUseCase(CLOCK)

    private val rows = listOf(
        tx("a", "2026-06-01T06:00:00Z", "-39.99"),
        tx("b", "2026-06-01T07:00:00Z", "-227.00"),
        tx("c", "2026-05-28T06:00:00Z", "420.00"),
        tx("d", "2026-05-27T09:14:00Z", "-24.50"),
        tx("e", "2026-05-22T20:12:00Z", "1450.42"),
    )
    private val summary = useCase(groupBy(rows))

    @Test
    fun `Should sum credits into money in`() {
        assertEquals(BigDecimal("1870.42"), summary.moneyIn)
    }

    @Test
    fun `Should sum debits into a positive money out`() {
        assertEquals(BigDecimal("291.49"), summary.moneyOut)
    }

    @Test
    fun `Should net money in against money out`() {
        assertEquals(BigDecimal("1578.93"), summary.net)
    }

    @Test
    fun `Should count the transactions`() {
        assertEquals(5, summary.count)
    }

    @Test
    fun `Should give one bar per day of debits only oldest first`() {
        assertEquals(
            listOf(
                DailySpend(LocalDate.of(2026, 5, 22), BigDecimal.ZERO),
                DailySpend(LocalDate.of(2026, 5, 27), BigDecimal("24.50")),
                DailySpend(LocalDate.of(2026, 5, 28), BigDecimal.ZERO),
                DailySpend(LocalDate.of(2026, 6, 1), BigDecimal("266.99")),
            ),
            summary.dailySpending,
        )
    }

    @Test
    fun `Should keep only the newest days for the bars`() {
        val manyDays = (1..20).map { day -> tx("t$day", "2026-05-%02dT10:00:00Z".format(day), "-1.00") }

        val bars = useCase(groupBy(manyDays)).dailySpending

        assertEquals(
            (20 - GetTransactionsSummaryUseCase.MAX_BARS + 1..20).map { LocalDate.of(2026, 5, it) },
            bars.map { it.date },
        )
    }

    @Test
    fun `Should be all zero for no transactions`() {
        val empty = useCase(emptyList())

        assertEquals(listOf(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0), listOf(empty.moneyIn, empty.moneyOut, empty.net, empty.count))
    }
}
