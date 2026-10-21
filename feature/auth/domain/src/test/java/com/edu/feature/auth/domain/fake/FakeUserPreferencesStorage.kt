package com.edu.feature.auth.domain.fake

import com.edu.core.domain.storage.UserPreferencesStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeUserPreferencesStorage(
    onboardingCompleted: Boolean = false,
    biometricEnabled: Boolean = false,
    lastLoginEpochMillis: Long? = null,
) : UserPreferencesStorage {
    val consentDate = MutableStateFlow<Long?>(null)
    val lastLogin = MutableStateFlow(lastLoginEpochMillis)
    val biometric = MutableStateFlow(biometricEnabled)
    val notifications = MutableStateFlow(true)
    val onboarding = MutableStateFlow(onboardingCompleted)

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
