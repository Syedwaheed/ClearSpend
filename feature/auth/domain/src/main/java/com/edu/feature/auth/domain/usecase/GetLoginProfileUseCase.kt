package com.edu.feature.auth.domain.usecase

import com.edu.core.domain.storage.UserPreferencesStorage
import com.edu.feature.auth.domain.model.LoginProfile
import com.edu.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.first
import java.time.Instant

/** Combines the cached account-holder name with the locally stored last login. */
class GetLoginProfileUseCase(
    private val authRepository: AuthRepository,
    private val userPreferencesStorage: UserPreferencesStorage,
) {
    suspend operator fun invoke(): LoginProfile = LoginProfile(
        accountHolderName = authRepository.getAccountHolderName()?.takeIf { it.isNotBlank() },
        lastLogin = userPreferencesStorage.observeLastLoginEpochMillis().first()
            ?.let(Instant::ofEpochMilli),
    )
}
