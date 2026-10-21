package com.edu.feature.transactions.data.di

import com.edu.feature.transactions.data.remote.TransactionsApi
import com.edu.feature.transactions.data.remote.TrueLayerTransactionsApi
import org.koin.dsl.module

/** API plumbing; TransactionRepositoryImpl is bound in :app's appModule. */
val transactionsDataModule = module {
    single<TransactionsApi> { TrueLayerTransactionsApi(dataHttpClient = get()) }
}
