package com.edu.core.presentation.ui.mapper

import com.edu.core.domain.model.Transaction
import com.edu.core.domain.model.TransactionCategory
import com.edu.core.domain.model.TransactionType
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

class TransactionUiMapperImplTest {

    private val amountFormatter = AmountFormatterImpl(Locale.UK)
    private val dateFormatter = DateFormatterImpl(
        Locale.UK,
        Clock.fixed(Instant.parse("2026-09-16T00:00:00Z"), ZoneOffset.UTC),
    )
    private val mapper = TransactionUiMapperImpl(amountFormatter, dateFormatter)

    @Test
    fun `map formats a debit purchase with negative amount and classification subtitle`() {
        val transaction = Transaction(
            transactionId = "tx-1",
            accountId = "acc-1",
            timestamp = Instant.parse("2026-09-16T14:32:00Z"),
            description = "TESCO STORES",
            merchantName = "Tesco",
            amount = BigDecimal("-46.82"),
            transactionType = TransactionType.DEBIT,
            transactionCategory = TransactionCategory.PURCHASE,
            classification = listOf("Shopping", "Groceries"),
            runningBalance = BigDecimal("1200.00"),
            isPending = false,
        )

        val result = mapper.map(transaction)

        assertEquals("Tesco", result.title)
        assertEquals("-£46.82", result.formattedAmount)
        assertTrue(result.isDebit)
        assertEquals("Groceries · 14:32 · Card", result.subtitle)
        assertEquals("£1,200.00", result.formattedRunningBalance)
        assertFalse(result.isPending)
        assertEquals("shopping_cart", result.categoryIconId)
    }

    @Test
    fun `map formats a credit with signed positive amount and falls back to description`() {
        val transaction = Transaction(
            transactionId = "tx-2",
            accountId = "acc-1",
            timestamp = Instant.parse("2026-09-16T09:00:00Z"),
            description = "SALARY PAYMENT",
            merchantName = null,
            amount = BigDecimal("2500.00"),
            transactionType = TransactionType.CREDIT,
            transactionCategory = TransactionCategory.CREDIT,
            classification = emptyList(),
            runningBalance = null,
            isPending = false,
        )

        val result = mapper.map(transaction)

        assertEquals("SALARY PAYMENT", result.title)
        assertEquals("+£2,500.00", result.formattedAmount)
        assertFalse(result.isDebit)
        assertEquals("09:00 · BACS", result.subtitle)
        assertNull(result.formattedRunningBalance)
    }

    @Test
    fun `map marks pending transactions`() {
        val transaction = Transaction(
            transactionId = "tx-3",
            accountId = "acc-1",
            timestamp = Instant.parse("2026-09-16T09:00:00Z"),
            description = "Card payment",
            amount = BigDecimal("-10.00"),
            transactionType = TransactionType.DEBIT,
            transactionCategory = TransactionCategory.PURCHASE,
            isPending = true,
        )

        assertTrue(mapper.map(transaction).isPending)
    }
}
