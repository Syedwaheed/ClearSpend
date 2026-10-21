package com.edu.feature.dashboard.domain.usecase

import com.edu.core.domain.model.AccountType
import com.edu.core.domain.model.UserInfo
import com.edu.core.domain.repository.AccountRepository
import com.edu.core.domain.repository.BalanceRepository
import com.edu.core.domain.repository.CardRepository
import com.edu.core.domain.repository.DirectDebitRepository
import com.edu.core.domain.repository.StandingOrderRepository
import com.edu.core.domain.repository.TransactionRepository
import com.edu.core.domain.repository.UserInfoRepository
import com.edu.core.domain.storage.SyncStatusStorage
import com.edu.core.domain.usecase.GetAccountsWithBalancesUseCase
import com.edu.core.domain.usecase.GetCardSummariesUseCase
import com.edu.core.domain.usecase.GetCardUtilisationUseCase
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.feature.dashboard.domain.fixture.CLOCK
import com.edu.feature.dashboard.domain.fixture.NOW
import com.edu.feature.dashboard.domain.fixture.TODAY
import com.edu.feature.dashboard.domain.fixture.account
import com.edu.feature.dashboard.domain.fixture.tx
import com.edu.feature.dashboard.domain.fixture.withBalance
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

class GetDashboardUseCaseTest {

    private val accountRepository: AccountRepository = mockk()
    private val balanceRepository: BalanceRepository = mockk()
    private val cardRepository: CardRepository = mockk()
    private val transactionRepository: TransactionRepository = mockk()
    private val directDebitRepository: DirectDebitRepository = mockk()
    private val standingOrderRepository: StandingOrderRepository = mockk()
    private val userInfoRepository: UserInfoRepository = mockk()
    private val syncStatus: SyncStatusStorage = mockk()

    private val useCase = GetDashboardUseCase(
        getAccountsWithBalances = GetAccountsWithBalancesUseCase(accountRepository, balanceRepository),
        getCardSummaries = GetCardSummariesUseCase(cardRepository, GetCardUtilisationUseCase()),
        transactionRepository = transactionRepository,
        directDebitRepository = directDebitRepository,
        standingOrderRepository = standingOrderRepository,
        userInfoRepository = userInfoRepository,
        syncStatus = syncStatus,
        getGreeting = GetGreetingUseCase(CLOCK),
        getPayday = GetPaydayUseCase(CLOCK),
        getUpcomingPayments = GetUpcomingPaymentsUseCase(CLOCK),
        getSafeToSpend = GetSafeToSpendUseCase(CLOCK),
        getMonthlyCashFlow = GetMonthlyCashFlowUseCase(CLOCK),
        getPendingSummary = GetPendingSummaryUseCase(),
        getSpendingByCategory = GetSpendingByCategoryUseCase(CLOCK),
        getRecentTransactions = GetRecentTransactionsUseCase(),
        clock = CLOCK,
    )

    @Before
    fun setUp() {
        val savings = account("sav", AccountType.SAVINGS)
        val current = account("cur")
        coEvery { accountRepository.getAccounts() } returns Result.Success(listOf(savings, current))
        coEvery { balanceRepository.getBalance("sav") } returns Result.Success(withBalance("sav", "2150").balance!!)
        coEvery { balanceRepository.getBalance("cur") } returns Result.Success(withBalance("cur", "44.01", "100").balance!!)
        coEvery { transactionRepository.getTransactions("cur", any(), any()) } returns
            Result.Success(listOf(tx("t1", "2026-09-15", "-46.82"), tx("t2", "2026-09-14", "420.00")))
        coEvery { transactionRepository.getPendingTransactions("cur") } returns
            Result.Success(listOf(tx("p", "2026-09-16", "-19.55", pending = true)))
        coEvery { directDebitRepository.getDirectDebits("cur") } returns Result.Success(emptyList())
        coEvery { standingOrderRepository.getStandingOrders("cur") } returns Result.Success(emptyList())
        coEvery { cardRepository.getCards() } returns Result.Success(emptyList())
        coEvery { userInfoRepository.getUserInfo() } returns Result.Success(UserInfo("Waheed Shah"))
        coEvery { syncStatus.getOfflineSince() } returns null
    }

    private suspend fun dashboard() = (useCase() as Result.Success).data

    @Test
    fun `Should use the first current account as primary`() = runTest {
        assertEquals("cur", dashboard().primaryAccount?.accountId)
    }

    @Test
    fun `Should show the primary account's balance`() = runTest {
        assertEquals(BigDecimal("44.01"), dashboard().primaryBalance?.available)
    }

    @Test
    fun `Should fetch 90 days of primary account history`() = runTest {
        dashboard()

        coVerify { transactionRepository.getTransactions("cur", TODAY.minusDays(90), TODAY) }
    }

    @Test
    fun `Should greet by first name`() = runTest {
        assertEquals("Waheed", dashboard().greeting.firstName)
    }

    @Test
    fun `Should summarise pending transactions`() = runTest {
        assertEquals(BigDecimal("19.55"), dashboard().pending.total)
    }

    @Test
    fun `Should list recent transactions newest first`() = runTest {
        assertEquals(listOf("t1", "t2"), dashboard().recentTransactions.map { it.transactionId })
    }

    @Test
    fun `Should still load When the name is unavailable`() = runTest {
        coEvery { userInfoRepository.getUserInfo() } returns Result.Error(DataError.NetworkError.SERVER_ERROR)

        assertNull(dashboard().greeting.firstName)
    }

    @Test
    fun `Should have no card When the user has no cards`() = runTest {
        assertNull(dashboard().card)
    }

    @Test
    fun `Should leave cash flow empty When transactions fail`() = runTest {
        coEvery { transactionRepository.getTransactions(any(), any(), any()) } returns Result.Error(DataError.NetworkError.SERVER_ERROR)

        assertNull(dashboard().cashFlow)
    }

    @Test
    fun `Should fail with unauthorized When a secondary call is unauthorized`() = runTest {
        coEvery { directDebitRepository.getDirectDebits(any()) } returns Result.Error(DataError.NetworkError.UNAUTHORIZED)

        assertEquals(Result.Error(DataError.NetworkError.UNAUTHORIZED), useCase())
    }

    @Test
    fun `Should fail When accounts fail`() = runTest {
        coEvery { accountRepository.getAccounts() } returns Result.Error(DataError.NetworkError.NO_INTERNET)

        assertEquals(Result.Error(DataError.NetworkError.NO_INTERNET), useCase())
    }

    @Test
    fun `Should return an empty dashboard When there are no accounts`() = runTest {
        coEvery { accountRepository.getAccounts() } returns Result.Success(emptyList())

        assertTrue(dashboard().accounts.isEmpty())
    }

    @Test
    fun `Should report offline data`() = runTest {
        coEvery { syncStatus.getOfflineSince() } returns NOW

        assertEquals(NOW, dashboard().offlineSince)
    }
}
