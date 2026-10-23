package com.edu.feature.auth.data.remote

import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.feature.auth.data.remote.dto.TokenResponseDto

/**
 * Every call that needs the TrueLayer client secret. In production this
 * interface is re-implemented against our own backend (which holds the
 * secret); nothing outside `feature:auth:data` changes.
 *
 * Errors: a rejected code / refresh token / client (HTTP 400 or 401 from
 * the token endpoint) is reported as [DataError.NetworkError.UNAUTHORIZED].
 */
interface AuthApi {
    suspend fun exchangeAuthorizationCode(code: String): Result<TokenResponseDto, DataError.NetworkError>
    suspend fun refreshAccessToken(refreshToken: String): Result<TokenResponseDto, DataError.NetworkError>
}
