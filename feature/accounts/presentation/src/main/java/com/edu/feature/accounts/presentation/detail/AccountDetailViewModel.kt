package com.edu.feature.accounts.presentation.detail

import androidx.lifecycle.SavedStateHandle
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.onError
import com.edu.core.domain.util.onSuccess
import com.edu.core.presentation.ui.state.MviViewModel
import com.edu.feature.accounts.domain.usecase.GetAccountDetailUseCase
import com.edu.feature.accounts.presentation.mapper.AccountDetailUiMapper
import com.edu.feature.accounts.presentation.model.CopiedFieldUi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow

class AccountDetailViewModel(
    savedStateHandle: SavedStateHandle,
    initialState: AccountDetailUiState,
    private val accountId: String,
    private val getAccountDetail: GetAccountDetailUseCase,
    private val detailMapper: AccountDetailUiMapper,
) : MviViewModel<AccountDetailUiState, AccountDetailPartialState, AccountDetailAction>(
    savedStateHandle = savedStateHandle,
    initialState = initialState,
) {

    private val _effect = Channel<AccountDetailEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        if (!isStateRestored) onAction(AccountDetailAction.LoadData)
    }

    override fun handleAction(action: AccountDetailAction): Flow<AccountDetailPartialState> = when (action) {
        AccountDetailAction.LoadData, AccountDetailAction.RetryClicked -> load()
        AccountDetailAction.BackClicked -> sendEffect(AccountDetailEffect.NavigateBack)
        AccountDetailAction.CopySortCodeClicked ->
            copy(CopiedFieldUi.SORT_CODE, LABEL_SORT_CODE, uiState.value.detail?.sortCode)
        AccountDetailAction.CopyAccountNumberClicked ->
            copy(CopiedFieldUi.ACCOUNT_NUMBER, LABEL_ACCOUNT_NUMBER, uiState.value.detail?.accountNumber)
        is AccountDetailAction.CopiedFeedbackElapsed ->
            // Only clear if no newer copy replaced it in the meantime.
            if (uiState.value.copiedField == action.field) flowOf(AccountDetailPartialState.CopiedChanged(null)) else emptyFlow()
        is AccountDetailAction.TransactionSelected -> sendEffect(AccountDetailEffect.NavigateToTransaction(action.transactionId))
        AccountDetailAction.ViewAllTransactionsClicked -> sendEffect(AccountDetailEffect.NavigateToTransactions)
    }

    override fun reduceUiState(
        previousState: AccountDetailUiState,
        partialState: AccountDetailPartialState,
    ): AccountDetailUiState = when (partialState) {
        AccountDetailPartialState.Loading -> previousState.copy(isLoading = true, hasError = false)
        is AccountDetailPartialState.DetailLoaded -> previousState.copy(isLoading = false, hasError = false, detail = partialState.detail)
        AccountDetailPartialState.LoadFailed -> previousState.copy(isLoading = false, hasError = previousState.detail == null)
        is AccountDetailPartialState.CopiedChanged -> previousState.copy(copiedField = partialState.field)
    }

    private fun sendEffect(effect: AccountDetailEffect): Flow<AccountDetailPartialState> = flow { _effect.send(effect) }

    private fun copy(field: CopiedFieldUi, label: String, value: String?): Flow<AccountDetailPartialState> = flow {
        if (value == null) return@flow
        _effect.send(AccountDetailEffect.CopyToClipboard(label, value))
        emit(AccountDetailPartialState.CopiedChanged(field))
    }

    private fun load(): Flow<AccountDetailPartialState> = flow {
        emit(AccountDetailPartialState.Loading)
        getAccountDetail(accountId)
            .onSuccess { detail -> emit(AccountDetailPartialState.DetailLoaded(detailMapper.map(detail))) }
            .onError { error ->
                if (error == DataError.NetworkError.UNAUTHORIZED) _effect.send(AccountDetailEffect.NavigateToConnectBank)
                emit(AccountDetailPartialState.LoadFailed)
            }
    }

    private companion object {
        const val LABEL_SORT_CODE = "Sort code"
        const val LABEL_ACCOUNT_NUMBER = "Account number"
    }
}
