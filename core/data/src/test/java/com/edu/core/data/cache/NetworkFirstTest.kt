package com.edu.core.data.cache

import com.edu.core.domain.storage.SyncStatusStorage
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class NetworkFirstTest {

    private class RecordingSyncStatus : SyncStatusStorage {
        var synced = false
        var servedFromCache = false
        override suspend fun recordSuccessfulSync() { synced = true; servedFromCache = false }
        override suspend fun recordServedFromCache() { servedFromCache = true }
        override suspend fun getLastSyncedAt(): Instant? = null
        override suspend fun getOfflineSince(): Instant? = null
    }

    private val syncStatus = RecordingSyncStatus()
    private var saved: List<String>? = null

    @Test
    fun `Should return network data When fetch succeeds`() = runTest {
        val result = networkFirstList(syncStatus, { Result.Success(listOf("a")) }, { saved = it }, { listOf("cached") })

        assertEquals(Result.Success(listOf("a")), result)
    }

    @Test
    fun `Should cache network data When fetch succeeds`() = runTest {
        networkFirstList(syncStatus, { Result.Success(listOf("a")) }, { saved = it }, { emptyList() })

        assertEquals(listOf("a"), saved)
    }

    @Test
    fun `Should record a successful sync When fetch succeeds`() = runTest {
        networkFirstList(syncStatus, { Result.Success(listOf("a")) }, { saved = it }, { emptyList() })

        assertTrue(syncStatus.synced)
    }

    @Test
    fun `Should return cached data When offline`() = runTest {
        val result = networkFirstList<String>(
            syncStatus,
            { Result.Error(DataError.NetworkError.NO_INTERNET) },
            { saved = it },
            { listOf("cached") },
        )

        assertEquals(Result.Success(listOf("cached")), result)
    }

    @Test
    fun `Should flag served from cache When offline with cache`() = runTest {
        networkFirstList<String>(syncStatus, { Result.Error(DataError.NetworkError.NO_INTERNET) }, {}, { listOf("c") })

        assertTrue(syncStatus.servedFromCache)
    }

    @Test
    fun `Should return network error When offline with empty cache`() = runTest {
        val result = networkFirstList<String>(syncStatus, { Result.Error(DataError.NetworkError.NO_INTERNET) }, {}, { emptyList() })

        assertEquals(Result.Error(DataError.NetworkError.NO_INTERNET), result)
    }

    @Test
    fun `Should not fall back to cache When unauthorized`() = runTest {
        val result = networkFirstList<String>(
            syncStatus,
            { Result.Error(DataError.NetworkError.UNAUTHORIZED) },
            {},
            { listOf("cached") },
        )

        assertEquals(Result.Error(DataError.NetworkError.UNAUTHORIZED), result)
    }

    @Test
    fun `Should return network error When the cache read throws`() = runTest {
        val result = networkFirst<String>(
            syncStatus,
            { Result.Error(DataError.NetworkError.SERVER_ERROR) },
            {},
            { throw IllegalStateException("db closed") },
        )

        assertEquals(Result.Error(DataError.NetworkError.SERVER_ERROR), result)
    }

    @Test
    fun `Should still return network data When the cache write throws`() = runTest {
        val result = networkFirst(syncStatus, { Result.Success("fresh") }, { throw IllegalStateException("disk") }, { null })

        assertEquals(Result.Success("fresh"), result)
    }

    @Test
    fun `Should not flag served from cache When cache is empty`() = runTest {
        networkFirstList<String>(syncStatus, { Result.Error(DataError.NetworkError.NO_INTERNET) }, {}, { emptyList() })

        assertFalse(syncStatus.servedFromCache)
    }
}
