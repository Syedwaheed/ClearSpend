package com.edu.feature.auth.domain.usecase

import com.edu.core.domain.util.Result
import com.edu.feature.auth.domain.model.AuthError
import com.edu.feature.auth.domain.model.AuthRedirect
import com.edu.feature.auth.domain.repository.AuthRepository
import java.security.MessageDigest

/**
 * Checks the `clearspend://callback` redirect against the `state` we generated
 * (CSRF protection) and extracts the authorization code. The stored state is
 * consumed whatever the outcome, so a redirect can never be replayed.
 */
class ValidateAuthRedirectUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(redirect: AuthRedirect): Result<String, AuthError> {
        val expectedState = authRepository.consumePendingState()
        val receivedState = redirect.state

        if (expectedState.isNullOrEmpty() || receivedState.isNullOrEmpty() ||
            !constantTimeEquals(expectedState, receivedState)
        ) {
            return Result.Error(AuthError.STATE_MISMATCH)
        }
        if (!redirect.error.isNullOrBlank()) {
            return Result.Error(AuthError.ACCESS_DENIED)
        }
        val code = redirect.code
        if (code.isNullOrBlank()) {
            return Result.Error(AuthError.MISSING_CODE)
        }
        return Result.Success(code)
    }

    private fun constantTimeEquals(a: String, b: String): Boolean =
        MessageDigest.isEqual(a.toByteArray(), b.toByteArray())
}
