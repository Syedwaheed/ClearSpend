package com.edu.core.data.storage

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class DataStoreUserPreferencesStorageTest {

    private lateinit var tempFile: File
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var storage: DataStoreUserPreferencesStorage

    @Before
    fun setUp() {
        tempFile = File.createTempFile("user_preferences_test", ".preferences_pb")
        dataStore = PreferenceDataStoreFactory.create(produceFile = { tempFile })
        storage = DataStoreUserPreferencesStorage(dataStore)
    }

    @After
    fun tearDown() {
        tempFile.delete()
    }

    @Test
    fun `observeConsentDateEpochDay returns null When nothing stored`() = runTest {
        assertNull(storage.observeConsentDateEpochDay().first())
    }

    @Test
    fun `setConsentDateEpochDay persists the value`() = runTest {
        storage.setConsentDateEpochDay(19_000L)

        assertEquals(19_000L, storage.observeConsentDateEpochDay().first())
    }

    @Test
    fun `setLastLoginEpochMillis persists the value`() = runTest {
        storage.setLastLoginEpochMillis(1_700_000_000_000L)

        assertEquals(1_700_000_000_000L, storage.observeLastLoginEpochMillis().first())
    }

    @Test
    fun `observeBiometricEnabled defaults to false`() = runTest {
        assertFalse(storage.observeBiometricEnabled().first())
    }

    @Test
    fun `setBiometricEnabled persists true`() = runTest {
        storage.setBiometricEnabled(true)

        assertTrue(storage.observeBiometricEnabled().first())
    }

    @Test
    fun `observeNotificationsEnabled defaults to true`() = runTest {
        assertTrue(storage.observeNotificationsEnabled().first())
    }

    @Test
    fun `setNotificationsEnabled persists false`() = runTest {
        storage.setNotificationsEnabled(false)

        assertFalse(storage.observeNotificationsEnabled().first())
    }
}
