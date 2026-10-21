package com.edu.feature.dashboard.data.di

import com.edu.feature.dashboard.data.remote.RecurringPaymentsApi
import com.edu.feature.dashboard.data.remote.TrueLayerRecurringPaymentsApi
import org.koin.dsl.module

/** API plumbing; the DD/SO repositories are bound in :app's appModule. */
val dashboardDataModule = module {
    single<RecurringPaymentsApi> { TrueLayerRecurringPaymentsApi(dataHttpClient = get()) }
}
