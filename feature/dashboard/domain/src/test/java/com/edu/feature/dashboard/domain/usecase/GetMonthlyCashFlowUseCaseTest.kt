package com.edu.feature.dashboard.domain.usecase

import com.edu.core.domain.model.TransactionCategory
import com.edu.feature.dashboard.domain.fixture.CLOCK
import com.edu.feature.dashboard.domain.fixture.clockAt
import com.edu.feature.dashboard.domain.fixture.tx
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal
import java.time.YearMonth

class GetMonthlyCashFlowUseCaseTest {

    private val useCase = GetMonthlyCashFlowUseCase(CLOCK)

    private val transactions = listOf(
        tx("in", "2026-09-01", "1450.42", TransactionCategory.CREDIT),
        tx("out1", "2026-09-02", "-800.00"),
        tx("out2", "2026-09-10", "-47.23"),
        tx("aug", "2026-08-20", "-1010.00"),
        tx("pending", "2026-09-15", "-99.00", pending = true),
    )

    @Test
    fun `Should sum this month's credits`() {
        assertEquals(BigDecimal("1450.42"), useCase(transactions).moneyIn)
    }

    @Test
    fun `Should sum this month's debits as a positive amount`() {
        assertEquals(BigDecimal("847.23"), useCase(transactions).moneyOut)
    }

    @Test
    fun `Should compare money out with last month`() {
        assertEquals(-16, useCase(transactions).moneyOutChangePercent)
    }

    @Test
    fun `Should name the previous month`() {
        assertEquals(YearMonth.of(2026, 8), useCase(transactions).previousMonth)
    }

    @Test
    fun `Should have no change When last month had no spending`() {
        assertNull(useCase(listOf(tx("out", "2026-09-02", "-10.00"))).moneyOutChangePercent)
    }

    @Test
    fun `Should be zero for an empty list`() {
        assertEquals(BigDecimal.ZERO, useCase(emptyList()).moneyOut)
    }

    @Test
    fun `Should put a transaction just before midnight in the old month`() {
        val flow = GetMonthlyCashFlowUseCase(clockAt("2026-10-01T08:00:00Z"))(
            listOf(tx("late", "2026-09-30T23:59:59Z", "-5.00"), tx("early", "2026-10-01T00:00:00Z", "-7.00")),
        )

        assertEquals(BigDecimal("7.00"), flow.moneyOut)
    }

    @Test
    fun `Should compare January with December`() {
        val flow = GetMonthlyCashFlowUseCase(clockAt("2027-01-10T08:00:00Z"))(
            listOf(tx("dec", "2026-12-20", "-100.00"), tx("jan", "2027-01-05", "-150.00")),
        )

        assertEquals(50, flow.moneyOutChangePercent)
    }
}
