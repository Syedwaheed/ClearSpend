package com.edu.core.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.io.File
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class DataStoreSyncStatusStorageTest {

    private lateinit var tempFile: File
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var storage: DataStoreSyncStatusStorage
    private val now = Instant.parse("2026-09-16T14:32:00Z")

    @Before
    fun setUp() {
        tempFile = File.createTempFile("sync_status_test", ".preferences_pb")
        tempFile.delete()
        dataStore = PreferenceDataStoreFactory.create(produceFile = { tempFile })
        storage = DataStoreSyncStatusStorage(dataStore, Clock.fixed(now, ZoneOffset.UTC))
    }

    @After
    fun tearDown() {
        tempFile.delete()
    }

    @Test
    fun `Should have no last sync When never synced`() = runTest {
        assertNull(storage.getLastSyncedAt())
    }

    @Test
    fun `Should store the sync time When a sync succeeds`() = runTest {
        storage.recordSuccessfulSync()

        assertEquals(now, storage.getLastSyncedAt())
    }

    @Test
    fun `Should not be offline When data is fresh`() = runTest {
        storage.recordSuccessfulSync()

        assertNull(storage.getOfflineSince())
    }

    @Test
    fun `Should report the last sync time When served from cache`() = runTest {
        storage.recordSuccessfulSync()
        storage.recordServedFromCache()

        assertEquals(now, storage.getOfflineSince())
    }

    @Test
    fun `Should clear the offline flag When a later sync succeeds`() = runTest {
        storage.recordServedFromCache()
        storage.recordSuccessfulSync()

        assertNull(storage.getOfflineSince())
    }
}
