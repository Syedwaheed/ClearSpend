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
}
