package com.edu.feature.accounts.domain.usecase

import com.edu.core.domain.model.AccountType
import com.edu.core.domain.model.AccountWithBalance
import com.edu.feature.accounts.domain.fixture.account
import com.edu.feature.accounts.domain.fixture.cardSummary
import com.edu.feature.accounts.domain.fixture.withBalance
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class GetNetPositionUseCaseTest {

    private val useCase = GetNetPositionUseCase()

    private val accounts = listOf(
        withBalance("cur", "44.01"),
        withBalance("sav", "2150.00", AccountType.SAVINGS),
        withBalance("biz", "3280.75", AccountType.BUSINESS_TRANSACTION),
    )

    @Test
    fun `Should subtract card debt from all available balances`() {
        assertEquals(BigDecimal("3894.76"), useCase(accounts, listOf(cardSummary("c", "1580.00"))).total)
    }

    @Test
    fun `Should count business current accounts as current`() {
        assertEquals(BigDecimal("3324.76"), useCase(accounts, emptyList()).current)
    }

    @Test
    fun `Should total savings`() {
        assertEquals(BigDecimal("2150.00"), useCase(accounts, emptyList()).savings)
    }

    @Test
    fun `Should count business savings as savings`() {
        assertEquals(BigDecimal("10"), useCase(listOf(withBalance("bs", "10", AccountType.BUSINESS_SAVINGS)), emptyList()).savings)
    }

    @Test
    fun `Should owe nothing When there are no cards`() {
        assertEquals(BigDecimal.ZERO, useCase(accounts, emptyList()).cardOwed)
    }

    @Test
    fun `Should not treat a card in credit as owed`() {
        assertEquals(BigDecimal.ZERO, useCase(accounts, listOf(cardSummary("c", "-20"))).cardOwed)
    }

    @Test
    fun `Should be zero When there are no accounts`() {
        assertEquals(BigDecimal.ZERO, useCase(emptyList(), emptyList()).total)
    }

    @Test
    fun `Should skip an account with no balance`() {
        assertEquals(BigDecimal.ZERO, useCase(listOf(AccountWithBalance(account("x"), null)), emptyList()).total)
    }

    @Test
    fun `Should count accounts`() {
        assertEquals(3, useCase(accounts, emptyList()).accountCount)
    }
}
