package com.edu.core.presentation.ui.state

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
abstract class MviViewModel<
    UI_STATE : Parcelable,
    UI_PARTIAL_STATE,
    UI_ACTION,
    >(
    savedStateHandle: SavedStateHandle,
    initialState: UI_STATE,
) : ViewModel() {

    private val actions = Channel<UI_ACTION>(Channel.UNLIMITED)

    private val uiStateMachine: UiStateMachine<UI_STATE> =
        savedStateHandle.asUiStateMachine(initialState)

    protected val isStateRestored: Boolean
        get() = uiStateMachine.isStateRestored

    val uiState: StateFlow<UI_STATE> by uiStateMachine

    init {
        viewModelScope.launch {
            actions.receiveAsFlow()
                .flatMapConcat(transform = ::handleAction)
                .scan(uiState.value, ::reduceUiState)
                .collect { newState ->
                    uiStateMachine.update { newState }
                }
        }
    }

    fun onAction(action: UI_ACTION) {
        viewModelScope.launch { actions.send(action) }
    }

    protected abstract fun handleAction(
        action: UI_ACTION,
    ): Flow<UI_PARTIAL_STATE>

    protected abstract fun reduceUiState(
        previousState: UI_STATE,
        partialState: UI_PARTIAL_STATE,
    ): UI_STATE
}
