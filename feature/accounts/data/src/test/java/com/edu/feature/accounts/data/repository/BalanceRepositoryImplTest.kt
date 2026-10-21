package com.edu.feature.accounts.data.repository

import com.edu.core.database.dao.BalanceDao
import com.edu.core.database.entity.BalanceEntity
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.feature.accounts.data.fake.FakeSyncStatusStorage
import com.edu.feature.accounts.data.fake.json
import com.edu.feature.accounts.data.fake.mockDataClient
import com.edu.feature.accounts.data.remote.TrueLayerAccountsApi
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
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class BalanceRepositoryImplTest {

    private val balanceDao: BalanceDao = mockk(relaxed = true)
    private var requestedPath: String? = null
    private val clock = Clock.fixed(Instant.parse("2026-09-16T12:00:00Z"), ZoneOffset.UTC)

    private fun repository(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) =
        BalanceRepositoryImpl(
            api = TrueLayerAccountsApi(mockDataClient { request -> requestedPath = request.url.encodedPath; handler(request) }),
            balanceDao = balanceDao,
            syncStatus = FakeSyncStatusStorage(),
            clock = clock,
        )

    private val balanceJson = """
        {"results":[{"currency":"GBP","available":44.01,"current":2.0,"overdraft":100,
          "update_timestamp":"2026-09-16T11:59:00Z"}],"status":"Succeeded"}
    """.trimIndent()

    @Test
    fun `Should call the balance endpoint`() = runTest {
        repository { json(balanceJson) }.getBalance("acc-1")

        assertEquals("/data/v1/accounts/acc-1/balance", requestedPath)
    }

    @Test
    fun `Should parse money as exact BigDecimal`() = runTest {
        val result = repository { json(balanceJson) }.getBalance("acc-1") as Result.Success

        assertEquals(BigDecimal("44.01"), result.data.available)
    }

    @Test
    fun `Should parse the overdraft`() = runTest {
        val result = repository { json(balanceJson) }.getBalance("acc-1") as Result.Success

        assertEquals(BigDecimal("100"), result.data.overdraft)
    }

    @Test
    fun `Should cache the balance When fetched`() = runTest {
        repository { json(balanceJson) }.getBalance("acc-1")

        coVerify { balanceDao.upsert(match { it.available == "44.01" }) }
    }

    @Test
    fun `Should return the cached balance When offline`() = runTest {
        coEvery { balanceDao.getBalance("acc-1") } returns
            BalanceEntity("acc-1", "10.00", "9.00", "0", "GBP", 0L)

        val result = repository { throw IOException("offline") }.getBalance("acc-1") as Result.Success

        assertEquals(BigDecimal("10.00"), result.data.available)
    }

    @Test
    fun `Should return not found When the envelope is empty`() = runTest {
        val result = repository { json("""{"results":[]}""") }.getBalance("acc-1")

        assertEquals(Result.Error(DataError.NetworkError.NOT_FOUND), result)
    }

    @Test
    fun `Should map a server error`() = runTest {
        coEvery { balanceDao.getBalance(any()) } returns null

        val result = repository { json("{}", HttpStatusCode.InternalServerError) }.getBalance("acc-1")

        assertEquals(Result.Error(DataError.NetworkError.SERVER_ERROR), result)
    }
}
