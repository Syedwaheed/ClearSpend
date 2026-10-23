package com.edu.feature.accounts.domain.usecase

import com.edu.feature.accounts.domain.fixture.CLOCK
import com.edu.feature.accounts.domain.fixture.tx
import com.edu.feature.accounts.domain.fixture.withBalance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.math.BigDecimal

class GetBalanceHistoryUseCaseTest {

    private val useCase = GetBalanceHistoryUseCase(CLOCK)
    private val account = withBalance("a", "500", current = "500")

    @Test
    fun `Should produce eight weekly points`() {
        assertEquals(8, useCase(listOf(account), emptyMap(), BigDecimal.ZERO).weeklyNetBalances.size)
    }

    @Test
    fun `Should be flat When there are no transactions`() {
        val history = useCase(listOf(account), mapOf("a" to emptyList()), BigDecimal.ZERO)

        assertEquals(setOf(BigDecimal("500")), history.weeklyNetBalances.toSet())
    }

    @Test
    fun `Should use running balance When present`() {
        val history = useCase(
            listOf(account),
            mapOf("a" to listOf(tx("old", "2026-08-01", "-10", runningBalance = "300"), tx("new", "2026-09-15", "200", runningBalance = "500"))),
            BigDecimal.ZERO,
        )

        // Third point = end of 12 Aug: latest transaction at or before it is "old".
        assertEquals(BigDecimal("300"), history.weeklyNetBalances[2])
    }

    @Test
    fun `Should step back over the first transaction When the point predates it`() {
        val history = useCase(
            listOf(account),
            mapOf("a" to listOf(tx("old", "2026-08-01", "-10", runningBalance = "300"))),
            BigDecimal.ZERO,
        )

        // First point = end of 29 Jul, before "old": 300 − (−10).
        assertEquals(BigDecimal("310"), history.weeklyNetBalances.first())
    }

    @Test
    fun `Should back-calculate When running balance is null`() {
        val history = useCase(
            listOf(account),
            mapOf("a" to listOf(tx("credit", "2026-09-15", "200"))),
            BigDecimal.ZERO,
        )

        assertEquals(BigDecimal("300"), history.weeklyNetBalances.first())
    }

    @Test
    fun `Should end at today's balance`() {
        val history = useCase(listOf(account), mapOf("a" to listOf(tx("credit", "2026-09-15", "200"))), BigDecimal.ZERO)

        assertEquals(BigDecimal("500"), history.weeklyNetBalances.last())
    }

    @Test
    fun `Should subtract card debt from every point`() {
        val history = useCase(listOf(account), emptyMap(), BigDecimal("100"))

        assertEquals(BigDecimal("400"), history.weeklyNetBalances.first())
    }

    @Test
    fun `Should compute the change since the end of last month`() {
        val history = useCase(
            listOf(account),
            mapOf("a" to listOf(tx("aug", "2026-08-30", "-50"), tx("sep1", "2026-09-01", "100"), tx("sep2", "2026-09-10", "27.85"))),
            BigDecimal.ZERO,
        )

        assertEquals(BigDecimal("127.85"), history.changeThisMonth)
    }

    @Test
    fun `Should compute a negative 30-day change`() {
        val history = useCase(
            listOf(account),
            mapOf("a" to listOf(tx("big", "2026-09-01", "-312.45"))),
            BigDecimal.ZERO,
        )

        assertEquals(BigDecimal("-312.45"), history.thirtyDayChangeByAccount["a"])
    }

    @Test
    fun `Should ignore pending transactions`() {
        val history = useCase(listOf(account), mapOf("a" to listOf(tx("p", "2026-09-15", "-99", pending = true))), BigDecimal.ZERO)

        assertEquals(BigDecimal.ZERO, history.thirtyDayChangeByAccount["a"])
    }

    @Test
    fun `Should have no 30-day change When the account's history failed to load`() {
        assertFalse("a" in useCase(listOf(account), emptyMap(), BigDecimal.ZERO).thirtyDayChangeByAccount)
    }

    @Test
    fun `Should be all zero When there are no accounts`() {
        assertEquals(BigDecimal.ZERO, useCase(emptyList(), emptyMap(), BigDecimal.ZERO).changeThisMonth)
    }
}
