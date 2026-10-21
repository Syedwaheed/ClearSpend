package com.edu.feature.auth.data.token

import java.time.Instant

/** Tokens as persisted (encrypted) on the device. */
data class StoredTokens(
    val accessToken: String,
    val refreshToken: String?,
    val accessTokenExpiresAt: Instant,
) {
    override fun toString(): String =
        "StoredTokens(accessToken=***, refreshToken=${if (refreshToken == null) "null" else "***"}, accessTokenExpiresAt=$accessTokenExpiresAt)"
}
