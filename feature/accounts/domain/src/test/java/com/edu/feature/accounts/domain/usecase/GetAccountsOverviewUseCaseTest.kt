package com.edu.feature.accounts.domain.usecase

import com.edu.core.domain.model.AccountType
import com.edu.core.domain.repository.AccountRepository
import com.edu.core.domain.repository.BalanceRepository
import com.edu.core.domain.repository.CardRepository
import com.edu.core.domain.repository.TransactionRepository
import com.edu.core.domain.storage.SyncStatusStorage
import com.edu.core.domain.usecase.GetAccountsWithBalancesUseCase
import com.edu.core.domain.usecase.GetCardSummariesUseCase
import com.edu.core.domain.usecase.GetCardUtilisationUseCase
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.feature.accounts.domain.fixture.CLOCK
import com.edu.feature.accounts.domain.fixture.FakeUserPreferencesStorage
import com.edu.feature.accounts.domain.fixture.TODAY
import com.edu.feature.accounts.domain.fixture.account
import com.edu.feature.accounts.domain.fixture.balance
import com.edu.feature.accounts.domain.fixture.tx
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class GetAccountsOverviewUseCaseTest {

    private val accountRepository: AccountRepository = mockk()
    private val balanceRepository: BalanceRepository = mockk()
    private val cardRepository: CardRepository = mockk()
    private val transactionRepository: TransactionRepository = mockk()
    private val syncStatus: SyncStatusStorage = mockk()

    private val useCase = GetAccountsOverviewUseCase(
        getAccountsWithBalances = GetAccountsWithBalancesUseCase(accountRepository, balanceRepository),
        getCardSummaries = GetCardSummariesUseCase(cardRepository, GetCardUtilisationUseCase()),
        transactionRepository = transactionRepository,
        getNetPosition = GetNetPositionUseCase(),
        getBalanceHistory = GetBalanceHistoryUseCase(CLOCK),
        getConsentDaysRemaining = GetConsentDaysRemainingUseCase(FakeUserPreferencesStorage(TODAY.minusDays(22).toEpochDay()), CLOCK),
        syncStatus = syncStatus,
        clock = CLOCK,
    )

    @Before
    fun setUp() {
        coEvery { accountRepository.getAccounts() } returns Result.Success(
            listOf(account("a", provider = "monzo"), account("s", AccountType.SAVINGS, "monzo"), account("b", AccountType.BUSINESS_TRANSACTION, "starling")),
        )
        coEvery { balanceRepository.getBalance(any()) } answers { Result.Success(balance(firstArg(), "100")) }
        coEvery { transactionRepository.getTransactions(any(), any(), any()) } returns Result.Success(listOf(tx("t", "2026-09-10", "-20")))
        coEvery { cardRepository.getCards() } returns Result.Success(emptyList())
        coEvery { syncStatus.getLastSyncedAt() } returns null
        coEvery { syncStatus.getOfflineSince() } returns null
    }

    private suspend fun overview() = (useCase() as Result.Success).data

    @Test
    fun `Should group accounts by provider in API order`() = runTest {
        assertEquals(listOf("Monzo", "Starling"), overview().groups.map { it.provider.displayName })
    }

    @Test
    fun `Should subtotal each provider`() = runTest {
        assertEquals(BigDecimal("200"), overview().groups.first().subtotal)
    }

    @Test
    fun `Should attach each account's 30-day change`() = runTest {
        assertEquals(BigDecimal("-20"), overview().groups.first().accounts.first().thirtyDayChange)
    }

    @Test
    fun `Should fetch eight weeks of history per account`() = runTest {
        overview()

        coVerify { transactionRepository.getTransactions("b", TODAY.minusDays(56), TODAY) }
    }

    @Test
    fun `Should compute consent days remaining`() = runTest {
        assertEquals(68, overview().consentDaysRemaining)
    }

    @Test
    fun `Should still load When an account's history fails`() = runTest {
        coEvery { transactionRepository.getTransactions("a", any(), any()) } returns Result.Error(DataError.NetworkError.SERVER_ERROR)

        assertNull(overview().groups.first().accounts.first().thirtyDayChange)
    }

    @Test
    fun `Should return no groups When there are no accounts`() = runTest {
        coEvery { accountRepository.getAccounts() } returns Result.Success(emptyList())

        assertTrue(overview().groups.isEmpty())
    }

    @Test
    fun `Should fail with unauthorized When cards are unauthorized`() = runTest {
        coEvery { cardRepository.getCards() } returns Result.Error(DataError.NetworkError.UNAUTHORIZED)

        assertEquals(Result.Error(DataError.NetworkError.UNAUTHORIZED), useCase())
    }

    @Test
    fun `Should fail When accounts fail`() = runTest {
        coEvery { accountRepository.getAccounts() } returns Result.Error(DataError.NetworkError.NO_INTERNET)

        assertEquals(Result.Error(DataError.NetworkError.NO_INTERNET), useCase())
    }
}
