package com.edu.core.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.edu.core.domain.storage.UserPreferencesStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private object PreferenceKeys {
    val CONSENT_DATE_EPOCH_DAY = longPreferencesKey("consent_date_epoch_day")
    val LAST_LOGIN_EPOCH_MILLIS = longPreferencesKey("last_login_epoch_millis")
    val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
    val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
}

class DataStoreUserPreferencesStorage(
    private val dataStore: DataStore<Preferences>,
) : UserPreferencesStorage {

    override suspend fun setConsentDateEpochDay(epochDay: Long) {
        dataStore.edit { it[PreferenceKeys.CONSENT_DATE_EPOCH_DAY] = epochDay }
    }

    override fun observeConsentDateEpochDay(): Flow<Long?> =
        dataStore.data.map { it[PreferenceKeys.CONSENT_DATE_EPOCH_DAY] }

    override suspend fun setLastLoginEpochMillis(epochMillis: Long) {
        dataStore.edit { it[PreferenceKeys.LAST_LOGIN_EPOCH_MILLIS] = epochMillis }
    }

    override fun observeLastLoginEpochMillis(): Flow<Long?> =
        dataStore.data.map { it[PreferenceKeys.LAST_LOGIN_EPOCH_MILLIS] }

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferenceKeys.BIOMETRIC_ENABLED] = enabled }
    }

    override fun observeBiometricEnabled(): Flow<Boolean> =
        dataStore.data.map { it[PreferenceKeys.BIOMETRIC_ENABLED] ?: false }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferenceKeys.NOTIFICATIONS_ENABLED] = enabled }
    }

    override fun observeNotificationsEnabled(): Flow<Boolean> =
        dataStore.data.map { it[PreferenceKeys.NOTIFICATIONS_ENABLED] ?: true }
}
