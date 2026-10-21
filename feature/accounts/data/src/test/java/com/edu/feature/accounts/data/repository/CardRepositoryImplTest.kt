package com.edu.feature.accounts.data.repository

import com.edu.core.database.dao.CardBalanceDao
import com.edu.core.database.dao.CardDao
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.feature.accounts.data.fake.FakeSyncStatusStorage
import com.edu.feature.accounts.data.fake.json
import com.edu.feature.accounts.data.fake.mockDataClient
import com.edu.feature.accounts.data.remote.TrueLayerCardsApi
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException
import java.math.BigDecimal
import java.time.LocalDate

class CardRepositoryImplTest {

    private val cardDao: CardDao = mockk(relaxed = true)
    private val cardBalanceDao: CardBalanceDao = mockk(relaxed = true)
    private var requestedPath: String? = null

    private fun repository(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) =
        CardRepositoryImpl(
            api = TrueLayerCardsApi(mockDataClient { request -> requestedPath = request.url.encodedPath; handler(request) }),
            cardDao = cardDao,
            cardBalanceDao = cardBalanceDao,
            syncStatus = FakeSyncStatusStorage(),
        )

    private val cardsJson = """
        {"results":[{"account_id":"card-1","card_network":"MASTERCARD","card_type":"CREDIT","currency":"GBP",
          "display_name":"ClearSpend Rewards","partial_card_number":"4471","name_on_card":"J Smith",
          "provider":{"display_name":"Mock","provider_id":"mock"}}]}
    """.trimIndent()

    private val cardBalanceJson = """
        {"results":[{"available":3420.0,"currency":"GBP","current":1580.0,"credit_limit":5000,
          "last_statement_balance":1850.0,"payment_due":210.0,"payment_due_date":"2026-09-28T00:00:00+00:00"}]}
    """.trimIndent()

    @Test
    fun `Should call the cards endpoint`() = runTest {
        repository { json(cardsJson) }.getCards()

        assertEquals("/data/v1/cards", requestedPath)
    }

    @Test
    fun `Should use account_id as the card id`() = runTest {
        val result = repository { json(cardsJson) }.getCards() as Result.Success

        assertEquals("card-1", result.data.single().cardId)
    }

    @Test
    fun `Should cache cards When fetched`() = runTest {
        repository { json(cardsJson) }.getCards()

        coVerify { cardDao.replaceAll(match { it.single().cardId == "card-1" }) }
    }

    @Test
    fun `Should return no cards When the user has none`() = runTest {
        val result = repository { json("""{"results":[]}""") }.getCards()

        assertEquals(Result.Success(emptyList<Any>()), result)
    }

    @Test
    fun `Should call the card balance endpoint`() = runTest {
        repository { json(cardBalanceJson) }.getCardBalance("card-1")

        assertEquals("/data/v1/cards/card-1/balance", requestedPath)
    }

    @Test
    fun `Should parse the credit limit`() = runTest {
        val result = repository { json(cardBalanceJson) }.getCardBalance("card-1") as Result.Success

        assertEquals(BigDecimal("5000"), result.data.creditLimit)
    }

    @Test
    fun `Should parse the payment due date`() = runTest {
        val result = repository { json(cardBalanceJson) }.getCardBalance("card-1") as Result.Success

        assertEquals(LocalDate.of(2026, 9, 28), result.data.paymentDueDate)
    }

    @Test
    fun `Should return no internet When offline with nothing cached`() = runTest {
        coEvery { cardBalanceDao.getCardBalance(any()) } returns null

        val result = repository { throw IOException("offline") }.getCardBalance("card-1")

        assertEquals(Result.Error(DataError.NetworkError.NO_INTERNET), result)
    }
}
