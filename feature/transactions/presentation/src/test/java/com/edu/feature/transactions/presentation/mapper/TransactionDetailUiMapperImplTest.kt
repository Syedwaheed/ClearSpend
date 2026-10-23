package com.edu.feature.transactions.presentation.mapper

import com.edu.core.domain.model.Transaction
import com.edu.core.domain.model.TransactionCategory
import com.edu.feature.transactions.domain.model.MerchantInsight
import com.edu.feature.transactions.domain.model.TransactionDetail
import com.edu.feature.transactions.presentation.fake.account
import com.edu.feature.transactions.presentation.fake.amountFormatter
import com.edu.feature.transactions.presentation.fake.dateFormatter
import com.edu.feature.transactions.presentation.fake.syncTimeMapper
import com.edu.feature.transactions.presentation.fake.transactionUiMapper
import com.edu.feature.transactions.presentation.fake.tx
import com.edu.feature.transactions.presentation.model.MerchantInsightUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.YearMonth

class TransactionDetailUiMapperImplTest {

    private val mapper = TransactionDetailUiMapperImpl(transactionUiMapper, amountFormatter, dateFormatter, syncTimeMapper)

    private fun detail(
        transaction: Transaction = tx("t1"),
        withAccount: Boolean = true,
        insight: MerchantInsight = MerchantInsight.FirstTime("Tesco", 90),
    ) = TransactionDetail(transaction, if (withAccount) account("acc-1") else null, insight, offlineSince = null)

    @Test
    fun `Should keep the minus sign on a debit`() {
        assertEquals("-£39.99", mapper.map(detail()).formattedAmount)
    }

    @Test
    fun `Should add a plus sign to a credit`() {
        assertEquals("+£420.00", mapper.map(detail(tx("c", amount = "420.00", category = TransactionCategory.CREDIT))).formattedAmount)
    }

    @Test
    fun `Should mark a credit`() {
        assertTrue(mapper.map(detail(tx("c", amount = "420.00", category = TransactionCategory.CREDIT))).isCredit)
    }

    @Test
    fun `Should show the date and 24h time`() {
        assertEquals("10 Jun 2026 · 06:00", mapper.map(detail()).dateTime)
    }

    @Test
    fun `Should name the account and its provider`() {
        assertEquals("Account acc-1 · Monzo", mapper.map(detail()).accountLabel)
    }

    @Test
    fun `Should leave the account empty When it could not be loaded`() {
        assertNull(mapper.map(detail(withAccount = false)).accountLabel)
    }

    @Test
    fun `Should use the provider reference When present`() {
        assertEquals("RENT JUNE", mapper.map(detail(tx("t", providerReference = "RENT JUNE"))).reference)
    }

    @Test
    fun `Should fall back to the description for the reference`() {
        assertEquals("TESCO STORES", mapper.map(detail()).reference)
    }

    @Test
    fun `Should format the balance after`() {
        assertEquals("£317.84", mapper.map(detail()).formattedBalanceAfter)
    }

    @Test
    fun `Should hide the balance after When the API omits it`() {
        assertNull(mapper.map(detail(tx("t", runningBalance = null))).formattedBalanceAfter)
    }

    @Test
    fun `Should hide the balance after for a pending transaction`() {
        assertNull(mapper.map(detail(tx("p", pending = true))).formattedBalanceAfter)
    }

    @Test
    fun `Should mark a pending transaction`() {
        assertTrue(mapper.map(detail(tx("p", pending = true))).isPending)
    }

    @Test
    fun `Should list the classifications`() {
        assertEquals(listOf("Shopping", "Groceries"), mapper.map(detail()).classifications)
    }

    @Test
    fun `Should fall back to the category label When there is no classification`() {
        assertEquals(listOf("Direct Debit"), mapper.map(detail(tx("d", category = TransactionCategory.DIRECT_DEBIT, classification = emptyList()))).classifications)
    }

    @Test
    fun `Should give the payment method`() {
        assertEquals("Debit card", mapper.map(detail()).paymentMethod)
    }

    @Test
    fun `Should offer Manage in Bills for a direct debit`() {
        assertTrue(mapper.map(detail(tx("d", category = TransactionCategory.DIRECT_DEBIT))).showManageInBills)
    }

    @Test
    fun `Should not offer Manage in Bills for a card payment`() {
        assertFalse(mapper.map(detail()).showManageInBills)
    }

    @Test
    fun `Should format the monthly merchant total`() {
        val insight = MerchantInsight.MonthlyTotal("Tesco", YearMonth.of(2026, 6), BigDecimal("156.99"), 8, isTopMerchant = true)

        assertEquals(MerchantInsightUi.MonthlyTotal("Tesco", "£156.99", 8, true), mapper.map(detail(insight = insight)).insight)
    }

    @Test
    fun `Should pass a first visit through`() {
        assertEquals(MerchantInsightUi.FirstTime("Tesco", 90), mapper.map(detail()).insight)
    }
}
