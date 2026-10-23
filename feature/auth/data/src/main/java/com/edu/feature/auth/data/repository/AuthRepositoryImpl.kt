package com.edu.feature.auth.data.repository

import com.edu.core.domain.storage.UserPreferencesStorage
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.EmptyResult
import com.edu.core.domain.util.Result
import com.edu.feature.auth.data.oauth.AuthUrlBuilder
import com.edu.feature.auth.data.oauth.StateGenerator
import com.edu.feature.auth.data.remote.AuthApi
import com.edu.feature.auth.data.remote.UserInfoApi
import com.edu.feature.auth.data.token.SessionTokenManager
import com.edu.feature.auth.data.token.TokenMapper
import com.edu.feature.auth.data.token.TokenStore
import com.edu.feature.auth.domain.model.SessionState
import com.edu.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.CancellationException
import java.time.Clock
import java.time.LocalDate

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val userInfoApi: UserInfoApi,
    private val tokenStore: TokenStore,
    private val tokenManager: SessionTokenManager,
    private val tokenMapper: TokenMapper,
    private val authUrlBuilder: AuthUrlBuilder,
    private val stateGenerator: StateGenerator,
    private val userPreferencesStorage: UserPreferencesStorage,
    private val clock: Clock,
) : AuthRepository {

    override suspend fun createAuthorizationUrl(
        providerIds: List<String>,
    ): Result<String, DataError.LocalError> {
        val state = stateGenerator.generate()
        val saved = attempt { tokenStore.savePendingState(state) }
        if (!saved) return Result.Error(DataError.LocalError.DISKFULL)
        return Result.Success(authUrlBuilder.build(providerIds, state))
    }

    override suspend fun consumePendingState(): String? = attemptOrNull { tokenStore.takePendingState() }

    override suspend fun exchangeAuthorizationCode(code: String): EmptyResult<DataError.NetworkError> {
        val response = authApi.exchangeAuthorizationCode(code)
        if (response is Result.Error) return response
        val tokens = tokenMapper.map((response as Result.Success).data)

        if (!tokenManager.save(tokens)) return Result.Error(DataError.NetworkError.UNKNOWN)
        // DESIGN_SPEC §1.1 step 6: consent lasts 90 days from today.
        attempt { userPreferencesStorage.setConsentDateEpochDay(LocalDate.now(clock).toEpochDay()) }
        cacheAccountHolderName()
        return Result.Success(Unit)
    }

    override suspend fun getSessionState(): SessionState {
        val tokens = tokenManager.currentTokens() ?: return SessionState.NO_SESSION
        val expired = tokenManager.isExpired(tokens)
        return when {
            !expired -> SessionState.ACTIVE
            tokens.refreshToken.isNullOrBlank() -> SessionState.NO_SESSION
            else -> SessionState.ACCESS_TOKEN_EXPIRED
        }
    }

    override suspend fun refreshSession(): EmptyResult<DataError.NetworkError> =
        tokenManager.refresh(force = true)

    override suspend fun getAccountHolderName(): String? = attemptOrNull { tokenStore.getAccountHolderName() }

    override suspend fun signOut() {
        attempt { tokenStore.clear() }
    }

    /** Best effort: the greeting falls back to a generic one if this fails. */
    private suspend fun cacheAccountHolderName() {
        val info = userInfoApi.getUserInfo()
        if (info is Result.Success) {
            val name = info.data.results.firstOrNull()?.fullName?.trim()
            if (!name.isNullOrEmpty()) attempt { tokenStore.saveAccountHolderName(name) }
        }
    }

    private suspend fun attempt(block: suspend () -> Unit): Boolean = try {
        block()
        true
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        false
    }

    private suspend fun <T> attemptOrNull(block: suspend () -> T?): T? = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }
}
