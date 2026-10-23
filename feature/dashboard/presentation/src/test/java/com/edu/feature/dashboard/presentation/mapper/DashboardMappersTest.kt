package com.edu.feature.dashboard.presentation.mapper

import androidx.compose.ui.graphics.toArgb
import com.edu.core.presentation.designsystem.Blue
import com.edu.feature.dashboard.domain.model.CategorySpend
import com.edu.feature.dashboard.domain.model.Greeting
import com.edu.feature.dashboard.domain.model.GreetingPeriod
import com.edu.feature.dashboard.domain.model.MonthlyCashFlow
import com.edu.feature.dashboard.domain.model.PendingSummary
import com.edu.feature.dashboard.domain.model.SafeToSpend
import com.edu.feature.dashboard.domain.model.UpcomingPayment
import com.edu.feature.dashboard.domain.model.UpcomingPaymentType
import com.edu.feature.dashboard.presentation.fake.amountFormatter
import com.edu.feature.dashboard.presentation.fake.dashboard
import com.edu.feature.dashboard.presentation.fake.dateFormatter
import com.edu.feature.dashboard.presentation.model.GreetingPeriodUi
import com.edu.feature.dashboard.presentation.model.UpcomingPaymentTypeUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

class DashboardHeaderUiMapperImplTest {

    private val mapper = DashboardHeaderUiMapperImpl(dateFormatter)

    @Test
    fun `map formats today as a long UK date`() {
        assertEquals("Wednesday, 16 September 2026", mapper.map(dashboard()).formattedDate)
    }

    @Test
    fun `map keeps the period`() {
        assertEquals(GreetingPeriodUi.MORNING, mapper.map(dashboard()).period)
    }

    @Test
    fun `map uses the first letter for the avatar`() {
        assertEquals("W", mapper.map(dashboard()).avatarInitial)
    }

    @Test
    fun `map uses a dot avatar When the name is unknown`() {
        assertEquals("•", mapper.map(dashboard().copy(greeting = Greeting(GreetingPeriod.EVENING, null))).avatarInitial)
    }
}

class SafeToSpendUiMapperImplTest {

    private val mapper = SafeToSpendUiMapperImpl(amountFormatter)
    private val safe = SafeToSpend(
        amount = BigDecimal("1406.78"),
        billsDue = BigDecimal("887.23"),
        spentThisMonth = BigDecimal("847.23"),
        billsShare = BigDecimal("0.2836"),
        spentShare = BigDecimal("0.2708"),
        yoursShare = BigDecimal("0.4456"),
    )

    @Test
    fun `map formats the safe amount`() {
        assertEquals("£1,406.78", mapper.map(safe).formattedAmount)
    }

    @Test
    fun `map formats bills due`() {
        assertEquals("£887.23", mapper.map(safe).formattedBillsDue)
    }

    @Test
    fun `map converts shares to fractions`() {
        assertEquals(0.4456f, mapper.map(safe).yoursFraction, 0.0001f)
    }

    @Test
    fun `map leaves the payday chip to the content mapper`() {
        assertNull(mapper.map(safe).daysToPayday)
    }
}

class CashFlowUiMapperImplTest {

    private val mapper = CashFlowUiMapperImpl(amountFormatter, dateFormatter)
    private fun flow(change: Int?) = MonthlyCashFlow(
        YearMonth.of(2026, 9), BigDecimal("1450.42"), BigDecimal("847.23"), YearMonth.of(2026, 8), BigDecimal("1010"), change,
    )

    @Test
    fun `map formats money in`() {
        assertEquals("£1,450.42", mapper.map(flow(-16)).formattedMoneyIn)
    }

    @Test
    fun `map names this and last month`() {
        val ui = mapper.map(flow(-16))

        assertEquals("Sep/Aug", "${ui.monthLabel}/${ui.previousMonthLabel}")
    }

    @Test
    fun `map shows the absolute change`() {
        assertEquals(16, mapper.map(flow(-16)).changePercent)
    }

    @Test
    fun `map marks spending down`() {
        assertFalse(mapper.map(flow(-16)).isSpendingUp)
    }

    @Test
    fun `map marks spending up`() {
        assertTrue(mapper.map(flow(12)).isSpendingUp)
    }

    @Test
    fun `map has no change When there is nothing to compare`() {
        assertNull(mapper.map(flow(null)).changePercent)
    }
}

class PendingUiMapperImplTest {

    @Test
    fun `map formats the pending total`() {
        assertEquals("£19.55", PendingUiMapperImpl(amountFormatter).map(PendingSummary(2, BigDecimal("19.55"))).formattedTotal)
    }
}

class UpcomingPaymentUiMapperImplTest {

    private val mapper = UpcomingPaymentUiMapperImpl(amountFormatter)
    private val payment = UpcomingPayment("dd", "EDF Energy", UpcomingPaymentType.DIRECT_DEBIT, BigDecimal("24.5"), LocalDate.of(2026, 9, 17), 1)

    @Test
    fun `map formats the amount`() {
        assertEquals("£24.50", mapper.map(payment).formattedAmount)
    }

    @Test
    fun `map takes the initial`() {
        assertEquals("E", mapper.map(payment).initial)
    }

    @Test
    fun `map keeps the type`() {
        assertEquals(UpcomingPaymentTypeUi.DIRECT_DEBIT, mapper.map(payment).type)
    }

    @Test
    fun `map keeps the days until due`() {
        assertEquals(1, mapper.map(payment).daysUntil)
    }

    @Test
    fun `map gives the same payee the same colour`() {
        assertEquals(mapper.map(payment).colorArgb, mapper.map(payment.copy(id = "other")).colorArgb)
    }
}

class CategorySpendUiMapperImplTest {

    private val mapper = CategorySpendUiMapperImpl(amountFormatter)

    @Test
    fun `map formats the amount`() {
        assertEquals("£234.50", mapper.map(CategorySpend("Shopping", BigDecimal("234.5"), BigDecimal.ONE)).formattedAmount)
    }

    @Test
    fun `map colours shopping blue`() {
        assertEquals(Blue.toArgb(), mapper.map(CategorySpend("Shopping", BigDecimal.ONE, BigDecimal.ONE)).colorArgb)
    }

    @Test
    fun `map converts the bar ratio`() {
        assertEquals(0.5f, mapper.map(CategorySpend("Bills", BigDecimal.ONE, BigDecimal("0.5"))).fraction, 0.0001f)
    }
}
