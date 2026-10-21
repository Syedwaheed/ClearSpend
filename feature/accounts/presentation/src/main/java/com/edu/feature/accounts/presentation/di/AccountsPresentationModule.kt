package com.edu.feature.accounts.presentation.di

import com.edu.feature.accounts.presentation.accounts.AccountsUiState
import com.edu.feature.accounts.presentation.accounts.AccountsViewModel
import com.edu.feature.accounts.presentation.card.CardUiState
import com.edu.feature.accounts.presentation.card.CardViewModel
import com.edu.feature.accounts.presentation.detail.AccountDetailUiState
import com.edu.feature.accounts.presentation.detail.AccountDetailViewModel
import com.edu.feature.accounts.presentation.mapper.AccountDetailUiMapper
import com.edu.feature.accounts.presentation.mapper.AccountDetailUiMapperImpl
import com.edu.feature.accounts.presentation.mapper.AccountRowUiMapper
import com.edu.feature.accounts.presentation.mapper.AccountRowUiMapperImpl
import com.edu.feature.accounts.presentation.mapper.AccountsContentUiMapper
import com.edu.feature.accounts.presentation.mapper.AccountsContentUiMapperImpl
import com.edu.feature.accounts.presentation.mapper.NetPositionUiMapper
import com.edu.feature.accounts.presentation.mapper.NetPositionUiMapperImpl
import com.edu.feature.accounts.presentation.mapper.ProviderGroupUiMapper
import com.edu.feature.accounts.presentation.mapper.ProviderGroupUiMapperImpl
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** Accounts (10), Account Detail (11), Card (21). Use cases come from :app. */
val accountsPresentationModule = module {
    factory { AccountsUiState() }
    factory { AccountDetailUiState() }
    factory { CardUiState() }

    factory<AccountRowUiMapper> { AccountRowUiMapperImpl(accountMapper = get(), amountFormatter = get()) }
    factory<ProviderGroupUiMapper> { ProviderGroupUiMapperImpl(rowMapper = get(), amountFormatter = get()) }
    factory<NetPositionUiMapper> { NetPositionUiMapperImpl(amountFormatter = get()) }
    factory<AccountsContentUiMapper> {
        AccountsContentUiMapperImpl(
            netPositionMapper = get(),
            groupMapper = get(),
            cardMapper = get(),
            syncTimeMapper = get(),
            amountFormatter = get(),
            dateFormatter = get(),
        )
    }
    factory<AccountDetailUiMapper> {
        AccountDetailUiMapperImpl(amountFormatter = get(), ibanFormatter = get(), transactionMapper = get(), syncTimeMapper = get())
    }

    viewModel {
        AccountsViewModel(savedStateHandle = get(), initialState = get(), getAccountsOverview = get(), contentMapper = get())
    }
    viewModel { params ->
        AccountDetailViewModel(
            savedStateHandle = get(),
            initialState = get(),
            accountId = params.get(),
            getAccountDetail = get(),
            detailMapper = get(),
        )
    }
    viewModel {
        CardViewModel(savedStateHandle = get(), initialState = get(), getPrimaryCard = get(), cardMapper = get(), syncTimeMapper = get())
    }
}
