package com.edu.feature.auth.presentation.bankselection

import androidx.lifecycle.SavedStateHandle
import com.edu.core.domain.util.Result
import com.edu.core.presentation.ui.mapper.mapList
import com.edu.core.presentation.ui.state.MviViewModel
import com.edu.feature.auth.domain.usecase.BuildAuthorizationUrlUseCase
import com.edu.feature.auth.domain.usecase.SearchBanksUseCase
import com.edu.feature.auth.presentation.mapper.BankUiMapper
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow

class BankSelectionViewModel(
    savedStateHandle: SavedStateHandle,
    initialState: BankSelectionUiState,
    private val searchBanks: SearchBanksUseCase,
    private val buildAuthorizationUrl: BuildAuthorizationUrlUseCase,
    private val bankMapper: BankUiMapper,
) : MviViewModel<BankSelectionUiState, BankSelectionPartialState, BankSelectionAction>(
    savedStateHandle = savedStateHandle,
    initialState = initialState,
) {

    private val _effect = Channel<BankSelectionEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        if (!isStateRestored) onAction(BankSelectionAction.LoadBanks)
    }

    override fun handleAction(action: BankSelectionAction): Flow<BankSelectionPartialState> = when (action) {
        BankSelectionAction.LoadBanks, BankSelectionAction.RetryClicked -> search(uiState.value.query, showLoading = true)
        is BankSelectionAction.QueryChanged -> search(action.query, showLoading = false)
        BankSelectionAction.ClearQueryClicked -> search("", showLoading = false)
        is BankSelectionAction.BankSelected -> openAuthLink(action.providerId)
        BankSelectionAction.OtherBankClicked -> openAuthLink(providerId = null)
        BankSelectionAction.BackClicked -> flow { _effect.send(BankSelectionEffect.NavigateBack) }
        BankSelectionAction.BrowserUnavailable -> flow { emit(BankSelectionPartialState.LaunchErrorChanged(true)) }
    }

    override fun reduceUiState(
        previousState: BankSelectionUiState,
        partialState: BankSelectionPartialState,
    ): BankSelectionUiState = when (partialState) {
        is BankSelectionPartialState.QueryChanged -> previousState.copy(query = partialState.query)
        BankSelectionPartialState.Loading -> previousState.copy(isLoading = true, hasError = false)
        is BankSelectionPartialState.BanksLoaded ->
            previousState.copy(isLoading = false, hasError = false, banks = partialState.banks)
        BankSelectionPartialState.LoadFailed -> previousState.copy(isLoading = false, hasError = true)
        is BankSelectionPartialState.OpeningBankChanged -> previousState.copy(isOpeningBank = partialState.isOpening)
        is BankSelectionPartialState.LaunchErrorChanged -> previousState.copy(hasLaunchError = partialState.hasError)
    }

    private fun search(query: String, showLoading: Boolean): Flow<BankSelectionPartialState> = flow {
        emit(BankSelectionPartialState.QueryChanged(query))
        if (showLoading) emit(BankSelectionPartialState.Loading)
        when (val result = searchBanks(query)) {
            is Result.Success -> emit(BankSelectionPartialState.BanksLoaded(bankMapper.mapList(result.data)))
            is Result.Error -> emit(BankSelectionPartialState.LoadFailed)
        }
    }

    private fun openAuthLink(providerId: String?): Flow<BankSelectionPartialState> = flow {
        emit(BankSelectionPartialState.LaunchErrorChanged(false))
        emit(BankSelectionPartialState.OpeningBankChanged(true))
        when (val result = buildAuthorizationUrl(providerId)) {
            is Result.Success -> _effect.send(BankSelectionEffect.OpenAuthLink(result.data))
            is Result.Error -> emit(BankSelectionPartialState.LaunchErrorChanged(true))
        }
        emit(BankSelectionPartialState.OpeningBankChanged(false))
    }
}
