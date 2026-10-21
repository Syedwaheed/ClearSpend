package com.edu.core.data.cache

import com.edu.core.domain.storage.SyncStatusStorage
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import kotlinx.coroutines.CancellationException

/**
 * Network-first read with a Room fallback, shared by every `*:data`
 * repository so the offline policy lives in one place (DESIGN_SPEC §7):
 *
 * 1. [fetch] (a core:network call) succeeds → [saveToCache], record the sync, return it.
 * 2. It fails with UNAUTHORIZED → return the error untouched: consent has
 *    expired and the user must re-authenticate, stale data must not hide that.
 * 3. Any other failure → [readCache]; if it holds data, flag "served from
 *    cache" and return it, otherwise return the original network error.
 *
 * Cache read/write exceptions never escape (exceptions must not cross the
 * data → domain boundary): a failed write is ignored, a failed read falls
 * back to the network error.
 */
suspend fun <T> networkFirst(
    syncStatus: SyncStatusStorage,
    fetch: suspend () -> Result<T, DataError.NetworkError>,
    saveToCache: suspend (T) -> Unit,
    readCache: suspend () -> T?,
): Result<T, DataError> {
    when (val remote = fetch()) {
        is Result.Success -> {
            runCatchingNonCancellation { saveToCache(remote.data) }
            runCatchingNonCancellation { syncStatus.recordSuccessfulSync() }
            return remote
        }
        is Result.Error -> {
            if (remote.error == DataError.NetworkError.UNAUTHORIZED) return remote
            val cached = runCatchingNonCancellation { readCache() }
            if (cached != null) {
                runCatchingNonCancellation { syncStatus.recordServedFromCache() }
                return Result.Success(cached)
            }
            return remote
        }
    }
}

/** Convenience for list endpoints: an empty cache counts as "no cache". */
suspend fun <T> networkFirstList(
    syncStatus: SyncStatusStorage,
    fetch: suspend () -> Result<List<T>, DataError.NetworkError>,
    saveToCache: suspend (List<T>) -> Unit,
    readCache: suspend () -> List<T>,
): Result<List<T>, DataError> = networkFirst(
    syncStatus = syncStatus,
    fetch = fetch,
    saveToCache = saveToCache,
    readCache = { readCache().takeIf { it.isNotEmpty() } },
)

private inline fun <R> runCatchingNonCancellation(block: () -> R): R? = try {
    block()
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    null
}
