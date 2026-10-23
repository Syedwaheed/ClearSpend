package com.edu.feature.transactions.domain.usecase

import com.edu.feature.transactions.domain.fixture.CLOCK
import com.edu.feature.transactions.domain.fixture.tx
import com.edu.feature.transactions.domain.model.MerchantInsight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.YearMonth

class GetMerchantInsightUseCaseTest {

    private val useCase = GetMerchantInsightUseCase(CLOCK)

    private val tesco = tx("t1", "2026-06-10T06:00:00Z", "-39.99", merchant = "Tesco")

    private fun monthly(insight: MerchantInsight) = insight as MerchantInsight.MonthlyTotal

    @Test
    fun `Should be a first visit When there is no other payment to the merchant`() {
        assertEquals(MerchantInsight.FirstTime("Tesco", 90), useCase(tesco, listOf(tesco)))
    }

    @Test
    fun `Should sum the merchant's payments in the transaction's month`() {
        val history = listOf(tesco, tx("t2", "2026-06-02T06:00:00Z", "-117.00", merchant = "Tesco"))

        assertEquals(BigDecimal("156.99"), monthly(useCase(tesco, history)).total)
    }

    @Test
    fun `Should count the merchant's payments in the month`() {
        val history = listOf(tesco, tx("t2", "2026-06-02T06:00:00Z", "-117.00", merchant = "Tesco"))

        assertEquals(2, monthly(useCase(tesco, history)).count)
    }

    @Test
    fun `Should leave other months out of the total`() {
        val history = listOf(tesco, tx("may", "2026-05-30T06:00:00Z", "-50.00", merchant = "Tesco"))

        assertEquals(BigDecimal("39.99"), monthly(useCase(tesco, history)).total)
    }

    @Test
    fun `Should not be a first visit When the merchant was paid in the last 90 days`() {
        val history = listOf(tesco, tx("may", "2026-05-30T06:00:00Z", "-50.00", merchant = "Tesco"))

        assertEquals(YearMonth.of(2026, 6), monthly(useCase(tesco, history)).month)
    }

    @Test
    fun `Should be a first visit When the earlier payment is older than 90 days`() {
        val history = listOf(tesco, tx("old", "2026-02-01T06:00:00Z", "-50.00", merchant = "Tesco"))

        assertTrue(useCase(tesco, history) is MerchantInsight.FirstTime)
    }

    @Test
    fun `Should match the merchant ignoring case`() {
        val history = listOf(tesco, tx("t2", "2026-06-02T06:00:00Z", "-1.00", merchant = "TESCO"))

        assertEquals(2, monthly(useCase(tesco, history)).count)
    }

    @Test
    fun `Should use the description When there is no merchant name`() {
        val rent = tx("r1", "2026-06-01T06:00:00Z", "-900.00", merchant = null, description = "RENT JUNE")

        assertEquals(MerchantInsight.FirstTime("RENT JUNE", 90), useCase(rent, emptyList()))
    }

    @Test
    fun `Should ignore pending rows in the history`() {
        val history = listOf(tesco, tx("p", "2026-06-12T06:00:00Z", "-5.00", merchant = "Tesco", pending = true))

        assertTrue(useCase(tesco, history) is MerchantInsight.FirstTime)
    }

    @Test
    fun `Should count a pending transaction itself`() {
        val pending = tx("p", "2026-06-12T06:00:00Z", "-5.00", merchant = "Tesco", pending = true)

        assertEquals(BigDecimal("44.99"), monthly(useCase(pending, listOf(tesco))).total)
    }

    @Test
    fun `Should flag one of the month's top merchants`() {
        val history = listOf(tesco, tx("t2", "2026-06-02T06:00:00Z", "-117.00", merchant = "Tesco"))

        assertTrue(monthly(useCase(tesco, history)).isTopMerchant)
    }

    @Test
    fun `Should not flag a merchant outside the month's top five`() {
        val bigger = (1..5).map { tx("big$it", "2026-06-0${it}T06:00:00Z", "-500.00", merchant = "Shop $it") }
        val history = bigger + tesco + tx("t2", "2026-06-02T06:00:00Z", "-1.00", merchant = "Tesco")

        assertFalse(monthly(useCase(tesco, history)).isTopMerchant)
    }

    @Test
    fun `Should not flag a credit as a top merchant`() {
        val salary = tx("s1", "2026-06-10T06:00:00Z", "1450.42", merchant = "Acme Ltd")
        val history = listOf(salary, tx("s0", "2026-05-10T06:00:00Z", "1450.42", merchant = "Acme Ltd"))

        assertFalse(monthly(useCase(salary, history)).isTopMerchant)
    }
}
