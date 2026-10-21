package com.edu.feature.auth.data.token

import com.edu.core.domain.storage.SecureDataStorage
import java.time.Instant

/**
 * Typed view over [SecureDataStorage] (Tink AEAD + DataStore) for everything
 * the session keeps: tokens, their expiry, the pending OAuth state and the
 * account holder's name. Every value is stored as an encrypted string.
 */
class TokenStore(
    private val secureStorage: SecureDataStorage,
) {
    suspend fun saveTokens(tokens: StoredTokens) {
        secureStorage.putString(KEY_ACCESS_TOKEN, tokens.accessToken)
        secureStorage.putString(KEY_EXPIRES_AT, tokens.accessTokenExpiresAt.toEpochMilli().toString())
        val refreshToken = tokens.refreshToken
        if (refreshToken != null) {
            secureStorage.putString(KEY_REFRESH_TOKEN, refreshToken)
        } else {
            secureStorage.remove(KEY_REFRESH_TOKEN)
        }
    }

    suspend fun getTokens(): StoredTokens? {
        val accessToken = secureStorage.getString(KEY_ACCESS_TOKEN) ?: return null
        val expiresAt = secureStorage.getString(KEY_EXPIRES_AT)?.toLongOrNull() ?: return null
        return StoredTokens(
            accessToken = accessToken,
            refreshToken = secureStorage.getString(KEY_REFRESH_TOKEN),
            accessTokenExpiresAt = Instant.ofEpochMilli(expiresAt),
        )
    }

    suspend fun savePendingState(state: String) = secureStorage.putString(KEY_PENDING_STATE, state)

    suspend fun takePendingState(): String? {
        val state = secureStorage.getString(KEY_PENDING_STATE)
        secureStorage.remove(KEY_PENDING_STATE)
        return state
    }

    suspend fun saveAccountHolderName(name: String) = secureStorage.putString(KEY_ACCOUNT_HOLDER_NAME, name)

    suspend fun getAccountHolderName(): String? = secureStorage.getString(KEY_ACCOUNT_HOLDER_NAME)

    suspend fun clear() = secureStorage.clear()

    private companion object {
        const val KEY_ACCESS_TOKEN = "tl_access_token"
        const val KEY_REFRESH_TOKEN = "tl_refresh_token"
        const val KEY_EXPIRES_AT = "tl_access_token_expires_at"
        const val KEY_PENDING_STATE = "tl_oauth_pending_state"
        const val KEY_ACCOUNT_HOLDER_NAME = "tl_account_holder_name"
    }
}
