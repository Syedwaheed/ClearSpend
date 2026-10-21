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
import com.edu.core.database.dao.AccountDao
import com.edu.core.database.dao.BalanceDao
import com.edu.core.database.dao.CardBalanceDao
import com.edu.core.database.dao.CardDao
import com.edu.core.database.dao.DirectDebitDao
import com.edu.core.database.dao.StandingOrderDao
import com.edu.core.database.dao.TransactionDao
import com.edu.core.domain.repository.AccountRepository
import com.edu.core.domain.repository.BalanceRepository
import com.edu.core.domain.repository.CardRepository
import com.edu.core.domain.repository.DirectDebitRepository
import com.edu.core.domain.repository.StandingOrderRepository
import com.edu.core.domain.repository.TransactionRepository
import com.edu.core.domain.repository.UserInfoRepository
import com.edu.core.domain.storage.SyncStatusStorage
import com.edu.feature.accounts.data.di.accountsDataModule
import com.edu.feature.accounts.domain.usecase.GetAccountDetailUseCase
import com.edu.feature.accounts.domain.usecase.GetAccountsOverviewUseCase
import com.edu.feature.accounts.domain.usecase.GetPrimaryCardUseCase
import com.edu.feature.accounts.presentation.accounts.AccountsUiState
import com.edu.feature.accounts.presentation.di.accountsPresentationModule
import com.edu.feature.accounts.presentation.mapper.AccountDetailUiMapper
import com.edu.feature.accounts.presentation.mapper.AccountsContentUiMapper
import com.edu.feature.dashboard.data.di.dashboardDataModule
import com.edu.feature.dashboard.domain.usecase.GetDashboardUseCase
import com.edu.feature.dashboard.presentation.di.dashboardPresentationModule
import com.edu.feature.dashboard.presentation.home.DashboardUiState
import com.edu.feature.dashboard.presentation.mapper.DashboardContentUiMapper
import com.edu.feature.transactions.data.di.transactionsDataModule
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

    @Test
    fun `Should resolve the dashboard and accounts graph`() {
        val koin = startKoin {
            allowOverride(true)
            androidContext(mockk<Context>(relaxed = true))
            modules(
                appModule,
                networkModule,
                coreUiModule,
                authDataModule,
                accountsDataModule,
                transactionsDataModule,
                dashboardDataModule,
                dashboardPresentationModule,
                accountsPresentationModule,
                module {
                    single<SecureDataStorage> { mockk(relaxed = true) }
                    single<UserPreferencesStorage> { mockk(relaxed = true) }
                    single<SyncStatusStorage> { mockk(relaxed = true) }
                    // Room needs a device; the DAOs are mocked (databaseModule is covered on device).
                    single<AccountDao> { mockk(relaxed = true) }
                    single<BalanceDao> { mockk(relaxed = true) }
                    single<CardDao> { mockk(relaxed = true) }
                    single<CardBalanceDao> { mockk(relaxed = true) }
                    single<TransactionDao> { mockk(relaxed = true) }
                    single<DirectDebitDao> { mockk(relaxed = true) }
                    single<StandingOrderDao> { mockk(relaxed = true) }
                },
            )
        }.koin

        assertNotNull(koin.get<AccountRepository>())
        assertNotNull(koin.get<BalanceRepository>())
        assertNotNull(koin.get<CardRepository>())
        assertNotNull(koin.get<UserInfoRepository>())
        assertNotNull(koin.get<TransactionRepository>())
        assertNotNull(koin.get<DirectDebitRepository>())
        assertNotNull(koin.get<StandingOrderRepository>())
        assertNotNull(koin.get<GetDashboardUseCase>())
        assertNotNull(koin.get<GetAccountsOverviewUseCase>())
        assertNotNull(koin.get<GetAccountDetailUseCase>())
        assertNotNull(koin.get<GetPrimaryCardUseCase>())
        assertNotNull(koin.get<DashboardContentUiMapper>())
        assertNotNull(koin.get<AccountsContentUiMapper>())
        assertNotNull(koin.get<AccountDetailUiMapper>())
        assertNotNull(koin.get<DashboardUiState>())
        assertNotNull(koin.get<AccountsUiState>())
    }
}
