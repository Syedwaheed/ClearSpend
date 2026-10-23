package com.edu.feature.accounts.data.repository

import com.edu.core.database.dao.AccountDao
import com.edu.core.database.entity.AccountEntity
import com.edu.core.domain.model.AccountType
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
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class AccountRepositoryImplTest {

    private val accountDao: AccountDao = mockk(relaxed = true)
    private val syncStatus = FakeSyncStatusStorage()
    private var requestedPath: String? = null

    private fun repository(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) =
        AccountRepositoryImpl(
            api = TrueLayerAccountsApi(mockDataClient { request -> requestedPath = request.url.encodedPath; handler(request) }),
            accountDao = accountDao,
            syncStatus = syncStatus,
        )

    private val accountsJson = """
        {"results":[{"update_timestamp":"2026-09-16T10:00:00Z","account_id":"acc-1",
          "account_type":"TRANSACTION","display_name":"Transaction Account 1","currency":"GBP",
          "account_number":{"iban":"GB29MONZ01213100000000","number":"00000000","sort_code":"012131"},
          "provider":{"display_name":"Monzo","logo_uri":"https://logo/monzo.svg","provider_id":"ob-monzo"}}],
         "status":"Succeeded"}
    """.trimIndent()

    private val cachedEntity = AccountEntity(
        accountId = "cached", accountType = "SAVINGS", displayName = "Cached", currency = "GBP",
        providerId = "p", providerDisplayName = "Bank", providerLogoUri = null, iban = null, sortCode = null, number = null,
    )

    @Test
    fun `Should call the accounts endpoint`() = runTest {
        repository { json(accountsJson) }.getAccounts()

        assertEquals("/data/v1/accounts", requestedPath)
    }

    @Test
    fun `Should map accounts from the API`() = runTest {
        val result = repository { json(accountsJson) }.getAccounts() as Result.Success

        assertEquals("Transaction Account 1", result.data.single().displayName)
    }

    @Test
    fun `Should map the provider`() = runTest {
        val result = repository { json(accountsJson) }.getAccounts() as Result.Success

        assertEquals("Monzo", result.data.single().provider.displayName)
    }

    @Test
    fun `Should cache accounts When fetched`() = runTest {
        repository { json(accountsJson) }.getAccounts()

        coVerify { accountDao.replaceAll(match { it.single().accountId == "acc-1" }) }
    }

    @Test
    fun `Should return cached accounts When offline`() = runTest {
        coEvery { accountDao.getAccounts() } returns listOf(cachedEntity)

        val result = repository { throw IOException("offline") }.getAccounts() as Result.Success

        assertEquals(AccountType.SAVINGS, result.data.single().accountType)
    }

    @Test
    fun `Should flag cached data When offline`() = runTest {
        coEvery { accountDao.getAccounts() } returns listOf(cachedEntity)

        repository { throw IOException("offline") }.getAccounts()

        assertTrue(syncStatus.servedFromCache)
    }

    @Test
    fun `Should return no internet When offline and nothing cached`() = runTest {
        coEvery { accountDao.getAccounts() } returns emptyList()

        val result = repository { throw IOException("offline") }.getAccounts()

        assertEquals(Result.Error(DataError.NetworkError.NO_INTERNET), result)
    }

    @Test
    fun `Should return unauthorized When consent expired even with a cache`() = runTest {
        coEvery { accountDao.getAccounts() } returns listOf(cachedEntity)

        val result = repository { json("{}", HttpStatusCode.Unauthorized) }.getAccounts()

        assertEquals(Result.Error(DataError.NetworkError.UNAUTHORIZED), result)
    }

    @Test
    fun `Should return an empty list When the bank has no accounts`() = runTest {
        val result = repository { json("""{"results":[],"status":"Succeeded"}""") }.getAccounts()

        assertEquals(Result.Success(emptyList<Any>()), result)
    }

    @Test
    fun `Should call the single account endpoint`() = runTest {
        repository { json(accountsJson) }.getAccount("acc-1")

        assertEquals("/data/v1/accounts/acc-1", requestedPath)
    }

    @Test
    fun `Should return not found When the single account is missing`() = runTest {
        coEvery { accountDao.getAccount("x") } returns null

        val result = repository { json("""{"results":[]}""") }.getAccount("x")

        assertEquals(Result.Error(DataError.NetworkError.NOT_FOUND), result)
    }

    @Test
    fun `Should return the cached single account When offline`() = runTest {
        coEvery { accountDao.getAccount("cached") } returns cachedEntity

        val result = repository { throw IOException("offline") }.getAccount("cached") as Result.Success

        assertEquals("Cached", result.data.displayName)
    }
}
