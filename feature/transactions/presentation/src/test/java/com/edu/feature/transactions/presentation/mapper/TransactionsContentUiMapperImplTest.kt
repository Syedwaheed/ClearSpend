package com.edu.feature.transactions.presentation.mapper

import com.edu.feature.transactions.domain.model.DailySpend
import com.edu.feature.transactions.domain.model.PeriodBounds
import com.edu.feature.transactions.domain.model.TransactionDayGroup
import com.edu.feature.transactions.domain.model.TransactionPeriod
import com.edu.feature.transactions.domain.model.TransactionsOverview
import com.edu.feature.transactions.domain.model.TransactionsSummary
import com.edu.feature.transactions.presentation.fake.NOW
import com.edu.feature.transactions.presentation.fake.TODAY
import com.edu.feature.transactions.presentation.fake.amountFormatter
import com.edu.feature.transactions.presentation.fake.dateFormatter
import com.edu.feature.transactions.presentation.fake.syncTimeMapper
import com.edu.feature.transactions.presentation.fake.transactionUiMapper
import com.edu.feature.transactions.presentation.fake.tx
import com.edu.feature.transactions.presentation.model.DailyBarUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Locale

class TransactionsContentUiMapperImplTest {

    private val mapper = TransactionsContentUiMapperImpl(transactionUiMapper, amountFormatter, dateFormatter, syncTimeMapper, Locale.UK)

    private fun overview(
        period: TransactionPeriod = TransactionPeriod.All,
        bounds: PeriodBounds = PeriodBounds(TODAY.minusMonths(24), TODAY),
        summary: TransactionsSummary = TransactionsSummary(
            moneyIn = BigDecimal("1870.42"),
            moneyOut = BigDecimal("513.02"),
            net = BigDecimal("1357.40"),
            count = 12,
            dailySpending = listOf(DailySpend(TODAY.minusDays(1), BigDecimal("50")), DailySpend(TODAY, BigDecimal("200"))),
        ),
        groups: List<TransactionDayGroup> = emptyList(),
        pendingTotal: BigDecimal = BigDecimal.ZERO,
        offlineSince: java.time.Instant? = null,
    ) = TransactionsOverview(period, bounds, summary, emptyList(), pendingTotal, groups, offlineSince)

    @Test
    fun `Should flag All time`() {
        assertTrue(mapper.map(overview()).summary.isAllTime)
    }

    @Test
    fun `Should label This Month with the month and year`() {
        val result = mapper.map(overview(TransactionPeriod.ThisMonth, PeriodBounds(LocalDate.of(2026, 6, 1), TODAY)))

        assertEquals("June 2026", result.summary.periodLabel)
    }

    @Test
    fun `Should label Last Month with the previous month`() {
        val bounds = PeriodBounds(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31))

        assertEquals("May 2026", mapper.map(overview(TransactionPeriod.LastMonth, bounds)).summary.periodLabel)
    }

    @Test
    fun `Should label a custom range with its dates`() {
        val from = LocalDate.of(2026, 5, 21)
        val to = LocalDate.of(2026, 6, 1)

        assertEquals("21 May – 1 Jun 2026", mapper.map(overview(TransactionPeriod.Custom(from, to), PeriodBounds(from, to))).summary.periodLabel)
    }

    @Test
    fun `Should format money in`() {
        assertEquals("£1,870.42", mapper.map(overview()).summary.formattedMoneyIn)
    }

    @Test
    fun `Should format money out without a sign`() {
        assertEquals("£513.02", mapper.map(overview()).summary.formattedMoneyOut)
    }

    @Test
    fun `Should sign the net`() {
        assertEquals("+£1,357.40", mapper.map(overview()).summary.formattedNet)
    }

    @Test
    fun `Should pass the transaction count`() {
        assertEquals(12, mapper.map(overview()).summary.transactionCount)
    }

    @Test
    fun `Should scale bars to the busiest day and highlight the newest`() {
        assertEquals(listOf(DailyBarUi(0.25f, false), DailyBarUi(1f, true)), mapper.map(overview()).summary.dailyBars)
    }

    @Test
    fun `Should draw flat bars When nothing was spent`() {
        val zero = overview().summary.copy(dailySpending = listOf(DailySpend(TODAY, BigDecimal.ZERO)))

        assertEquals(listOf(DailyBarUi(0f, true)), mapper.map(overview(summary = zero)).summary.dailyBars)
    }

    @Test
    fun `Should capitalise the date group header`() {
        val group = TransactionDayGroup(TODAY, BigDecimal("-24.50"), listOf(tx("a")))

        assertEquals("TODAY — 15 JUNE 2026", mapper.map(overview(groups = listOf(group))).groups.single().label)
    }

    @Test
    fun `Should sign the day net`() {
        val group = TransactionDayGroup(LocalDate.of(2026, 5, 28), BigDecimal("-24.50"), listOf(tx("a")))

        assertEquals("-£24.50", mapper.map(overview(groups = listOf(group))).groups.single().formattedNet)
    }

    @Test
    fun `Should mark a negative day net`() {
        val group = TransactionDayGroup(LocalDate.of(2026, 5, 28), BigDecimal("-24.50"), listOf(tx("a")))

        assertFalse(mapper.map(overview(groups = listOf(group))).groups.single().isNetPositive)
    }

    @Test
    fun `Should mark a positive day net`() {
        val group = TransactionDayGroup(LocalDate.of(2026, 5, 28), BigDecimal("420.00"), listOf(tx("a", amount = "420.00")))

        assertTrue(mapper.map(overview(groups = listOf(group))).groups.single().isNetPositive)
    }

    @Test
    fun `Should map the rows of a group`() {
        val group = TransactionDayGroup(TODAY, BigDecimal("-39.99"), listOf(tx("a")))

        assertEquals(listOf("-£39.99"), mapper.map(overview(groups = listOf(group))).groups.single().transactions.map { it.formattedAmount })
    }

    @Test
    fun `Should sign the pending total`() {
        assertEquals("-£19.55", mapper.map(overview(pendingTotal = BigDecimal("-19.55"))).formattedPendingTotal)
    }

    @Test
    fun `Should show the sync time When served from the cache`() {
        assertEquals("09:30", mapper.map(overview(offlineSince = NOW)).offlineSyncTime)
    }

    @Test
    fun `Should hide the offline banner When the data is fresh`() {
        assertNull(mapper.map(overview()).offlineSyncTime)
    }
}
