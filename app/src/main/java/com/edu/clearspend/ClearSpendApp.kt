package com.edu.clearspend

import android.app.Application
import com.edu.clearspend.di.appModule
import com.edu.core.network.di.networkModule
import com.edu.core.presentation.ui.di.coreUiModule
import com.edu.feature.auth.data.di.authDataModule
import com.edu.feature.auth.presentation.di.authPresentationModule
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
            )
        }
    }
}
