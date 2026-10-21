package com.edu.clearspend.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.edu.clearspend.BuildConfig
import com.edu.clearspend.MainViewModel
import com.edu.clearspend.splash.SplashUiState
import com.edu.clearspend.splash.SplashViewModel
import com.edu.core.data.storage.DataStoreSyncStatusStorage
import com.edu.core.data.storage.DataStoreUserPreferencesStorage
import com.edu.core.data.storage.EncryptedDataStoreSecureStorage
import com.edu.core.domain.repository.AccountRepository
import com.edu.core.domain.repository.BalanceRepository
import com.edu.core.domain.repository.CardRepository
import com.edu.core.domain.repository.DirectDebitRepository
import com.edu.core.domain.repository.StandingOrderRepository
import com.edu.core.domain.repository.TransactionRepository
import com.edu.core.domain.repository.UserInfoRepository
import com.edu.core.domain.storage.SecureDataStorage
import com.edu.core.domain.storage.SyncStatusStorage
import com.edu.core.domain.storage.UserPreferencesStorage
import com.edu.core.domain.usecase.GetAccountsWithBalancesUseCase
import com.edu.core.domain.usecase.GetCardSummariesUseCase
import com.edu.core.domain.usecase.GetCardUtilisationUseCase
import com.edu.feature.accounts.data.repository.AccountRepositoryImpl
import com.edu.feature.accounts.data.repository.BalanceRepositoryImpl
import com.edu.feature.accounts.data.repository.CardRepositoryImpl
import com.edu.feature.accounts.data.repository.UserInfoRepositoryImpl
import com.edu.feature.accounts.domain.usecase.GetAccountDetailUseCase
import com.edu.feature.accounts.domain.usecase.GetAccountsOverviewUseCase
import com.edu.feature.accounts.domain.usecase.GetBalanceHistoryUseCase
import com.edu.feature.accounts.domain.usecase.GetConsentDaysRemainingUseCase
import com.edu.feature.accounts.domain.usecase.GetNetPositionUseCase
import com.edu.feature.accounts.domain.usecase.GetPrimaryCardUseCase
import com.edu.feature.dashboard.data.repository.DirectDebitRepositoryImpl
import com.edu.feature.dashboard.data.repository.StandingOrderRepositoryImpl
import com.edu.feature.dashboard.domain.usecase.GetDashboardUseCase
import com.edu.feature.dashboard.domain.usecase.GetGreetingUseCase
import com.edu.feature.dashboard.domain.usecase.GetMonthlyCashFlowUseCase
import com.edu.feature.dashboard.domain.usecase.GetPaydayUseCase
import com.edu.feature.dashboard.domain.usecase.GetPendingSummaryUseCase
import com.edu.feature.dashboard.domain.usecase.GetRecentTransactionsUseCase
import com.edu.feature.dashboard.domain.usecase.GetSafeToSpendUseCase
import com.edu.feature.dashboard.domain.usecase.GetSpendingByCategoryUseCase
import com.edu.feature.dashboard.domain.usecase.GetUpcomingPaymentsUseCase
import com.edu.feature.transactions.data.repository.TransactionRepositoryImpl
import com.edu.feature.transactions.domain.usecase.GetFilteredTransactionsUseCase
import com.edu.feature.transactions.domain.usecase.GetMerchantInsightUseCase
import com.edu.feature.transactions.domain.usecase.GetPageWindowUseCase
import com.edu.feature.transactions.domain.usecase.GetPeriodBoundsUseCase
import com.edu.feature.transactions.domain.usecase.GetTransactionDayGroupsUseCase
import com.edu.feature.transactions.domain.usecase.GetTransactionDetailUseCase
import com.edu.feature.transactions.domain.usecase.GetTransactionsOverviewUseCase
import com.edu.feature.transactions.domain.usecase.GetTransactionsPageUseCase
import com.edu.feature.transactions.domain.usecase.GetTransactionsSummaryUseCase
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
    single<SyncStatusStorage> { DataStoreSyncStatusStorage(dataStore = get(USER_PREFERENCES), clock = get()) }

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

    // Phase 3 — accounts / cards / info (feature:accounts:data), transactions
    // (feature:transactions:data), direct debits + standing orders (feature:dashboard:data).
    single<AccountRepository> { AccountRepositoryImpl(api = get(), accountDao = get(), syncStatus = get()) }
    single<BalanceRepository> { BalanceRepositoryImpl(api = get(), balanceDao = get(), syncStatus = get(), clock = get()) }
    single<CardRepository> { CardRepositoryImpl(api = get(), cardDao = get(), cardBalanceDao = get(), syncStatus = get()) }
    single<UserInfoRepository> { UserInfoRepositoryImpl(api = get(), secureStorage = get(), syncStatus = get()) }
    single<TransactionRepository> { TransactionRepositoryImpl(api = get(), transactionDao = get(), syncStatus = get(), clock = get()) }
    single<DirectDebitRepository> { DirectDebitRepositoryImpl(api = get(), directDebitDao = get(), syncStatus = get()) }
    single<StandingOrderRepository> { StandingOrderRepositoryImpl(api = get(), standingOrderDao = get(), syncStatus = get()) }

    // --- Use cases (only those with real logic) -----------------------------
    factory { DetermineStartDestinationUseCase(authRepository = get(), userPreferencesStorage = get()) }
    factory { BuildAuthorizationUrlUseCase(authRepository = get(), isSandbox = BuildConfig.TRUELAYER_SANDBOX) }
    factory { ValidateAuthRedirectUseCase(authRepository = get()) }
    factory { RecordSuccessfulLoginUseCase(userPreferencesStorage = get(), clock = get()) }
    factory { ConnectBankUseCase(authRepository = get(), recordSuccessfulLogin = get()) }
    factory { GetLoginProfileUseCase(authRepository = get(), userPreferencesStorage = get()) }
    factory { SearchBanksUseCase(bankProviderRepository = get()) }

    // Phase 3 — shared (core:domain)
    factory { GetCardUtilisationUseCase() }
    factory { GetAccountsWithBalancesUseCase(accountRepository = get(), balanceRepository = get()) }
    factory { GetCardSummariesUseCase(cardRepository = get(), getCardUtilisation = get()) }

    // Phase 3 — dashboard (feature:dashboard:domain)
    factory { GetGreetingUseCase(clock = get()) }
    factory { GetPaydayUseCase(clock = get()) }
    factory { GetUpcomingPaymentsUseCase(clock = get()) }
    factory { GetSafeToSpendUseCase(clock = get()) }
    factory { GetMonthlyCashFlowUseCase(clock = get()) }
    factory { GetPendingSummaryUseCase() }
    factory { GetSpendingByCategoryUseCase(clock = get()) }
    factory { GetRecentTransactionsUseCase() }
    factory {
        GetDashboardUseCase(
            getAccountsWithBalances = get(),
            getCardSummaries = get(),
            transactionRepository = get(),
            directDebitRepository = get(),
            standingOrderRepository = get(),
            userInfoRepository = get(),
            syncStatus = get(),
            getGreeting = get(),
            getPayday = get(),
            getUpcomingPayments = get(),
            getSafeToSpend = get(),
            getMonthlyCashFlow = get(),
            getPendingSummary = get(),
            getSpendingByCategory = get(),
            getRecentTransactions = get(),
            clock = get(),
        )
    }

    // Phase 3 — accounts (feature:accounts:domain)
    factory { GetNetPositionUseCase() }
    factory { GetBalanceHistoryUseCase(clock = get()) }
    factory { GetConsentDaysRemainingUseCase(userPreferencesStorage = get(), clock = get()) }
    factory {
        GetAccountsOverviewUseCase(
            getAccountsWithBalances = get(),
            getCardSummaries = get(),
            transactionRepository = get(),
            getNetPosition = get(),
            getBalanceHistory = get(),
            getConsentDaysRemaining = get(),
            syncStatus = get(),
            clock = get(),
        )
    }
    factory {
        GetAccountDetailUseCase(
            accountRepository = get(),
            balanceRepository = get(),
            transactionRepository = get(),
            syncStatus = get(),
            clock = get(),
        )
    }
    factory { GetPrimaryCardUseCase(getCardSummaries = get(), syncStatus = get()) }

    // Phase 4 — transactions (feature:transactions:domain)
    factory { GetPeriodBoundsUseCase(clock = get()) }
    factory { GetPageWindowUseCase() }
    factory { GetFilteredTransactionsUseCase() }
    factory { GetTransactionDayGroupsUseCase(clock = get()) }
    factory { GetTransactionsSummaryUseCase() }
    factory {
        GetTransactionsOverviewUseCase(
            getPeriodBounds = get(),
            getFilteredTransactions = get(),
            getDayGroups = get(),
            getSummary = get(),
            clock = get(),
        )
    }
    factory { GetTransactionsPageUseCase(accountRepository = get(), transactionRepository = get(), syncStatus = get()) }
    factory { GetMerchantInsightUseCase(clock = get()) }
    factory {
        GetTransactionDetailUseCase(
            transactionRepository = get(),
            accountRepository = get(),
            getMerchantInsight = get(),
            syncStatus = get(),
            clock = get(),
        )
    }

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
