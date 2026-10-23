package com.edu.feature.transactions.domain.usecase

import com.edu.core.domain.model.TransactionCategory
import com.edu.feature.transactions.domain.fixture.CLOCK
import com.edu.feature.transactions.domain.fixture.NOW
import com.edu.feature.transactions.domain.fixture.tx
import com.edu.feature.transactions.domain.model.CategoryFilter
import com.edu.feature.transactions.domain.model.TransactionPeriod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class GetTransactionsOverviewUseCaseTest {

    private val useCase = GetTransactionsOverviewUseCase(
        getPeriodBounds = GetPeriodBoundsUseCase(CLOCK),
        getFilteredTransactions = GetFilteredTransactionsUseCase(),
        getDayGroups = GetTransactionDayGroupsUseCase(CLOCK),
        getSummary = GetTransactionsSummaryUseCase(),
        clock = CLOCK,
    )

    private val june = tx("june", "2026-06-10T10:00:00Z", "-39.99", merchant = "Tesco")
    private val may = tx("may", "2026-05-20T10:00:00Z", "-24.50", merchant = "EDF Energy", category = TransactionCategory.DIRECT_DEBIT)
    private val coffee = tx("p1", "2026-06-15T08:41:00Z", "-4.65", merchant = "Costa Coffee", pending = true)
    private val uber = tx("p2", "2026-06-15T09:00:00Z", "-14.90", merchant = "Uber", pending = true)

    private fun overview(
        period: TransactionPeriod = TransactionPeriod.All,
        query: String = "",
        category: CategoryFilter = CategoryFilter.ALL,
    ) = useCase(listOf(june, may), listOf(coffee, uber), period, query, category, offlineSince = null)

    @Test
    fun `Should keep only settled rows inside the period`() {
        val groups = overview(period = TransactionPeriod.ThisMonth).groups

        assertEquals(listOf("june"), groups.flatMap { it.transactions }.map { it.transactionId })
    }

    @Test
    fun `Should show pending rows When the period includes today`() {
        assertEquals(listOf("p2", "p1"), overview(period = TransactionPeriod.ThisMonth).pending.map { it.transactionId })
    }

    @Test
    fun `Should hide pending rows When the period ends before today`() {
        assertTrue(overview(period = TransactionPeriod.LastMonth).pending.isEmpty())
    }

    @Test
    fun `Should total the visible pending rows`() {
        assertEquals(BigDecimal("-19.55"), overview().pendingTotal)
    }

    @Test
    fun `Should search the pending rows too`() {
        assertEquals(listOf("p1"), overview(query = "costa").pending.map { it.transactionId })
    }

    @Test
    fun `Should apply the category to the settled rows`() {
        val groups = overview(category = CategoryFilter.DIRECT_DEBIT).groups

        assertEquals(listOf("may"), groups.flatMap { it.transactions }.map { it.transactionId })
    }

    @Test
    fun `Should summarise only the filtered settled rows`() {
        assertEquals(BigDecimal("39.99"), overview(query = "tesco").summary.moneyOut)
    }

    @Test
    fun `Should be empty When nothing matches`() {
        assertTrue(overview(query = "zzzz").isEmpty)
    }

    @Test
    fun `Should not be empty When only pending rows match`() {
        assertFalse(overview(query = "uber").isEmpty)
    }

    @Test
    fun `Should pass the offline time through`() {
        val result = useCase(emptyList(), emptyList(), TransactionPeriod.All, "", CategoryFilter.ALL, offlineSince = NOW)

        assertEquals(NOW, result.offlineSince)
    }
}
