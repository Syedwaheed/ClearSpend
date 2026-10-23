package com.edu.core.domain.usecase

import com.edu.core.domain.fixture.account
import com.edu.core.domain.fixture.balance
import com.edu.core.domain.repository.AccountRepository
import com.edu.core.domain.repository.BalanceRepository
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetAccountsWithBalancesUseCaseTest {

    private val accountRepository: AccountRepository = mockk()
    private val balanceRepository: BalanceRepository = mockk()
    private val useCase = GetAccountsWithBalancesUseCase(accountRepository, balanceRepository)

    @Test
    fun `Should pair each account with its balance`() = runTest {
        coEvery { accountRepository.getAccounts() } returns Result.Success(listOf(account("a"), account("b")))
        coEvery { balanceRepository.getBalance(any()) } answers { Result.Success(balance(firstArg())) }

        val result = useCase() as Result.Success

        assertEquals(listOf("a", "b"), result.data.map { it.balance?.accountId })
    }

    @Test
    fun `Should return no accounts When the bank has none`() = runTest {
        coEvery { accountRepository.getAccounts() } returns Result.Success(emptyList())

        assertEquals(Result.Success(emptyList<Any>()), useCase())
    }

    @Test
    fun `Should keep the account When its balance fails`() = runTest {
        coEvery { accountRepository.getAccounts() } returns Result.Success(listOf(account("a")))
        coEvery { balanceRepository.getBalance("a") } returns Result.Error(DataError.NetworkError.SERVER_ERROR)

        assertNull((useCase() as Result.Success).data.single().balance)
    }

    @Test
    fun `Should fail with unauthorized When a balance call is unauthorized`() = runTest {
        coEvery { accountRepository.getAccounts() } returns Result.Success(listOf(account("a")))
        coEvery { balanceRepository.getBalance("a") } returns Result.Error(DataError.NetworkError.UNAUTHORIZED)

        assertEquals(Result.Error(DataError.NetworkError.UNAUTHORIZED), useCase())
    }

    @Test
    fun `Should fail When accounts fail`() = runTest {
        coEvery { accountRepository.getAccounts() } returns Result.Error(DataError.NetworkError.NO_INTERNET)

        assertEquals(Result.Error(DataError.NetworkError.NO_INTERNET), useCase())
    }
}
