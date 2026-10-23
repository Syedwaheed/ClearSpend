package com.edu.feature.auth.presentation.connectbank

import androidx.lifecycle.SavedStateHandle
import com.edu.core.domain.util.Result
import com.edu.core.presentation.ui.mapper.mapList
import com.edu.core.presentation.ui.state.MviViewModel
import com.edu.feature.auth.domain.repository.BankProviderRepository
import com.edu.feature.auth.presentation.mapper.BankUiMapper
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow

class ConnectBankViewModel(
    savedStateHandle: SavedStateHandle,
    initialState: ConnectBankUiState,
    private val bankProviderRepository: BankProviderRepository,
    private val bankMapper: BankUiMapper,
) : MviViewModel<ConnectBankUiState, ConnectBankPartialState, ConnectBankAction>(
    savedStateHandle = savedStateHandle,
    initialState = initialState,
) {

    private val _effect = Channel<ConnectBankEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        if (!isStateRestored) onAction(ConnectBankAction.LoadBanks)
    }

    override fun handleAction(action: ConnectBankAction): Flow<ConnectBankPartialState> = when (action) {
        ConnectBankAction.LoadBanks, ConnectBankAction.RetryClicked -> loadBanks()
        ConnectBankAction.ConnectClicked -> flow { _effect.send(ConnectBankEffect.NavigateToBankSelection) }
    }

    override fun reduceUiState(
        previousState: ConnectBankUiState,
        partialState: ConnectBankPartialState,
    ): ConnectBankUiState = when (partialState) {
        ConnectBankPartialState.BanksLoading ->
            previousState.copy(isLoadingBanks = true, hasBanksError = false)
        is ConnectBankPartialState.BanksLoaded ->
            previousState.copy(isLoadingBanks = false, featuredBanks = partialState.banks, hasBanksError = false)
        ConnectBankPartialState.BanksFailed ->
            previousState.copy(isLoadingBanks = false, hasBanksError = true)
    }

    private fun loadBanks(): Flow<ConnectBankPartialState> = flow {
        emit(ConnectBankPartialState.BanksLoading)
        when (val result = bankProviderRepository.getFeaturedProviders()) {
            is Result.Success -> emit(ConnectBankPartialState.BanksLoaded(bankMapper.mapList(result.data)))
            is Result.Error -> emit(ConnectBankPartialState.BanksFailed)
        }
    }
}
