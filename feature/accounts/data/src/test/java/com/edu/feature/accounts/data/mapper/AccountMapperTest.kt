package com.edu.feature.accounts.data.mapper

import com.edu.core.domain.model.AccountType
import com.edu.feature.accounts.data.remote.dto.AccountDto
import com.edu.feature.accounts.data.remote.dto.AccountNumberDto
import com.edu.feature.accounts.data.remote.dto.ProviderDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AccountMapperTest {

    private val mapper = AccountMapper()

    private val dto = AccountDto(
        accountId = "acc-1",
        accountType = "BUSINESS_SAVINGS",
        displayName = "Savings",
        currency = "GBP",
        accountNumber = AccountNumberDto(iban = "GB00", number = "12345678", sortCode = "040668"),
        provider = ProviderDto("ob-monzo", "Monzo", "https://logo"),
    )

    @Test
    fun `Should map the account type`() {
        assertEquals(AccountType.BUSINESS_SAVINGS, mapper.toDomain(dto).accountType)
    }

    @Test
    fun `Should default an unknown account type to a current account`() {
        assertEquals(AccountType.TRANSACTION, mapper.toDomain(dto.copy(accountType = "CHARITY")).accountType)
    }

    @Test
    fun `Should map the account number`() {
        assertEquals("040668", mapper.toDomain(dto).accountNumber.sortCode)
    }

    @Test
    fun `Should default a missing display name`() {
        assertEquals("Account", mapper.toDomain(dto.copy(displayName = null)).displayName)
    }

    @Test
    fun `Should drop a blank logo uri`() {
        assertNull(mapper.toDomain(dto.copy(provider = ProviderDto("p", "Bank", " "))).provider.logoUri)
    }

    @Test
    fun `Should round trip through the entity`() {
        val domain = mapper.toDomain(dto)

        assertEquals(domain, mapper.fromEntity(mapper.toEntity(domain)))
    }
}
