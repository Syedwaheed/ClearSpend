package com.edu.feature.auth.domain.repository

import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.EmptyResult
import com.edu.core.domain.util.Result
import com.edu.feature.auth.domain.model.SessionState

interface AuthRepository {

    /**
     * Generates a fresh single-use OAuth `state`, stores it until the redirect
     * arrives, and returns the TrueLayer auth link for [providerIds].
     */
    suspend fun createAuthorizationUrl(providerIds: List<String>): Result<String, DataError.LocalError>

    /** Returns the pending `state` (if any) and forgets it, so it can be used once only. */
    suspend fun consumePendingState(): String?

    /**
     * Exchanges an authorization code for tokens, stores them encrypted and
     * records the consent date.
     */
    suspend fun exchangeAuthorizationCode(code: String): EmptyResult<DataError.NetworkError>

    suspend fun getSessionState(): SessionState

    /** Uses the stored refresh token. `UNAUTHORIZED` means consent is gone and the user must reconnect. */
    suspend fun refreshSession(): EmptyResult<DataError.NetworkError>

    /** Account holder's full name from `/data/v1/info`, cached at connect time. */
    suspend fun getAccountHolderName(): String?

    /** Deletes tokens and every secret held for the session. */
    suspend fun signOut()
}
