package com.edu.feature.auth.data.token

import com.edu.core.domain.network.TokenProvider
import com.edu.core.domain.util.Result

/**
 * Supplies bearer tokens to the Data API client in core:network. Refreshes
 * proactively when the stored token is about to expire, and on demand after a
 * 401 (see TokenRefreshPlugin). A failed refresh leaves the request to fail
 * with 401, which surfaces as `DataError.NetworkError.UNAUTHORIZED`.
 */
class TrueLayerTokenProvider(
    private val tokenManager: SessionTokenManager,
) : TokenProvider {

    override suspend fun getAccessToken(): String? {
        val tokens = tokenManager.currentTokens() ?: return null
        if (!tokenManager.isExpired(tokens)) return tokens.accessToken
        tokenManager.refresh(force = false)
        return tokenManager.currentTokens()?.accessToken
    }

    override suspend fun refreshAccessToken(): Boolean {
        val rejected = tokenManager.currentTokens()?.accessToken
        return tokenManager.refresh(force = true, rejectedAccessToken = rejected) is Result.Success
    }
}
