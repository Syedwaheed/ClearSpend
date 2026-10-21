package com.edu.feature.auth.data.token

import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.EmptyResult
import com.edu.core.domain.util.Result
import com.edu.feature.auth.data.remote.AuthApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Clock
import java.time.Duration

/**
 * Owns access-token expiry checks and refreshes (CLAUDE.md §8 Token
 * Management). A [Mutex] makes concurrent callers share one refresh.
 */
class SessionTokenManager(
    private val tokenStore: TokenStore,
    private val authApi: AuthApi,
    private val tokenMapper: TokenMapper,
    private val clock: Clock,
    private val expiryLeeway: Duration = Duration.ofSeconds(60),
) {
    private val refreshMutex = Mutex()

    fun isExpired(tokens: StoredTokens): Boolean =
        !clock.instant().plus(expiryLeeway).isBefore(tokens.accessTokenExpiresAt)

    /**
     * Refreshes the access token.
     * @param force refresh even if the stored token looks valid (e.g. after a 401).
     * @param rejectedAccessToken when forcing, the token the server rejected; if
     *   another caller already replaced it, no second refresh is made.
     */
    suspend fun refresh(
        force: Boolean,
        rejectedAccessToken: String? = null,
    ): EmptyResult<DataError.NetworkError> = refreshMutex.withLock {
        val current = safeRead() ?: return@withLock Result.Error(DataError.NetworkError.UNAUTHORIZED)
        val alreadyRefreshedByOtherCaller =
            rejectedAccessToken != null && current.accessToken != rejectedAccessToken && !isExpired(current)
        if ((!force && !isExpired(current)) || alreadyRefreshedByOtherCaller) {
            return@withLock Result.Success(Unit)
        }
        val refreshToken = current.refreshToken
            ?: return@withLock Result.Error(DataError.NetworkError.UNAUTHORIZED)

        when (val response = authApi.refreshAccessToken(refreshToken)) {
            is Result.Error -> response
            is Result.Success -> {
                val saved = safeWrite(tokenMapper.map(response.data, previousRefreshToken = refreshToken))
                if (saved) Result.Success(Unit) else Result.Error(DataError.NetworkError.UNKNOWN)
            }
        }
    }

    suspend fun currentTokens(): StoredTokens? = safeRead()

    suspend fun save(tokens: StoredTokens): Boolean = safeWrite(tokens)

    private suspend fun safeRead(): StoredTokens? = try {
        tokenStore.getTokens()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }

    private suspend fun safeWrite(tokens: StoredTokens): Boolean = try {
        tokenStore.saveTokens(tokens)
        true
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        false
    }
}
