package com.edu.feature.accounts.domain.usecase

import com.edu.core.domain.storage.UserPreferencesStorage
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * "Consent renews in N days" (DESIGN_SPEC §4 · 10): consent date (stored at
 * connect time) + 90 days − today, never negative. Null when no consent date
 * is stored.
 */
class GetConsentDaysRemainingUseCase(
    private val userPreferencesStorage: UserPreferencesStorage,
    private val clock: Clock,
) {

    suspend operator fun invoke(): Int? {
        val consentDay = userPreferencesStorage.observeConsentDateEpochDay().first() ?: return null
        val expiry = LocalDate.ofEpochDay(consentDay).plusDays(CONSENT_DAYS)
        return ChronoUnit.DAYS.between(LocalDate.now(clock), expiry).toInt().coerceAtLeast(0)
    }

    companion object {
        const val CONSENT_DAYS = 90L
    }
}
