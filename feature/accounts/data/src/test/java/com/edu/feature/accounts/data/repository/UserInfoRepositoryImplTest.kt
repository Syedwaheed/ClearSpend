package com.edu.feature.accounts.data.repository

import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.feature.accounts.data.fake.FakeSecureDataStorage
import com.edu.feature.accounts.data.fake.FakeSyncStatusStorage
import com.edu.feature.accounts.data.fake.json
import com.edu.feature.accounts.data.fake.mockDataClient
import com.edu.feature.accounts.data.remote.TrueLayerInfoApi
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class UserInfoRepositoryImplTest {

    private val secureStorage = FakeSecureDataStorage()
    private var requestedPath: String? = null

    private fun repository(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) =
        UserInfoRepositoryImpl(
            api = TrueLayerInfoApi(mockDataClient { request -> requestedPath = request.url.encodedPath; handler(request) }),
            secureStorage = secureStorage,
            syncStatus = FakeSyncStatusStorage(),
        )

    private val infoJson = """{"results":[{"full_name":"John Doe","emails":["john@example.com"]}]}"""

    @Test
    fun `Should call the info endpoint`() = runTest {
        repository { json(infoJson) }.getUserInfo()

        assertEquals("/data/v1/info", requestedPath)
    }

    @Test
    fun `Should return the full name`() = runTest {
        val result = repository { json(infoJson) }.getUserInfo() as Result.Success

        assertEquals("John Doe", result.data.fullName)
    }

    @Test
    fun `Should return the cached name When offline`() = runTest {
        repository { json(infoJson) }.getUserInfo()

        val result = repository { throw IOException("offline") }.getUserInfo() as Result.Success

        assertEquals("John Doe", result.data.fullName)
    }

    @Test
    fun `Should return not found When the API has no name`() = runTest {
        val result = repository { json("""{"results":[{"full_name":""}]}""") }.getUserInfo()

        assertEquals(Result.Error(DataError.NetworkError.NOT_FOUND), result)
    }
}
