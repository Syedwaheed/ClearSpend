package com.edu.core.domain.storage

import kotlinx.coroutines.flow.Flow

interface UserPreferencesStorage {
    suspend fun setConsentDateEpochDay(epochDay: Long)
    fun observeConsentDateEpochDay(): Flow<Long?>

    suspend fun setLastLoginEpochMillis(epochMillis: Long)
    fun observeLastLoginEpochMillis(): Flow<Long?>

    suspend fun setBiometricEnabled(enabled: Boolean)
    fun observeBiometricEnabled(): Flow<Boolean>

    suspend fun setNotificationsEnabled(enabled: Boolean)
    fun observeNotificationsEnabled(): Flow<Boolean>

    /** Set once the user finishes (or skips) onboarding screens 02-04. */
    suspend fun setOnboardingCompleted(completed: Boolean)
    fun observeOnboardingCompleted(): Flow<Boolean>
}
