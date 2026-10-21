package com.edu.feature.transactions.presentation.di

import com.edu.feature.transactions.presentation.detail.TransactionDetailUiState
import com.edu.feature.transactions.presentation.detail.TransactionDetailViewModel
import com.edu.feature.transactions.presentation.list.TransactionsUiState
import com.edu.feature.transactions.presentation.list.TransactionsViewModel
import com.edu.feature.transactions.presentation.mapper.AccountOptionUiMapper
import com.edu.feature.transactions.presentation.mapper.AccountOptionUiMapperImpl
import com.edu.feature.transactions.presentation.mapper.TransactionDetailUiMapper
import com.edu.feature.transactions.presentation.mapper.TransactionDetailUiMapperImpl
import com.edu.feature.transactions.presentation.mapper.TransactionFilterMapper
import com.edu.feature.transactions.presentation.mapper.TransactionFilterMapperImpl
import com.edu.feature.transactions.presentation.mapper.TransactionsContentUiMapper
import com.edu.feature.transactions.presentation.mapper.TransactionsContentUiMapperImpl
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import java.util.Locale

/** Transactions (12/13) and Transaction Detail (14-16). Use cases come from :app. */
val transactionsPresentationModule = module {
    factory { TransactionsUiState() }
    factory { TransactionDetailUiState() }

    factory<TransactionFilterMapper> { TransactionFilterMapperImpl() }
    factory<AccountOptionUiMapper> { AccountOptionUiMapperImpl() }
    factory<TransactionsContentUiMapper> {
        TransactionsContentUiMapperImpl(
            transactionMapper = get(),
            amountFormatter = get(),
            dateFormatter = get(),
            syncTimeMapper = get(),
            locale = Locale.UK,
        )
    }
    factory<TransactionDetailUiMapper> {
        TransactionDetailUiMapperImpl(transactionMapper = get(), amountFormatter = get(), dateFormatter = get(), syncTimeMapper = get())
    }

    viewModel {
        TransactionsViewModel(
            savedStateHandle = get(),
            initialState = get(),
            getTransactionsPage = get(),
            getTransactionsOverview = get(),
            getPeriodBounds = get(),
            getPageWindow = get(),
            filterMapper = get(),
            contentMapper = get(),
            accountOptionMapper = get(),
        )
    }
    viewModel { params ->
        TransactionDetailViewModel(
            savedStateHandle = get(),
            initialState = get(),
            transactionId = params.get(),
            getTransactionDetail = get(),
            detailMapper = get(),
        )
    }
}
