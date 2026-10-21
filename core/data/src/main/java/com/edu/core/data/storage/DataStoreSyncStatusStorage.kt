package com.edu.core.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.edu.core.domain.storage.SyncStatusStorage
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.Instant

private val LAST_SYNC_EPOCH_MILLIS = longPreferencesKey("last_sync_epoch_millis")

/**
 * The last-sync time is persisted (so a cold start offline can still say
 * "Showing data from 14:32"); whether the current data is from the cache is
 * per-process state and starts as "fresh".
 */
class DataStoreSyncStatusStorage(
    private val dataStore: DataStore<Preferences>,
    private val clock: Clock,
) : SyncStatusStorage {

    @Volatile
    private var servingCache: Boolean = false

    override suspend fun recordSuccessfulSync() {
        servingCache = false
        dataStore.edit { it[LAST_SYNC_EPOCH_MILLIS] = clock.millis() }
    }

    override suspend fun recordServedFromCache() {
        servingCache = true
    }

    override suspend fun getLastSyncedAt(): Instant? =
        dataStore.data.first()[LAST_SYNC_EPOCH_MILLIS]?.let(Instant::ofEpochMilli)

    override suspend fun getOfflineSince(): Instant? =
        if (servingCache) getLastSyncedAt() else null
}
