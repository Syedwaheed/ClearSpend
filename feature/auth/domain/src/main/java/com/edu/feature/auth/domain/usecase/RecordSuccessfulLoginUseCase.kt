package com.edu.feature.auth.domain.usecase

import com.edu.core.domain.storage.UserPreferencesStorage
import java.time.Clock

/** "Last login" (screen 08) is Local: written on every successful unlock or connect. */
class RecordSuccessfulLoginUseCase(
    private val userPreferencesStorage: UserPreferencesStorage,
    private val clock: Clock,
) {
    suspend operator fun invoke() {
        userPreferencesStorage.setLastLoginEpochMillis(clock.millis())
    }
}
