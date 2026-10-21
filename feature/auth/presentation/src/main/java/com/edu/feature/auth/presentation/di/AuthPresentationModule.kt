package com.edu.feature.auth.presentation.di

import com.edu.feature.auth.presentation.bankselection.BankSelectionUiState
import com.edu.feature.auth.presentation.bankselection.BankSelectionViewModel
import com.edu.feature.auth.presentation.biometric.AndroidBiometricAvailabilityChecker
import com.edu.feature.auth.presentation.biometric.BiometricAvailabilityChecker
import com.edu.feature.auth.presentation.biometric.BiometricUiState
import com.edu.feature.auth.presentation.biometric.BiometricViewModel
import com.edu.feature.auth.presentation.connectbank.ConnectBankUiState
import com.edu.feature.auth.presentation.connectbank.ConnectBankViewModel
import com.edu.feature.auth.presentation.connecting.ConnectingUiState
import com.edu.feature.auth.presentation.connecting.ConnectingViewModel
import com.edu.feature.auth.presentation.mapper.BankUiMapper
import com.edu.feature.auth.presentation.mapper.BankUiMapperImpl
import com.edu.feature.auth.presentation.mapper.ConnectionErrorUiMapper
import com.edu.feature.auth.presentation.mapper.ConnectionErrorUiMapperImpl
import com.edu.feature.auth.presentation.mapper.WelcomeUiMapper
import com.edu.feature.auth.presentation.mapper.WelcomeUiMapperImpl
import com.edu.feature.auth.presentation.onboarding.OnboardingUiState
import com.edu.feature.auth.presentation.onboarding.OnboardingViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Auth screens. Use cases and repositories come from :app's appModule;
 * shared formatters (DateFormatter) and a `java.time.Clock` from the graph.
 */
val authPresentationModule = module {
    // Initial states — overridable in tests.
    factory { OnboardingUiState() }
    factory { ConnectBankUiState() }
    factory { BankSelectionUiState() }
    factory { ConnectingUiState() }
    factory { BiometricUiState() }

    // UI mappers
    factory<BankUiMapper> { BankUiMapperImpl() }
    factory<ConnectionErrorUiMapper> { ConnectionErrorUiMapperImpl() }
    factory<WelcomeUiMapper> { WelcomeUiMapperImpl(dateFormatter = get(), clock = get()) }

    factory<BiometricAvailabilityChecker> { AndroidBiometricAvailabilityChecker(androidContext()) }

    viewModel {
        OnboardingViewModel(
            savedStateHandle = get(),
            initialState = get(),
            userPreferencesStorage = get(),
        )
    }
    viewModel {
        ConnectBankViewModel(
            savedStateHandle = get(),
            initialState = get(),
            bankProviderRepository = get(),
            bankMapper = get(),
        )
    }
    viewModel {
        BankSelectionViewModel(
            savedStateHandle = get(),
            initialState = get(),
            searchBanks = get(),
            buildAuthorizationUrl = get(),
            bankMapper = get(),
        )
    }
    viewModel {
        ConnectingViewModel(
            savedStateHandle = get(),
            initialState = get(),
            validateAuthRedirect = get(),
            connectBank = get(),
            errorMapper = get(),
        )
    }
    viewModel {
        BiometricViewModel(
            savedStateHandle = get(),
            initialState = get(),
            getLoginProfile = get(),
            recordSuccessfulLogin = get(),
            authRepository = get(),
            availabilityChecker = get(),
            welcomeMapper = get(),
        )
    }
}
