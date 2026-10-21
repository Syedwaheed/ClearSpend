package com.edu.feature.dashboard.domain.usecase

import com.edu.feature.dashboard.domain.fixture.tx
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class GetPendingSummaryUseCaseTest {

    private val useCase = GetPendingSummaryUseCase()

    @Test
    fun `Should count pending transactions`() {
        assertEquals(2, useCase(listOf(tx("a", "2026-09-16", "-12.55", pending = true), tx("b", "2026-09-16", "-7.00", pending = true))).count)
    }

    @Test
    fun `Should report the absolute pending total`() {
        assertEquals(BigDecimal("19.55"), useCase(listOf(tx("a", "2026-09-16", "-12.55"), tx("b", "2026-09-16", "-7.00"))).total)
    }

    @Test
    fun `Should be zero When nothing is pending`() {
        assertEquals(0, useCase(emptyList()).count)
    }
}
