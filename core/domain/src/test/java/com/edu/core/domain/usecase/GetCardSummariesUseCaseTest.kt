package com.edu.core.domain.usecase

import com.edu.core.domain.fixture.card
import com.edu.core.domain.fixture.cardBalance
import com.edu.core.domain.repository.CardRepository
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetCardSummariesUseCaseTest {

    private val cardRepository: CardRepository = mockk()
    private val useCase = GetCardSummariesUseCase(cardRepository, GetCardUtilisationUseCase())

    @Test
    fun `Should compute utilisation for each card`() = runTest {
        coEvery { cardRepository.getCards() } returns Result.Success(listOf(card("c")))
        coEvery { cardRepository.getCardBalance("c") } returns Result.Success(cardBalance("c", "250", "1000"))

        assertEquals(25, (useCase() as Result.Success).data.single().utilisation?.percent)
    }

    @Test
    fun `Should return no cards When the user has none`() = runTest {
        coEvery { cardRepository.getCards() } returns Result.Success(emptyList())

        assertEquals(Result.Success(emptyList<Any>()), useCase())
    }

    @Test
    fun `Should keep the card without utilisation When its balance fails`() = runTest {
        coEvery { cardRepository.getCards() } returns Result.Success(listOf(card("c")))
        coEvery { cardRepository.getCardBalance("c") } returns Result.Error(DataError.NetworkError.SERVER_ERROR)

        assertNull((useCase() as Result.Success).data.single().utilisation)
    }

    @Test
    fun `Should fail with unauthorized When a card balance is unauthorized`() = runTest {
        coEvery { cardRepository.getCards() } returns Result.Success(listOf(card("c")))
        coEvery { cardRepository.getCardBalance("c") } returns Result.Error(DataError.NetworkError.UNAUTHORIZED)

        assertEquals(Result.Error(DataError.NetworkError.UNAUTHORIZED), useCase())
    }
}
