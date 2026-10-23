package com.edu.core.presentation.ui.mapper

import com.edu.core.domain.model.Transaction
import com.edu.core.domain.model.TransactionCategory
import com.edu.core.domain.model.TransactionType
import androidx.compose.ui.graphics.toArgb
import com.edu.core.presentation.designsystem.ErrorOnWhite
import com.edu.core.presentation.designsystem.SuccessOnWhite
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
        assertEquals("T", result.initial)
        assertEquals("Shopping · Today", result.compactSubtitle)
    }

    @Test
    fun `map falls back to the category label When there is no classification`() {
        val transaction = Transaction(
            transactionId = "tx-4",
            accountId = "acc-1",
            timestamp = Instant.parse("2026-05-28T09:00:00Z"),
            description = "CASH WITHDRAWAL",
            amount = BigDecimal("-20.00"),
            transactionType = TransactionType.DEBIT,
            transactionCategory = TransactionCategory.CASH,
        )

        assertEquals("Cash · 28 May", mapper.map(transaction).compactSubtitle)
    }

    @Test
    fun `map uses the description When the merchant name is blank`() {
        val transaction = Transaction(
            transactionId = "tx-5",
            accountId = "acc-1",
            timestamp = Instant.parse("2026-09-15T09:00:00Z"),
            description = "Account Overdraft Fee",
            merchantName = " ",
            amount = BigDecimal("-0.01"),
            transactionType = TransactionType.DEBIT,
            transactionCategory = TransactionCategory.BILL_PAYMENT,
        )

        val result = mapper.map(transaction)

        assertEquals("Account Overdraft Fee", result.title)
        assertEquals("Yesterday", result.relativeDay)
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

    private fun transaction(
        amount: String = "-46.82",
        type: TransactionType = TransactionType.DEBIT,
        category: TransactionCategory = TransactionCategory.PURCHASE,
        runningBalance: BigDecimal? = BigDecimal("44.01"),
        pending: Boolean = false,
    ) = Transaction(
        transactionId = "tx",
        accountId = "acc-1",
        timestamp = Instant.parse("2026-09-16T14:32:00Z"),
        description = "TALKTALK",
        merchantName = "TalkTalk Telecom",
        amount = BigDecimal(amount),
        transactionType = type,
        transactionCategory = category,
        classification = listOf("Bills and Utilities", "Internet"),
        runningBalance = runningBalance,
        isPending = pending,
    )

    @Test
    fun `map keeps the minus sign on a negative amount`() {
        assertEquals("-£46.82", mapper.map(transaction()).formattedAmount)
    }

    @Test
    fun `map colours a debit red`() {
        assertEquals(ErrorOnWhite.toArgb(), mapper.map(transaction()).amountColorArgb)
    }

    @Test
    fun `map adds a plus sign to a credit`() {
        assertEquals("+£420.00", mapper.map(transaction("420.00", TransactionType.CREDIT, TransactionCategory.CREDIT)).formattedAmount)
    }

    @Test
    fun `map colours a credit green`() {
        val credit = transaction("420.00", TransactionType.CREDIT, TransactionCategory.CREDIT)

        assertEquals(SuccessOnWhite.toArgb(), mapper.map(credit).amountColorArgb)
    }

    @Test
    fun `map hides the running balance When it is null`() {
        assertNull(mapper.map(transaction(runningBalance = null)).formattedRunningBalance)
    }

    @Test
    fun `map formats a negative running balance`() {
        assertEquals("-£62.17", mapper.map(transaction(runningBalance = BigDecimal("-62.17"))).formattedRunningBalance)
    }

    @Test
    fun `map drops the time from a pending row subtitle`() {
        assertEquals("Internet · Card", mapper.map(transaction(pending = true)).subtitle)
    }

    @Test
    fun `map gives the long payment method label`() {
        assertEquals("Direct Debit", mapper.map(transaction(category = TransactionCategory.DIRECT_DEBIT)).paymentMethod)
    }

    @Test
    fun `map labels a credit as BACS`() {
        val credit = transaction("420.00", TransactionType.CREDIT, TransactionCategory.CREDIT)

        assertEquals("Bank credit (BACS)", mapper.map(credit).paymentMethod)
    }

    @Test
    fun `map gives the category label`() {
        assertEquals("Shopping", mapper.map(transaction()).categoryLabel)
    }
}
