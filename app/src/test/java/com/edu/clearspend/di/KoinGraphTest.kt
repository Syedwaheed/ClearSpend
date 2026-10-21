package com.edu.clearspend.di

import android.content.Context
import com.edu.core.domain.network.TokenProvider
import com.edu.core.domain.storage.SecureDataStorage
import com.edu.core.domain.storage.UserPreferencesStorage
import com.edu.core.network.di.networkModule
import com.edu.core.presentation.ui.di.coreUiModule
import com.edu.feature.auth.data.di.authDataModule
import com.edu.feature.auth.domain.repository.AuthRepository
import com.edu.feature.auth.domain.repository.BankProviderRepository
import com.edu.feature.auth.domain.usecase.BuildAuthorizationUrlUseCase
import com.edu.feature.auth.domain.usecase.ConnectBankUseCase
import com.edu.feature.auth.domain.usecase.DetermineStartDestinationUseCase
import com.edu.feature.auth.domain.usecase.GetLoginProfileUseCase
import com.edu.feature.auth.domain.usecase.SearchBanksUseCase
import com.edu.feature.auth.domain.usecase.ValidateAuthRedirectUseCase
import com.edu.feature.auth.presentation.biometric.BiometricAvailabilityChecker
import com.edu.feature.auth.presentation.connectbank.ConnectBankUiState
import com.edu.feature.auth.presentation.di.authPresentationModule
import com.edu.feature.auth.presentation.mapper.BankUiMapper
import com.edu.feature.auth.presentation.mapper.ConnectionErrorUiMapper
import com.edu.feature.auth.presentation.mapper.WelcomeUiMapper
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

/**
 * Resolves every non-ViewModel definition the auth phase needs, on the JVM.
 * Android-only storage is replaced with mocks; everything else is the real graph.
 */
class KoinGraphTest {

    @After
    fun tearDown() = stopKoin()

    @Test
    fun `Should resolve the auth graph`() {
        val koin = startKoin {
            allowOverride(true)
            androidContext(mockk<Context>(relaxed = true))
            modules(
                appModule,
                networkModule,
                coreUiModule,
                authDataModule,
                authPresentationModule,
                module {
                    single<SecureDataStorage> { mockk(relaxed = true) }
                    single<UserPreferencesStorage> { mockk(relaxed = true) }
                },
            )
        }.koin

        assertNotNull(koin.get<TokenProvider>())
        assertNotNull(koin.get<AuthRepository>())
        assertNotNull(koin.get<BankProviderRepository>())
        assertNotNull(koin.get<DetermineStartDestinationUseCase>())
        assertNotNull(koin.get<BuildAuthorizationUrlUseCase>())
        assertNotNull(koin.get<ValidateAuthRedirectUseCase>())
        assertNotNull(koin.get<ConnectBankUseCase>())
        assertNotNull(koin.get<GetLoginProfileUseCase>())
        assertNotNull(koin.get<SearchBanksUseCase>())
        assertNotNull(koin.get<BankUiMapper>())
        assertNotNull(koin.get<ConnectionErrorUiMapper>())
        assertNotNull(koin.get<WelcomeUiMapper>())
        assertNotNull(koin.get<BiometricAvailabilityChecker>())
        assertNotNull(koin.get<ConnectBankUiState>())
    }
}
