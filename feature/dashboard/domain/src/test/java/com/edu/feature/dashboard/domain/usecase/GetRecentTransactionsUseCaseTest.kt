package com.edu.feature.dashboard.domain.usecase

import com.edu.feature.dashboard.domain.fixture.tx
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetRecentTransactionsUseCaseTest {

    private val useCase = GetRecentTransactionsUseCase()

    @Test
    fun `Should keep the newest five`() {
        val transactions = (1..7).map { tx("t$it", "2026-09-0$it", "-1.00") }

        assertEquals(listOf("t7", "t6", "t5", "t4", "t3"), useCase(transactions).map { it.transactionId })
    }

    @Test
    fun `Should leave out pending transactions`() {
        assertTrue(useCase(listOf(tx("p", "2026-09-16", "-1.00", pending = true))).isEmpty())
    }

    @Test
    fun `Should return everything When there are fewer than five`() {
        assertEquals(2, useCase(listOf(tx("a", "2026-09-01", "-1"), tx("b", "2026-09-02", "-1"))).size)
    }
}
