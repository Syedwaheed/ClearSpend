package com.edu.core.presentation.ui.mapper

import com.edu.core.domain.model.Balance
import com.edu.core.presentation.ui.formatter.AmountFormatterImpl
import com.edu.core.presentation.ui.formatter.DateFormatterImpl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.Locale

class BalanceUiMapperImplTest {

    private val amountFormatter = AmountFormatterImpl(Locale.UK)
    private val dateFormatter = DateFormatterImpl(
        Locale.UK,
        Clock.fixed(Instant.parse("2026-09-16T00:00:00Z"), ZoneOffset.UTC),
    )
    private val mapper = BalanceUiMapperImpl(amountFormatter, dateFormatter)

    @Test
    fun `map formats available and current and marks no overdraft`() {
        val balance = Balance(
            accountId = "acc-1",
            available = BigDecimal("1234.56"),
            current = BigDecimal("1200.00"),
            overdraft = BigDecimal.ZERO,
            updateTimestamp = Instant.parse("2026-09-16T10:00:00Z"),
        )

        val result = mapper.map(balance)

        assertEquals("£1,234.56", result.formattedAvailable)
        assertEquals("£1,200.00", result.formattedCurrent)
        assertFalse(result.hasOverdraft)
        assertNull(result.formattedOverdraft)
        assertEquals("16 Sep 2026", result.formattedUpdateDate)
    }

    @Test
    fun `map formats overdraft When present and available is negative`() {
        val balance = Balance(
            accountId = "acc-1",
            available = BigDecimal("-50.00"),
            current = BigDecimal("-50.00"),
            overdraft = BigDecimal("500.00"),
            updateTimestamp = Instant.parse("2026-09-16T10:00:00Z"),
        )

        val result = mapper.map(balance)

        assertTrue(result.hasOverdraft)
        assertEquals("£500.00", result.formattedOverdraft)
        assertEquals("-£50.00", result.formattedAvailable)
    }
}
