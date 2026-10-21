package com.edu.feature.auth.data.token

import com.edu.feature.auth.data.remote.dto.TokenResponseDto
import java.time.Clock

/** DTO → stored model. Converts the relative `expires_in` into an absolute instant. */
class TokenMapper(
    private val clock: Clock,
) {
    /**
     * @param previousRefreshToken kept when a refresh response omits `refresh_token`.
     */
    fun map(dto: TokenResponseDto, previousRefreshToken: String? = null): StoredTokens = StoredTokens(
        accessToken = dto.accessToken,
        refreshToken = dto.refreshToken?.takeIf { it.isNotBlank() } ?: previousRefreshToken,
        accessTokenExpiresAt = clock.instant().plusSeconds(dto.expiresIn.coerceAtLeast(0)),
    )
}
