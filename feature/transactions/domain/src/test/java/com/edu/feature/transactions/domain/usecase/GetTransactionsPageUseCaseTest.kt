package com.edu.feature.transactions.domain.usecase

import com.edu.core.domain.repository.AccountRepository
import com.edu.core.domain.repository.TransactionRepository
import com.edu.core.domain.storage.SyncStatusStorage
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.feature.transactions.domain.fixture.NOW
import com.edu.feature.transactions.domain.fixture.account
import com.edu.feature.transactions.domain.fixture.tx
import com.edu.feature.transactions.domain.model.DateWindow
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class GetTransactionsPageUseCaseTest {

    private val accountRepository: AccountRepository = mockk()
    private val transactionRepository: TransactionRepository = mockk()
    private val syncStatus: SyncStatusStorage = mockk()
    private val useCase = GetTransactionsPageUseCase(accountRepository, transactionRepository, syncStatus)

    private val window = DateWindow(LocalDate.of(2026, 5, 17), LocalDate.of(2026, 6, 15))

    @Before
    fun setUp() {
        coEvery { accountRepository.getAccounts() } returns Result.Success(listOf(account("a"), account("b")))
        coEvery { transactionRepository.getTransactions("a", window.from, window.to) } returns
            Result.Success(listOf(tx("a2", "2026-06-10T10:00:00Z", accountId = "a"), tx("a1", "2026-06-01T10:00:00Z", accountId = "a")))
        coEvery { transactionRepository.getTransactions("b", window.from, window.to) } returns
            Result.Success(listOf(tx("b1", "2026-06-05T10:00:00Z", accountId = "b")))
        coEvery { transactionRepository.getPendingTransactions(any()) } returns Result.Success(emptyList())
        coEvery { syncStatus.getOfflineSince() } returns null
    }

    private suspend fun success(accountId: String? = null, includePending: Boolean = true) =
        (useCase(accountId, window, includePending) as Result.Success).data

    @Test
    fun `Should load only the selected account`() = runTest {
        assertEquals(listOf("a2", "a1"), success(accountId = "a").settled.map { it.transactionId })
    }

    @Test
    fun `Should merge every account newest first for All accounts`() = runTest {
        assertEquals(listOf("a2", "b1", "a1"), success().settled.map { it.transactionId })
    }

    @Test
    fun `Should return every account for the switcher`() = runTest {
        assertEquals(listOf("a", "b"), success(accountId = "a").accounts.map { it.accountId })
    }

    @Test
    fun `Should load pending rows When asked`() = runTest {
        coEvery { transactionRepository.getPendingTransactions("a") } returns
            Result.Success(listOf(tx("p1", "2026-06-15T08:00:00Z", pending = true, accountId = "a")))

        assertEquals(listOf("p1"), success(accountId = "a").pending.map { it.transactionId })
    }

    @Test
    fun `Should not call the pending endpoint for older pages`() = runTest {
        useCase("a", window, includePending = false)

        coVerify(exactly = 0) { transactionRepository.getPendingTransactions(any()) }
    }

    @Test
    fun `Should drop duplicate rows`() = runTest {
        coEvery { transactionRepository.getTransactions("b", window.from, window.to) } returns
            Result.Success(listOf(tx("a1", "2026-06-01T10:00:00Z", accountId = "a")))

        assertEquals(listOf("a2", "a1"), success().settled.map { it.transactionId })
    }

    @Test
    fun `Should fail When the accounts fail`() = runTest {
        coEvery { accountRepository.getAccounts() } returns Result.Error(DataError.NetworkError.NO_INTERNET)

        assertEquals(Result.Error(DataError.NetworkError.NO_INTERNET), useCase(null, window, true))
    }

    @Test
    fun `Should fail with unauthorized When any call is unauthorized`() = runTest {
        coEvery { transactionRepository.getPendingTransactions("b") } returns Result.Error(DataError.NetworkError.UNAUTHORIZED)

        assertEquals(Result.Error(DataError.NetworkError.UNAUTHORIZED), useCase(null, window, true))
    }

    @Test
    fun `Should fail When every account's transactions fail`() = runTest {
        coEvery { transactionRepository.getTransactions(any(), any(), any()) } returns Result.Error(DataError.NetworkError.SERVER_ERROR)

        assertEquals(Result.Error(DataError.NetworkError.SERVER_ERROR), useCase(null, window, true))
    }

    @Test
    fun `Should show the accounts that loaded When one account fails`() = runTest {
        coEvery { transactionRepository.getTransactions("b", any(), any()) } returns Result.Error(DataError.NetworkError.SERVER_ERROR)

        assertEquals(listOf("a2", "a1"), success().settled.map { it.transactionId })
    }

    @Test
    fun `Should hide only the pending section When pending fails`() = runTest {
        coEvery { transactionRepository.getPendingTransactions(any()) } returns Result.Error(DataError.NetworkError.SERVER_ERROR)

        assertTrue(success().pending.isEmpty())
    }

    @Test
    fun `Should return an empty page When there are no accounts`() = runTest {
        coEvery { accountRepository.getAccounts() } returns Result.Success(emptyList())

        assertTrue(success().settled.isEmpty())
    }

    @Test
    fun `Should report when the data came from the cache`() = runTest {
        coEvery { syncStatus.getOfflineSince() } returns NOW

        assertEquals(NOW, success().offlineSince)
    }
}
