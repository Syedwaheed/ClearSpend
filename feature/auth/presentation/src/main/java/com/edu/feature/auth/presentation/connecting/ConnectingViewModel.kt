package com.edu.feature.auth.presentation.connecting

import androidx.lifecycle.SavedStateHandle
import com.edu.core.domain.util.Result
import com.edu.core.presentation.ui.state.MviViewModel
import com.edu.feature.auth.domain.model.AuthRedirect
import com.edu.feature.auth.domain.usecase.ConnectBankUseCase
import com.edu.feature.auth.domain.usecase.ValidateAuthRedirectUseCase
import com.edu.feature.auth.presentation.mapper.ConnectionErrorUiMapper
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow

class ConnectingViewModel(
    savedStateHandle: SavedStateHandle,
    initialState: ConnectingUiState,
    private val validateAuthRedirect: ValidateAuthRedirectUseCase,
    private val connectBank: ConnectBankUseCase,
    private val errorMapper: ConnectionErrorUiMapper,
) : MviViewModel<ConnectingUiState, ConnectingPartialState, ConnectingAction>(
    savedStateHandle = savedStateHandle,
    initialState = initialState,
) {

    private val _effect = Channel<ConnectingEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        // Nothing loads on a fresh start: work begins when :app forwards the redirect.
        // After process death, resume whatever was in flight.
        if (isStateRestored) {
            when (uiState.value.status) {
                ConnectingStatus.EXCHANGING -> onAction(ConnectingAction.RetryClicked)
                ConnectingStatus.CONNECTED -> onAction(ConnectingAction.ContinueAfterRestore)
                else -> Unit
            }
        }
    }

    override fun handleAction(action: ConnectingAction): Flow<ConnectingPartialState> = when (action) {
        is ConnectingAction.RedirectReceived -> handleRedirect(action)
        ConnectingAction.RetryClicked -> retry()
        ConnectingAction.StartAgainClicked -> flow { _effect.send(ConnectingEffect.NavigateToConnectBank) }
        ConnectingAction.ContinueAfterRestore -> flow { _effect.send(ConnectingEffect.NavigateToDashboard) }
    }

    override fun reduceUiState(
        previousState: ConnectingUiState,
        partialState: ConnectingPartialState,
    ): ConnectingUiState = when (partialState) {
        is ConnectingPartialState.RedirectStored -> previousState.copy(
            code = partialState.code,
            state = partialState.state,
            status = ConnectingStatus.VALIDATING,
            error = null,
        )
        ConnectingPartialState.ExchangeStarted ->
            previousState.copy(status = ConnectingStatus.EXCHANGING, error = null)
        ConnectingPartialState.Connected ->
            previousState.copy(status = ConnectingStatus.CONNECTED, code = null, state = null, error = null)
        is ConnectingPartialState.Failed -> previousState.copy(
            status = ConnectingStatus.FAILED,
            error = partialState.error,
            code = if (partialState.discardCode) null else previousState.code,
            state = if (partialState.discardCode) null else previousState.state,
        )
    }

    private fun handleRedirect(action: ConnectingAction.RedirectReceived): Flow<ConnectingPartialState> {
        val status = uiState.value.status
        // A redirect is single-use: ignore duplicates while one is being processed.
        if (status == ConnectingStatus.VALIDATING || status == ConnectingStatus.EXCHANGING ||
            status == ConnectingStatus.CONNECTED
        ) {
            return emptyFlow()
        }
        return flow {
            emit(ConnectingPartialState.RedirectStored(action.code, action.state))
            when (val validation = validateAuthRedirect(AuthRedirect(action.code, action.state, action.error))) {
                is Result.Error ->
                    emit(ConnectingPartialState.Failed(errorMapper.map(validation.error), discardCode = true))
                is Result.Success -> exchange(validation.data)
            }
        }
    }

    private fun retry(): Flow<ConnectingPartialState> {
        val code = uiState.value.code ?: return emptyFlow()
        return flow { exchange(code) }
    }

    private suspend fun FlowCollector<ConnectingPartialState>.exchange(code: String) {
        emit(ConnectingPartialState.ExchangeStarted)
        when (val result = connectBank(code)) {
            is Result.Success -> {
                emit(ConnectingPartialState.Connected)
                _effect.send(ConnectingEffect.NavigateToDashboard)
            }
            is Result.Error -> {
                val error = errorMapper.map(result.error)
                emit(ConnectingPartialState.Failed(error, discardCode = !error.isRetryable))
            }
        }
    }
}
