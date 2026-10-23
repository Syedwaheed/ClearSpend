package com.edu.feature.transactions.domain.usecase

import com.edu.core.domain.model.TransactionCategory
import com.edu.feature.transactions.domain.fixture.tx
import com.edu.feature.transactions.domain.model.CategoryFilter
import org.junit.Assert.assertEquals
import org.junit.Test

class GetFilteredTransactionsUseCaseTest {

    private val useCase = GetFilteredTransactionsUseCase()

    private val tesco = tx("tesco", merchant = "Tesco", description = "TESCO STORES 3297")
    private val edf = tx("edf", merchant = "EDF Energy", description = "EDF DD", category = TransactionCategory.DIRECT_DEBIT)
    private val noMerchant = tx("rent", merchant = null, description = "RENT JUNE", category = TransactionCategory.TRANSFER)
    private val atm = tx("atm", merchant = null, description = "LINK ATM", category = TransactionCategory.ATM)
    private val cash = tx("cash", merchant = "TSB Cleveleys", amount = "420.00", category = TransactionCategory.CASH)
    private val salary = tx("salary", merchant = "Acme Ltd", amount = "1450.42", category = TransactionCategory.CREDIT)
    private val all = listOf(tesco, edf, noMerchant, atm, cash, salary)

    @Test
    fun `Should keep everything When the query is blank and the category is All`() {
        assertEquals(all, useCase(all, "  ", CategoryFilter.ALL))
    }

    @Test
    fun `Should match the merchant name ignoring case`() {
        assertEquals(listOf(edf), useCase(all, "edf energy", CategoryFilter.ALL))
    }

    @Test
    fun `Should match the description`() {
        assertEquals(listOf(noMerchant), useCase(all, "rent", CategoryFilter.ALL))
    }

    @Test
    fun `Should match the description When a merchant name is also present`() {
        assertEquals(listOf(tesco), useCase(all, "3297", CategoryFilter.ALL))
    }

    @Test
    fun `Should ignore surrounding whitespace in the query`() {
        assertEquals(listOf(tesco), useCase(all, "  tesco ", CategoryFilter.ALL))
    }

    @Test
    fun `Should return nothing When no row matches`() {
        assertEquals(emptyList<Any>(), useCase(all, "zzzz", CategoryFilter.ALL))
    }

    @Test
    fun `Should keep only the selected category`() {
        assertEquals(listOf(edf), useCase(all, "", CategoryFilter.DIRECT_DEBIT))
    }

    @Test
    fun `Should include ATM withdrawals in the Cash chip`() {
        assertEquals(listOf(atm, cash), useCase(all, "", CategoryFilter.CASH))
    }

    @Test
    fun `Should keep credits in the Income chip`() {
        assertEquals(listOf(salary), useCase(all, "", CategoryFilter.CREDIT))
    }

    @Test
    fun `Should combine the search and the category`() {
        assertEquals(emptyList<Any>(), useCase(all, "tesco", CategoryFilter.TRANSFER))
    }
}
