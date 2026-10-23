package com.edu.feature.dashboard.domain.usecase

import com.edu.core.domain.model.AccountType
import com.edu.core.domain.model.AccountWithBalance
import com.edu.feature.dashboard.domain.fixture.CLOCK
import com.edu.feature.dashboard.domain.fixture.account
import com.edu.feature.dashboard.domain.fixture.withBalance
import com.edu.feature.dashboard.domain.model.Payday
import com.edu.feature.dashboard.domain.model.UpcomingPayment
import com.edu.feature.dashboard.domain.model.UpcomingPaymentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

class GetSafeToSpendUseCaseTest {

    private val useCase = GetSafeToSpendUseCase(CLOCK)

    private fun bill(date: String, amount: String) =
        UpcomingPayment(date, date, UpcomingPaymentType.DIRECT_DEBIT, BigDecimal(amount), LocalDate.parse(date), 0)

    private val payday = Payday(LocalDate.of(2026, 9, 28), 12, BigDecimal("2000"))

    @Test
    fun `Should add available and overdraft minus bills before payday`() {
        val result = useCase(
            listOf(withBalance("a", "44.01", overdraft = "100")),
            listOf(bill("2026-09-17", "24.50"), bill("2026-09-19", "10.00")),
            payday,
            BigDecimal.ZERO,
        )

        assertEquals(BigDecimal("109.51"), result?.amount)
    }

    @Test
    fun `Should ignore savings accounts`() {
        val result = useCase(
            listOf(withBalance("a", "100"), withBalance("s", "5000", type = AccountType.SAVINGS)),
            emptyList(),
            payday,
            BigDecimal.ZERO,
        )

        assertEquals(BigDecimal("100"), result?.amount)
    }

    @Test
    fun `Should include business current accounts`() {
        val result = useCase(
            listOf(withBalance("a", "100"), withBalance("b", "50", type = AccountType.BUSINESS_TRANSACTION)),
            emptyList(),
            payday,
            BigDecimal.ZERO,
        )

        assertEquals(BigDecimal("150"), result?.amount)
    }

    @Test
    fun `Should not count bills due on or after payday`() {
        val result = useCase(listOf(withBalance("a", "100")), listOf(bill("2026-09-28", "80")), payday, BigDecimal.ZERO)

        assertEquals(BigDecimal.ZERO, result?.billsDue)
    }

    @Test
    fun `Should use the end of the month When no salary is found`() {
        val result = useCase(
            listOf(withBalance("a", "100")),
            listOf(bill("2026-09-30", "30"), bill("2026-10-01", "50")),
            payday = null,
            spentThisMonth = BigDecimal.ZERO,
        )

        assertEquals(BigDecimal("30"), result?.billsDue)
    }

    @Test
    fun `Should never go below zero`() {
        val result = useCase(listOf(withBalance("a", "10")), listOf(bill("2026-09-17", "500")), payday, BigDecimal.ZERO)

        assertEquals(BigDecimal.ZERO, result?.amount)
    }

    @Test
    fun `Should split the bar into bills spent and yours`() {
        val result = useCase(listOf(withBalance("a", "50")), listOf(bill("2026-09-17", "25")), payday, BigDecimal("50"))!!

        assertEquals(
            listOf("0.2500", "0.5000", "0.2500"),
            listOf(result.billsShare, result.spentShare, result.yoursShare).map { it.toPlainString() },
        )
    }

    @Test
    fun `Should give zero shares When there is nothing at all`() {
        val result = useCase(listOf(withBalance("a", "0")), emptyList(), payday, BigDecimal.ZERO)!!

        assertEquals(BigDecimal.ZERO, result.yoursShare)
    }

    @Test
    fun `Should be null When there are no accounts`() {
        assertNull(useCase(emptyList(), emptyList(), payday, BigDecimal.ZERO))
    }

    @Test
    fun `Should be null When no current account has a balance`() {
        assertNull(useCase(listOf(AccountWithBalance(account("a"), null)), emptyList(), payday, BigDecimal.ZERO))
    }
}
