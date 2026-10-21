package com.edu.feature.accounts.presentation.accounts

import androidx.lifecycle.SavedStateHandle
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.onError
import com.edu.core.domain.util.onSuccess
import com.edu.core.presentation.ui.state.MviViewModel
import com.edu.feature.accounts.domain.usecase.GetAccountsOverviewUseCase
import com.edu.feature.accounts.presentation.mapper.AccountsContentUiMapper
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow

class AccountsViewModel(
    savedStateHandle: SavedStateHandle,
    initialState: AccountsUiState,
    private val getAccountsOverview: GetAccountsOverviewUseCase,
    private val contentMapper: AccountsContentUiMapper,
) : MviViewModel<AccountsUiState, AccountsPartialState, AccountsAction>(
    savedStateHandle = savedStateHandle,
    initialState = initialState,
) {

    private val _effect = Channel<AccountsEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        if (!isStateRestored) onAction(AccountsAction.LoadData)
    }

    override fun handleAction(action: AccountsAction): Flow<AccountsPartialState> = when (action) {
        AccountsAction.LoadData, AccountsAction.RetryClicked -> load(asRefresh = false)
        AccountsAction.RefreshClicked -> load(asRefresh = true)
        is AccountsAction.FilterSelected -> flowOf(AccountsPartialState.FilterChanged(action.filter))
        is AccountsAction.AccountSelected -> sendEffect(AccountsEffect.NavigateToAccount(action.accountId))
        AccountsAction.CardSelected -> sendEffect(AccountsEffect.NavigateToCard)
        AccountsAction.RenewConsentClicked, AccountsAction.ConnectAnotherBankClicked ->
            sendEffect(AccountsEffect.NavigateToBankSelection)
    }

    override fun reduceUiState(previousState: AccountsUiState, partialState: AccountsPartialState): AccountsUiState =
        when (partialState) {
            is AccountsPartialState.LoadingChanged -> previousState.copy(isLoading = partialState.isLoading, hasError = false)
            is AccountsPartialState.RefreshingChanged -> previousState.copy(isRefreshing = partialState.isRefreshing)
            is AccountsPartialState.ContentLoaded -> previousState.copy(
                content = partialState.content,
                isLoading = false,
                isRefreshing = false,
                hasError = false,
            )
            AccountsPartialState.LoadFailed -> previousState.copy(
                isLoading = false,
                isRefreshing = false,
                hasError = previousState.content == null,
            )
            is AccountsPartialState.FilterChanged -> previousState.copy(selectedFilter = partialState.filter)
        }

    private fun sendEffect(effect: AccountsEffect): Flow<AccountsPartialState> = flow { _effect.send(effect) }

    private fun load(asRefresh: Boolean): Flow<AccountsPartialState> = flow {
        val refreshing = asRefresh && uiState.value.content != null
        emit(if (refreshing) AccountsPartialState.RefreshingChanged(true) else AccountsPartialState.LoadingChanged(true))
        getAccountsOverview()
            .onSuccess { overview -> emit(AccountsPartialState.ContentLoaded(contentMapper.map(overview))) }
            .onError { error ->
                if (error == DataError.NetworkError.UNAUTHORIZED) _effect.send(AccountsEffect.NavigateToConnectBank)
                emit(AccountsPartialState.LoadFailed)
            }
    }
}
