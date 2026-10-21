package com.edu.clearspend.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.edu.clearspend.BuildConfig
import com.edu.clearspend.MainViewModel
import com.edu.clearspend.splash.SplashUiState
import com.edu.clearspend.splash.SplashViewModel
import com.edu.core.data.storage.DataStoreUserPreferencesStorage
import com.edu.core.data.storage.EncryptedDataStoreSecureStorage
import com.edu.core.domain.storage.SecureDataStorage
import com.edu.core.domain.storage.UserPreferencesStorage
import com.edu.core.network.ApiConstants
import com.edu.core.network.NetworkConfig
import com.edu.feature.auth.data.TrueLayerAuthConfig
import com.edu.feature.auth.data.repository.AuthRepositoryImpl
import com.edu.feature.auth.data.repository.StaticBankProviderRepository
import com.edu.feature.auth.domain.repository.AuthRepository
import com.edu.feature.auth.domain.repository.BankProviderRepository
import com.edu.feature.auth.domain.usecase.BuildAuthorizationUrlUseCase
import com.edu.feature.auth.domain.usecase.ConnectBankUseCase
import com.edu.feature.auth.domain.usecase.DetermineStartDestinationUseCase
import com.edu.feature.auth.domain.usecase.GetLoginProfileUseCase
import com.edu.feature.auth.domain.usecase.RecordSuccessfulLoginUseCase
import com.edu.feature.auth.domain.usecase.SearchBanksUseCase
import com.edu.feature.auth.domain.usecase.ValidateAuthRedirectUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.time.Clock

private val USER_PREFERENCES = named("user_preferences_datastore")
private const val DEFAULT_REDIRECT_URI = "clearspend://callback"

/**
 * The ONLY place repository implementations are bound and use cases are
 * registered (CLAUDE.md §4 Koin Setup). Credentials come from BuildConfig ←
 * local.properties (sandbox only, CLAUDE.md §1).
 */
val appModule = module {
    single<Clock> { Clock.systemDefaultZone() }

    // --- Config -------------------------------------------------------------
    single {
        NetworkConfig(
            baseUrl = (if (BuildConfig.TRUELAYER_SANDBOX) ApiConstants.SANDBOX_DATA_BASE_URL else ApiConstants.PRODUCTION_DATA_BASE_URL) + "/",
        )
    }
    single {
        TrueLayerAuthConfig(
            authBaseUrl = if (BuildConfig.TRUELAYER_SANDBOX) ApiConstants.SANDBOX_AUTH_BASE_URL else ApiConstants.PRODUCTION_AUTH_BASE_URL,
            clientId = BuildConfig.TRUELAYER_CLIENT_ID,
            clientSecret = BuildConfig.TRUELAYER_CLIENT_SECRET,
            redirectUri = BuildConfig.TRUELAYER_REDIRECT_URI.ifBlank { DEFAULT_REDIRECT_URI },
        )
    }

    // --- Storage (core:data) -----------------------------------------------
    single<SecureDataStorage> { EncryptedDataStoreSecureStorage(androidContext()) }
    single<DataStore<Preferences>>(USER_PREFERENCES) {
        PreferenceDataStoreFactory.create { androidContext().preferencesDataStoreFile("user_preferences") }
    }
    single<UserPreferencesStorage> { DataStoreUserPreferencesStorage(get(USER_PREFERENCES)) }

    // --- Repositories --------------------------------------------------------
    single<AuthRepository> {
        AuthRepositoryImpl(
            authApi = get(),
            userInfoApi = get(),
            tokenStore = get(),
            tokenManager = get(),
            tokenMapper = get(),
            authUrlBuilder = get(),
            stateGenerator = get(),
            userPreferencesStorage = get(),
            clock = get(),
        )
    }
    single<BankProviderRepository> { StaticBankProviderRepository() }

    // --- Use cases (only those with real logic) -----------------------------
    factory { DetermineStartDestinationUseCase(authRepository = get(), userPreferencesStorage = get()) }
    factory { BuildAuthorizationUrlUseCase(authRepository = get(), isSandbox = BuildConfig.TRUELAYER_SANDBOX) }
    factory { ValidateAuthRedirectUseCase(authRepository = get()) }
    factory { RecordSuccessfulLoginUseCase(userPreferencesStorage = get(), clock = get()) }
    factory { ConnectBankUseCase(authRepository = get(), recordSuccessfulLogin = get()) }
    factory { GetLoginProfileUseCase(authRepository = get(), userPreferencesStorage = get()) }
    factory { SearchBanksUseCase(bankProviderRepository = get()) }

    // --- :app screens ---------------------------------------------------------
    factory { SplashUiState() }
    viewModel {
        SplashViewModel(
            savedStateHandle = get(),
            initialState = get(),
            determineStartDestination = get(),
        )
    }
    viewModel { MainViewModel(savedStateHandle = get()) }
}
