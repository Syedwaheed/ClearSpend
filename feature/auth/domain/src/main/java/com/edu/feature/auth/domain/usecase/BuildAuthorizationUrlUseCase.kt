package com.edu.feature.auth.domain.usecase

import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.feature.auth.domain.repository.AuthRepository

/**
 * Chooses the `providers=` list for the auth link (DESIGN_SPEC §1.1 / §1.3):
 * - sandbox always includes the Mock bank `uk-cs-mock`
 * - a picked bank adds its provider id
 * - no pick ("Don't see your bank?") offers every OB + OAuth provider
 */
class BuildAuthorizationUrlUseCase(
    private val authRepository: AuthRepository,
    private val isSandbox: Boolean,
) {
    suspend operator fun invoke(selectedProviderId: String?): Result<String, DataError.LocalError> {
        val providers = buildList {
            if (isSandbox) add(MOCK_PROVIDER)
            if (selectedProviderId.isNullOrBlank()) {
                add(ALL_OPEN_BANKING_PROVIDERS)
                add(ALL_OAUTH_PROVIDERS)
            } else {
                add(selectedProviderId)
            }
        }.distinct()
        return authRepository.createAuthorizationUrl(providers)
    }

    companion object {
        const val MOCK_PROVIDER = "uk-cs-mock"
        const val ALL_OPEN_BANKING_PROVIDERS = "uk-ob-all"
        const val ALL_OAUTH_PROVIDERS = "uk-oauth-all"
    }
}
