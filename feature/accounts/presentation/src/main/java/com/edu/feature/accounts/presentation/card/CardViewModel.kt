package com.edu.feature.accounts.presentation.card

import androidx.lifecycle.SavedStateHandle
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.onError
import com.edu.core.domain.util.onSuccess
import com.edu.core.presentation.ui.mapper.CardSummaryUiMapper
import com.edu.core.presentation.ui.mapper.SyncTimeUiMapper
import com.edu.core.presentation.ui.state.MviViewModel
import com.edu.feature.accounts.domain.usecase.GetPrimaryCardUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Card (21). The tap shutter animation is purely visual and lives in the
 * Composable; this ViewModel only owns the loaded card.
 */
class CardViewModel(
    savedStateHandle: SavedStateHandle,
    initialState: CardUiState,
    private val getPrimaryCard: GetPrimaryCardUseCase,
    private val cardMapper: CardSummaryUiMapper,
    private val syncTimeMapper: SyncTimeUiMapper,
) : MviViewModel<CardUiState, CardPartialState, CardAction>(
    savedStateHandle = savedStateHandle,
    initialState = initialState,
) {

    private val _effect = Channel<CardEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        if (!isStateRestored) onAction(CardAction.LoadData)
    }

    override fun handleAction(action: CardAction): Flow<CardPartialState> = when (action) {
        CardAction.LoadData, CardAction.RetryClicked -> load()
        CardAction.BackClicked -> flow { _effect.send(CardEffect.NavigateBack) }
    }

    override fun reduceUiState(previousState: CardUiState, partialState: CardPartialState): CardUiState = when (partialState) {
        CardPartialState.Loading -> previousState.copy(isLoading = true, hasError = false)
        is CardPartialState.CardLoaded -> previousState.copy(
            isLoading = false,
            hasError = false,
            card = partialState.card,
            hasNoCard = partialState.card == null,
            offlineSyncTime = partialState.offlineSyncTime,
        )
        CardPartialState.LoadFailed -> previousState.copy(isLoading = false, hasError = previousState.card == null)
    }

    private fun load(): Flow<CardPartialState> = flow {
        emit(CardPartialState.Loading)
        getPrimaryCard()
            .onSuccess { detail ->
                emit(
                    CardPartialState.CardLoaded(
                        card = detail.summary?.let(cardMapper::map),
                        offlineSyncTime = detail.offlineSince?.let(syncTimeMapper::map),
                    ),
                )
            }
            .onError { error ->
                if (error == DataError.NetworkError.UNAUTHORIZED) _effect.send(CardEffect.NavigateToConnectBank)
                emit(CardPartialState.LoadFailed)
            }
    }
}
