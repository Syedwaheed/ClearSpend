package com.edu.feature.transactions.data.repository

import com.edu.core.database.dao.TransactionDao
import com.edu.core.database.entity.TransactionEntity
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.feature.transactions.data.fake.FakeSyncStatusStorage
import com.edu.feature.transactions.data.fake.json
import com.edu.feature.transactions.data.fake.mockDataClient
import com.edu.feature.transactions.data.remote.TrueLayerTransactionsApi
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class TransactionRepositoryImplTest {

    private val dao: TransactionDao = mockk(relaxed = true)
    private var lastRequest: HttpRequestData? = null
    private val clock = Clock.fixed(Instant.parse("2026-09-16T12:00:00Z"), ZoneOffset.UTC)

    private fun repository(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) =
        TransactionRepositoryImpl(
            api = TrueLayerTransactionsApi(mockDataClient { request -> lastRequest = request; handler(request) }),
            transactionDao = dao,
            syncStatus = FakeSyncStatusStorage(),
            clock = clock,
        )

    private val txJson = """
        {"results":[
          {"transaction_id":"old","timestamp":"2026-09-01T10:00:00+00:00","description":"A","amount":-1.0,
           "currency":"GBP","transaction_type":"DEBIT","transaction_category":"PURCHASE","transaction_classification":[]},
          {"transaction_id":"new","timestamp":"2026-09-15T10:00:00+00:00","description":"B","amount":420.0,
           "currency":"GBP","transaction_type":"CREDIT","transaction_category":"CREDIT","transaction_classification":[],
           "running_balance":{"amount":500.0,"currency":"GBP"}}
        ],"status":"Succeeded"}
    """.trimIndent()

    private val from = LocalDate.of(2026, 8, 17)
    private val to = LocalDate.of(2026, 9, 16)

    @Test
    fun `Should call the transactions endpoint`() = runTest {
        repository { json(txJson) }.getTransactions("acc-1", from, to)

        assertEquals("/data/v1/accounts/acc-1/transactions", lastRequest!!.url.encodedPath)
    }

    @Test
    fun `Should pass the date window`() = runTest {
        repository { json(txJson) }.getTransactions("acc-1", from, to)

        assertEquals("2026-08-17..2026-09-16", "${lastRequest!!.url.parameters["from"]}..${lastRequest!!.url.parameters["to"]}")
    }

    @Test
    fun `Should return transactions newest first`() = runTest {
        val result = repository { json(txJson) }.getTransactions("acc-1", from, to) as Result.Success

        assertEquals(listOf("new", "old"), result.data.map { it.transactionId })
    }

    @Test
    fun `Should cache settled transactions`() = runTest {
        repository { json(txJson) }.getTransactions("acc-1", from, to)

        coVerify { dao.upsertAll(match { it.size == 2 }) }
    }

    @Test
    fun `Should read the cached window When offline`() = runTest {
        coEvery { dao.getTransactions("acc-1", any(), any()) } returns listOf(
            TransactionEntity("c", "acc-1", 0L, "Cached", null, "-5.00", "GBP", "DEBIT", "PURCHASE", emptyList(), null, false),
        )

        val result = repository { throw IOException("offline") }.getTransactions("acc-1", from, to) as Result.Success

        assertEquals("c", result.data.single().transactionId)
    }

    @Test
    fun `Should query the cache with the whole last day included`() = runTest {
        coEvery { dao.getTransactions(any(), any(), any()) } returns emptyList()

        repository { throw IOException("offline") }.getTransactions("acc-1", from, to)

        coVerify {
            dao.getTransactions(
                "acc-1",
                Instant.parse("2026-08-17T00:00:00Z").toEpochMilli(),
                Instant.parse("2026-09-17T00:00:00Z").toEpochMilli() - 1,
            )
        }
    }

    @Test
    fun `Should call the pending endpoint`() = runTest {
        repository { json("""{"results":[]}""") }.getPendingTransactions("acc-1")

        assertEquals("/data/v1/accounts/acc-1/transactions/pending", lastRequest!!.url.encodedPath)
    }

    @Test
    fun `Should replace cached pending transactions`() = runTest {
        repository { json(txJson) }.getPendingTransactions("acc-1")

        coVerify { dao.replacePending("acc-1", match { list -> list.all { it.isPending } }) }
    }

    @Test
    fun `Should load a single transaction from the cache`() = runTest {
        coEvery { dao.getTransaction("p-1") } returns
            TransactionEntity("p-1", "acc-1", 0L, "COSTA", "Costa Coffee", "-4.65", "GBP", "DEBIT", "PURCHASE", emptyList(), null, true)

        val result = repository { json("{}") }.getTransaction("p-1") as Result.Success

        assertEquals(true, result.data.isPending)
    }

    @Test
    fun `Should return not found When the transaction is not cached`() = runTest {
        coEvery { dao.getTransaction("missing") } returns null

        val result = repository { json("{}") }.getTransaction("missing")

        assertEquals(Result.Error(DataError.NetworkError.NOT_FOUND), result)
    }

    @Test
    fun `Should not throw When the cache read fails`() = runTest {
        coEvery { dao.getTransaction("x") } throws IllegalStateException("db closed")

        val result = repository { json("{}") }.getTransaction("x")

        assertEquals(Result.Error(DataError.NetworkError.UNKNOWN), result)
    }

    @Test
    fun `Should return unauthorized When consent expired`() = runTest {
        val result = repository { json("{}", HttpStatusCode.Unauthorized) }.getPendingTransactions("acc-1")

        assertEquals(Result.Error(DataError.NetworkError.UNAUTHORIZED), result)
    }
}
