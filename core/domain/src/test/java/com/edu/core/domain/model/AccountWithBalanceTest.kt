package com.edu.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AccountWithBalanceTest {

    private fun account(id: String, type: AccountType) = Account(
        accountId = id,
        accountType = type,
        displayName = id,
        provider = Provider("p", "Bank"),
        accountNumber = AccountNumber(),
    )

    @Test
    fun `Should pick the first transaction account as primary`() {
        val accounts = listOf(account("s", AccountType.SAVINGS), account("t", AccountType.TRANSACTION))

        assertEquals("t", accounts.primaryAccount()?.accountId)
    }

    @Test
    fun `Should fall back to a business current account`() {
        val accounts = listOf(account("s", AccountType.SAVINGS), account("b", AccountType.BUSINESS_TRANSACTION))

        assertEquals("b", accounts.primaryAccount()?.accountId)
    }

    @Test
    fun `Should fall back to the first account When none are current`() {
        assertEquals("s", listOf(account("s", AccountType.SAVINGS)).primaryAccount()?.accountId)
    }

    @Test
    fun `Should have no primary account When there are no accounts`() {
        assertNull(emptyList<Account>().primaryAccount())
    }
}
