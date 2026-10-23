package com.edu.core.domain.storage

import java.time.Instant

/**
 * Tracks whether the data on screen came from the network or from the Room
 * cache, so screens can show the "Showing data from {time}" offline banner
 * (DESIGN_SPEC §7). Repositories write it; use cases read it.
 */
interface SyncStatusStorage {
    /** A network fetch succeeded and its result was cached. Clears the offline flag. */
    suspend fun recordSuccessfulSync()

    /** A network fetch failed and cached data was returned instead. */
    suspend fun recordServedFromCache()

    /** Time of the last successful sync, or null if the app has never synced. */
    suspend fun getLastSyncedAt(): Instant?

    /**
     * Time of the last successful sync when the most recent read was served
     * from the cache; null when the data is fresh.
     */
    suspend fun getOfflineSince(): Instant?
}
