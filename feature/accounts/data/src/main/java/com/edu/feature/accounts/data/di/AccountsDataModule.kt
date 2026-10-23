package com.edu.feature.accounts.data.di

import com.edu.feature.accounts.data.remote.AccountsApi
import com.edu.feature.accounts.data.remote.CardsApi
import com.edu.feature.accounts.data.remote.InfoApi
import com.edu.feature.accounts.data.remote.TrueLayerAccountsApi
import com.edu.feature.accounts.data.remote.TrueLayerCardsApi
import com.edu.feature.accounts.data.remote.TrueLayerInfoApi
import org.koin.dsl.module

/**
 * API plumbing for the accounts feature. Repositories are bound in :app's
 * appModule (CLAUDE.md §4 Koin Setup); this needs the Data API `HttpClient`.
 */
val accountsDataModule = module {
    single<AccountsApi> { TrueLayerAccountsApi(dataHttpClient = get()) }
    single<CardsApi> { TrueLayerCardsApi(dataHttpClient = get()) }
    single<InfoApi> { TrueLayerInfoApi(dataHttpClient = get()) }
}
