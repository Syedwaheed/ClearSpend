package com.edu.feature.auth.domain.usecase

import com.edu.core.domain.storage.UserPreferencesStorage
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.feature.auth.domain.model.SessionState
import com.edu.feature.auth.domain.model.StartDestination
import com.edu.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.first

/**
 * Start-up routing (CLAUDE.md §10, DESIGN_SPEC §3):
 * - no session           → Onboarding on first launch, Connect Bank after that
 * - session valid        → Biometric if enabled, else Dashboard
 * - access token expired → refresh silently, then as "session valid"
 * - refresh rejected     → Connect Bank (consent gone)
 *
 * A refresh that fails for a transient reason (no internet, timeout, 5xx)
 * keeps the session so cached data can be shown offline (DESIGN_SPEC §7).
 */
class DetermineStartDestinationUseCase(
    private val authRepository: AuthRepository,
    private val userPreferencesStorage: UserPreferencesStorage,
) {
    suspend operator fun invoke(): StartDestination =
        when (authRepository.getSessionState()) {
            SessionState.NO_SESSION ->
                if (userPreferencesStorage.observeOnboardingCompleted().first()) {
                    StartDestination.CONNECT_BANK
                } else {
                    StartDestination.ONBOARDING
                }

            SessionState.ACTIVE -> unlockedDestination()

            SessionState.ACCESS_TOKEN_EXPIRED ->
                when (val refresh = authRepository.refreshSession()) {
                    is Result.Success -> unlockedDestination()
                    is Result.Error ->
                        if (refresh.error == DataError.NetworkError.UNAUTHORIZED) {
                            StartDestination.CONNECT_BANK
                        } else {
                            unlockedDestination()
                        }
                }
        }

    private suspend fun unlockedDestination(): StartDestination =
        if (userPreferencesStorage.observeBiometricEnabled().first()) {
            StartDestination.BIOMETRIC
        } else {
            StartDestination.DASHBOARD
        }
}
