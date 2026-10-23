package com.edu.core.presentation.ui.mapper

import com.edu.core.domain.model.Account
import com.edu.core.domain.model.AccountNumber
import com.edu.core.domain.model.AccountType
import com.edu.core.domain.model.Provider
import com.edu.core.presentation.ui.formatter.IbanFormatterImpl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AccountUiMapperImplTest {

    private val mapper = AccountUiMapperImpl(IbanFormatterImpl())

    @Test
    fun `map masks iban and account number and formats sort code for a transaction account`() {
        val account = Account(
            accountId = "acc-1",
            accountType = AccountType.TRANSACTION,
            displayName = "Everyday account",
            currency = "GBP",
            provider = Provider(
                providerId = "mock",
                displayName = "Mock Bank",
                logoUri = "https://example.com/logo.png",
            ),
            accountNumber = AccountNumber(
                iban = "GB29 NWBK 6016 1331 9268 6789",
                sortCode = "123456",
                number = "16131926",
            ),
        )

        val result = mapper.map(account)

        assertEquals("acc-1", result.accountId)
        assertEquals("Everyday account", result.displayName)
        assertEquals("•••• 6789", result.maskedIban)
        assertEquals("•••• 1926", result.maskedAccountNumber)
        assertEquals("12-34-56", result.formattedSortCode)
        assertEquals("CURRENT", result.accountTypeBadge)
    }

    @Test
    fun `map returns null masked iban When account has no iban`() {
        val account = Account(
            accountId = "acc-2",
            accountType = AccountType.SAVINGS,
            displayName = "Savings",
            currency = "GBP",
            provider = Provider(providerId = "mock", displayName = "Mock Bank"),
            accountNumber = AccountNumber(sortCode = "123456", number = "16131926"),
        )

        val result = mapper.map(account)

        assertNull(result.maskedIban)
        assertEquals("SAVINGS", result.accountTypeBadge)
    }

    @Test
    fun `map assigns business badges`() {
        val businessTransaction = Account(
            accountId = "acc-3",
            accountType = AccountType.BUSINESS_TRANSACTION,
            displayName = "Business current",
            currency = "GBP",
            provider = Provider(providerId = "mock", displayName = "Mock Bank"),
            accountNumber = AccountNumber(),
        )
        val businessSavings = businessTransaction.copy(
            accountId = "acc-4",
            accountType = AccountType.BUSINESS_SAVINGS,
        )

        assertEquals("BUSINESS", mapper.map(businessTransaction).accountTypeBadge)
        assertEquals("BUS. SAVINGS", mapper.map(businessSavings).accountTypeBadge)
    }

    @Test
    fun `map picks the account type icon`() {
        val account = Account(
            accountId = "acc-5",
            accountType = AccountType.BUSINESS_TRANSACTION,
            displayName = "Business current",
            provider = Provider(providerId = "mock", displayName = "Mock Bank"),
            accountNumber = AccountNumber(),
        )

        assertEquals("briefcase", mapper.map(account).accountTypeIconId)
    }
}
