package com.edu.feature.auth.presentation.fake

import com.edu.core.domain.storage.UserPreferencesStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeUserPreferencesStorage : UserPreferencesStorage {
    val onboarding = MutableStateFlow(false)
    private val consentDate = MutableStateFlow<Long?>(null)
    private val lastLogin = MutableStateFlow<Long?>(null)
    private val biometric = MutableStateFlow(false)
    private val notifications = MutableStateFlow(true)

    override suspend fun setConsentDateEpochDay(epochDay: Long) { consentDate.value = epochDay }
    override fun observeConsentDateEpochDay(): Flow<Long?> = consentDate
    override suspend fun setLastLoginEpochMillis(epochMillis: Long) { lastLogin.value = epochMillis }
    override fun observeLastLoginEpochMillis(): Flow<Long?> = lastLogin
    override suspend fun setBiometricEnabled(enabled: Boolean) { biometric.value = enabled }
    override fun observeBiometricEnabled(): Flow<Boolean> = biometric
    override suspend fun setNotificationsEnabled(enabled: Boolean) { notifications.value = enabled }
    override fun observeNotificationsEnabled(): Flow<Boolean> = notifications
    override suspend fun setOnboardingCompleted(completed: Boolean) { onboarding.value = completed }
    override fun observeOnboardingCompleted(): Flow<Boolean> = onboarding
}
