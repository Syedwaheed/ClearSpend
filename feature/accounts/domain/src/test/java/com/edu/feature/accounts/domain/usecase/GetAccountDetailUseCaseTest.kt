package com.edu.feature.accounts.domain.usecase

import com.edu.core.domain.repository.AccountRepository
import com.edu.core.domain.repository.BalanceRepository
import com.edu.core.domain.repository.TransactionRepository
import com.edu.core.domain.storage.SyncStatusStorage
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.feature.accounts.domain.fixture.CLOCK
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
import org.junit.Before
import org.junit.Test

class GetAccountDetailUseCaseTest {

    private val accountRepository: AccountRepository = mockk()
    private val balanceRepository: BalanceRepository = mockk()
    private val transactionRepository: TransactionRepository = mockk()
    private val syncStatus: SyncStatusStorage = mockk()
    private val useCase = GetAccountDetailUseCase(accountRepository, balanceRepository, transactionRepository, syncStatus, CLOCK)

    @Before
    fun setUp() {
        coEvery { accountRepository.getAccount("a") } returns Result.Success(account("a"))
        coEvery { balanceRepository.getBalance("a") } returns Result.Success(balance("a", "44.01"))
        coEvery { transactionRepository.getTransactions("a", any(), any()) } returns Result.Success(
            (1..5).map { tx("t$it", "2026-09-0$it", "-1") } + tx("p", "2026-09-15", "-9", pending = true),
        )
        coEvery { syncStatus.getOfflineSince() } returns null
    }

    @Test
    fun `Should keep the three newest settled transactions`() = runTest {
        assertEquals(listOf("t5", "t4", "t3"), (useCase("a") as Result.Success).data.recentTransactions.map { it.transactionId })
    }

    @Test
    fun `Should fetch the last 30 days`() = runTest {
        useCase("a")

        coVerify { transactionRepository.getTransactions("a", TODAY.minusDays(30), TODAY) }
    }

    @Test
    fun `Should still show the account When the balance fails`() = runTest {
        coEvery { balanceRepository.getBalance("a") } returns Result.Error(DataError.NetworkError.SERVER_ERROR)

        assertNull((useCase("a") as Result.Success).data.balance)
    }

    @Test
    fun `Should fail When the account is not found`() = runTest {
        coEvery { accountRepository.getAccount("a") } returns Result.Error(DataError.NetworkError.NOT_FOUND)

        assertEquals(Result.Error(DataError.NetworkError.NOT_FOUND), useCase("a"))
    }

    @Test
    fun `Should fail with unauthorized When transactions are unauthorized`() = runTest {
        coEvery { transactionRepository.getTransactions("a", any(), any()) } returns Result.Error(DataError.NetworkError.UNAUTHORIZED)

        assertEquals(Result.Error(DataError.NetworkError.UNAUTHORIZED), useCase("a"))
    }
}
