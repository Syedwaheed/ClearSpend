package com.edu.feature.auth.domain.usecase

import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.feature.auth.domain.model.BankProvider
import com.edu.feature.auth.domain.repository.BankProviderRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchBanksUseCaseTest {

    private val barclays = BankProvider("ob-barclays", "Barclays")
    private val hsbc = BankProvider("ob-hsbc", "HSBC")
    private val repository: BankProviderRepository = mockk {
        coEvery { getProviders() } returns Result.Success(listOf(barclays, hsbc))
    }
    private val useCase = SearchBanksUseCase(repository)

    @Test
    fun `Should return every bank When query is blank`() = runTest {
        assertEquals(Result.Success(listOf(barclays, hsbc)), useCase("   "))
    }

    @Test
    fun `Should match case-insensitively on name`() = runTest {
        assertEquals(Result.Success(listOf(hsbc)), useCase(" hs "))
    }

    @Test
    fun `Should return empty list When nothing matches`() = runTest {
        assertEquals(Result.Success(emptyList<BankProvider>()), useCase("zzz"))
    }

    @Test
    fun `Should pass repository errors through`() = runTest {
        coEvery { repository.getProviders() } returns Result.Error(DataError.NetworkError.NO_INTERNET)

        assertEquals(Result.Error(DataError.NetworkError.NO_INTERNET), useCase("a"))
    }
}
