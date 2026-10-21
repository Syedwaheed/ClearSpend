package com.edu.feature.dashboard.domain.usecase

import com.edu.core.domain.model.TransactionCategory
import com.edu.feature.dashboard.domain.fixture.CLOCK
import com.edu.feature.dashboard.domain.fixture.tx
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class GetSpendingByCategoryUseCaseTest {

    private val useCase = GetSpendingByCategoryUseCase(CLOCK)

    private val transactions = listOf(
        tx("1", "2026-09-02", "-200.00", classification = listOf("Shopping", "Groceries")),
        tx("2", "2026-09-03", "-34.50", classification = listOf("Shopping", "Clothes")),
        tx("3", "2026-09-04", "-227.00", classification = listOf("Transfers")),
        tx("4", "2026-09-05", "-180.00", classification = listOf("Bills")),
        tx("aug", "2026-08-31", "-999.00", classification = listOf("Bills")),
        tx("in", "2026-09-01", "100.00", TransactionCategory.CREDIT, classification = listOf("Income")),
    )

    @Test
    fun `Should group this month's debits by the first classification`() {
        assertEquals(listOf("Shopping", "Transfers", "Bills"), useCase(transactions).map { it.category })
    }

    @Test
    fun `Should sum each category`() {
        assertEquals(BigDecimal("234.50"), useCase(transactions).first().amount)
    }

    @Test
    fun `Should give the largest category a full bar`() {
        assertEquals(BigDecimal("1.0000"), useCase(transactions).first().ratioToLargest)
    }

    @Test
    fun `Should put unclassified spending under Other`() {
        assertEquals("Other", useCase(listOf(tx("x", "2026-09-02", "-5.00"))).single().category)
    }

    @Test
    fun `Should be empty When nothing was spent this month`() {
        assertTrue(useCase(listOf(tx("aug", "2026-08-31", "-9.00"))).isEmpty())
    }
}
