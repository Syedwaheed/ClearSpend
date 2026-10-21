package com.edu.clearspend

import android.app.Application
import com.edu.clearspend.di.appModule
import com.edu.core.database.di.databaseModule
import com.edu.core.network.di.networkModule
import com.edu.core.presentation.ui.di.coreUiModule
import com.edu.feature.auth.data.di.authDataModule
import com.edu.feature.accounts.data.di.accountsDataModule
import com.edu.feature.accounts.presentation.di.accountsPresentationModule
import com.edu.feature.auth.presentation.di.authPresentationModule
import com.edu.feature.dashboard.data.di.dashboardDataModule
import com.edu.feature.dashboard.presentation.di.dashboardPresentationModule
import com.edu.feature.transactions.data.di.transactionsDataModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class ClearSpendApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
        startKoin {
            androidContext(this@ClearSpendApp)
            modules(
                appModule,
                networkModule,
                coreUiModule,
                authDataModule,
                authPresentationModule,
                databaseModule,
                accountsDataModule,
                transactionsDataModule,
                dashboardDataModule,
                dashboardPresentationModule,
                accountsPresentationModule,
            )
        }
    }
}
