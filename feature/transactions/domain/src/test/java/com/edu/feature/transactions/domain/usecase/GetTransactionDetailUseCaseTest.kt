package com.edu.feature.transactions.domain.usecase

import com.edu.core.domain.repository.AccountRepository
import com.edu.core.domain.repository.TransactionRepository
import com.edu.core.domain.storage.SyncStatusStorage
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.feature.transactions.domain.fixture.CLOCK
import com.edu.feature.transactions.domain.fixture.TODAY
import com.edu.feature.transactions.domain.fixture.account
import com.edu.feature.transactions.domain.fixture.tx
import com.edu.feature.transactions.domain.model.MerchantInsight
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class GetTransactionDetailUseCaseTest {

    private val transactionRepository: TransactionRepository = mockk()
    private val accountRepository: AccountRepository = mockk()
    private val syncStatus: SyncStatusStorage = mockk()
    private val useCase = GetTransactionDetailUseCase(
        transactionRepository = transactionRepository,
        accountRepository = accountRepository,
        getMerchantInsight = GetMerchantInsightUseCase(CLOCK),
        syncStatus = syncStatus,
        clock = CLOCK,
    )

    private val tesco = tx("t1", "2026-06-10T06:00:00Z", "-39.99", merchant = "Tesco", accountId = "a")

    @Before
    fun setUp() {
        coEvery { transactionRepository.getTransaction("t1") } returns Result.Success(tesco)
        coEvery { accountRepository.getAccount("a") } returns Result.Success(account("a"))
        coEvery { transactionRepository.getTransactions("a", any(), any()) } returns
            Result.Success(listOf(tesco, tx("t2", "2026-06-02T06:00:00Z", "-117.00", merchant = "Tesco", accountId = "a")))
        coEvery { syncStatus.getOfflineSince() } returns null
    }

    private suspend fun detail() = (useCase("t1") as Result.Success).data

    @Test
    fun `Should load the transaction by id`() = runTest {
        assertEquals(tesco, detail().transaction)
    }

    @Test
    fun `Should fail When the transaction is not cached`() = runTest {
        coEvery { transactionRepository.getTransaction("t1") } returns Result.Error(DataError.NetworkError.NOT_FOUND)

        assertEquals(Result.Error(DataError.NetworkError.NOT_FOUND), useCase("t1"))
    }

    @Test
    fun `Should include the account it came from`() = runTest {
        assertEquals("a", detail().account?.accountId)
    }

    @Test
    fun `Should still show the transaction When the account fails`() = runTest {
        coEvery { accountRepository.getAccount("a") } returns Result.Error(DataError.NetworkError.SERVER_ERROR)

        assertNull(detail().account)
    }

    @Test
    fun `Should derive the merchant insight from the account history`() = runTest {
        assertEquals(2, (detail().insight as MerchantInsight.MonthlyTotal).count)
    }

    @Test
    fun `Should fall back to a first visit When the history fails`() = runTest {
        coEvery { transactionRepository.getTransactions("a", any(), any()) } returns Result.Error(DataError.NetworkError.NO_INTERNET)

        assertTrue(detail().insight is MerchantInsight.FirstTime)
    }

    @Test
    fun `Should fail with unauthorized When consent expired`() = runTest {
        coEvery { accountRepository.getAccount("a") } returns Result.Error(DataError.NetworkError.UNAUTHORIZED)

        assertEquals(Result.Error(DataError.NetworkError.UNAUTHORIZED), useCase("t1"))
    }

    @Test
    fun `Should fetch history from 90 days back up to today`() = runTest {
        useCase("t1")

        coVerify { transactionRepository.getTransactions("a", LocalDate.of(2026, 3, 12), TODAY) }
    }

    @Test
    fun `Should fetch the whole month for an older transaction`() = runTest {
        val march = tx("t1", "2026-03-20T06:00:00Z", merchant = "Tesco", accountId = "a")
        coEvery { transactionRepository.getTransaction("t1") } returns Result.Success(march)

        useCase("t1")

        coVerify { transactionRepository.getTransactions("a", LocalDate.of(2025, 12, 20), LocalDate.of(2026, 3, 31)) }
    }
}
