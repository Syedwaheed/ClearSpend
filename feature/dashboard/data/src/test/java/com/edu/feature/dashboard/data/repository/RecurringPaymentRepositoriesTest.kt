package com.edu.feature.dashboard.data.repository

import com.edu.core.database.dao.DirectDebitDao
import com.edu.core.database.dao.StandingOrderDao
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.feature.dashboard.data.fake.FakeSyncStatusStorage
import com.edu.feature.dashboard.data.fake.json
import com.edu.feature.dashboard.data.fake.mockDataClient
import com.edu.feature.dashboard.data.remote.TrueLayerRecurringPaymentsApi
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

class DirectDebitRepositoryImplTest {

    private val dao: DirectDebitDao = mockk(relaxed = true)
    private var requestedPath: String? = null

    private fun repository(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) =
        DirectDebitRepositoryImpl(
            api = TrueLayerRecurringPaymentsApi(mockDataClient { request -> requestedPath = request.url.encodedPath; handler(request) }),
            directDebitDao = dao,
            syncStatus = FakeSyncStatusStorage(),
        )

    private val ddJson = """
        {"results":[{"direct_debit_id":"dd-1","timestamp":"2026-09-01T00:00:00Z","name":"EDF Energy","status":"Active",
          "previous_payment_timestamp":"2026-08-17T00:00:00Z","previous_payment_amount":24.5,"currency":"GBP"}]}
    """.trimIndent()

    @Test
    fun `Should call the direct debits endpoint`() = runTest {
        repository { json(ddJson) }.getDirectDebits("acc-1")

        assertEquals("/data/v1/accounts/acc-1/direct_debits", requestedPath)
    }

    @Test
    fun `Should map direct debits`() = runTest {
        val result = repository { json(ddJson) }.getDirectDebits("acc-1") as Result.Success

        assertEquals("EDF Energy", result.data.single().name)
    }

    @Test
    fun `Should replace the cached list for the account`() = runTest {
        repository { json(ddJson) }.getDirectDebits("acc-1")

        coVerify { dao.replaceAll("acc-1", match { it.size == 1 }) }
    }

    @Test
    fun `Should return no internet When offline with nothing cached`() = runTest {
        coEvery { dao.getDirectDebits(any()) } returns emptyList()

        val result = repository { throw IOException("offline") }.getDirectDebits("acc-1")

        assertEquals(Result.Error(DataError.NetworkError.NO_INTERNET), result)
    }
}

class StandingOrderRepositoryImplTest {

    private val dao: StandingOrderDao = mockk(relaxed = true)
    private var requestedPath: String? = null

    private fun repository(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) =
        StandingOrderRepositoryImpl(
            api = TrueLayerRecurringPaymentsApi(mockDataClient { request -> requestedPath = request.url.encodedPath; handler(request) }),
            standingOrderDao = dao,
            syncStatus = FakeSyncStatusStorage(),
        )

    private val soJson = """
        {"results":[{"frequency":"IntrvlMnthDay:01:19","status":"Active","next_payment_date":"2026-09-19T00:00:00+00:00",
          "next_payment_amount":850.0,"reference":"RENT","payee":"Landlord","currency":"GBP"}]}
    """.trimIndent()

    @Test
    fun `Should call the standing orders endpoint`() = runTest {
        repository { json(soJson) }.getStandingOrders("acc-1")

        assertEquals("/data/v1/accounts/acc-1/standing_orders", requestedPath)
    }

    @Test
    fun `Should map standing orders`() = runTest {
        val result = repository { json(soJson) }.getStandingOrders("acc-1") as Result.Success

        assertEquals("Landlord", result.data.single().payee)
    }

    @Test
    fun `Should replace the cached list for the account`() = runTest {
        repository { json(soJson) }.getStandingOrders("acc-1")

        coVerify { dao.replaceAll("acc-1", match { it.size == 1 }) }
    }
}
