package com.edu.feature.accounts.domain.usecase

import com.edu.core.domain.repository.CardRepository
import com.edu.core.domain.storage.SyncStatusStorage
import com.edu.core.domain.usecase.GetCardSummariesUseCase
import com.edu.core.domain.usecase.GetCardUtilisationUseCase
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.feature.accounts.domain.fixture.cardSummary
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GetPrimaryCardUseCaseTest {

    private val cardRepository: CardRepository = mockk()
    private val syncStatus: SyncStatusStorage = mockk()
    private val useCase = GetPrimaryCardUseCase(GetCardSummariesUseCase(cardRepository, GetCardUtilisationUseCase()), syncStatus)

    @Before
    fun setUp() {
        coEvery { syncStatus.getOfflineSince() } returns null
    }

    @Test
    fun `Should prefer the credit card`() = runTest {
        val debit = cardSummary("debit", "0", type = "DEBIT")
        val credit = cardSummary("credit", "1580")
        coEvery { cardRepository.getCards() } returns Result.Success(listOf(debit.card, credit.card))
        coEvery { cardRepository.getCardBalance(any()) } answers { Result.Success(cardSummary(firstArg(), "10").balance!!) }

        assertEquals("credit", (useCase() as Result.Success).data.summary?.card?.cardId)
    }

    @Test
    fun `Should compute utilisation`() = runTest {
        val credit = cardSummary("credit", "1580")
        coEvery { cardRepository.getCards() } returns Result.Success(listOf(credit.card))
        coEvery { cardRepository.getCardBalance("credit") } returns Result.Success(credit.balance!!)

        assertEquals(32, (useCase() as Result.Success).data.summary?.utilisation?.percent)
    }

    @Test
    fun `Should have no card When the user has none`() = runTest {
        coEvery { cardRepository.getCards() } returns Result.Success(emptyList())

        assertNull((useCase() as Result.Success).data.summary)
    }

    @Test
    fun `Should fail When cards fail`() = runTest {
        coEvery { cardRepository.getCards() } returns Result.Error(DataError.NetworkError.NO_INTERNET)

        assertEquals(Result.Error(DataError.NetworkError.NO_INTERNET), useCase())
    }
}
