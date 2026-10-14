package com.edu.core.presentation.ui.state

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

interface UiStateMachine<UI_STATE : Parcelable> : ReadOnlyProperty<Any?, StateFlow<UI_STATE>> {

    val isStateRestored: Boolean

    fun update(block: UI_STATE.() -> UI_STATE)
}

class UiStateMachineImpl<UI_STATE : Parcelable>(
    private val savedStateHandle: SavedStateHandle,
    private val key: String,
    fallback: UI_STATE,
) : UiStateMachine<UI_STATE> {

    override val isStateRestored: Boolean
    private val uiState: MutableStateFlow<UI_STATE>

    init {
        val restoredUiState: UI_STATE? = savedStateHandle[key]
        val currentUiState = restoredUiState ?: fallback
        isStateRestored = restoredUiState != null
        uiState = MutableStateFlow(currentUiState)
    }

    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>,
    ): StateFlow<UI_STATE> = uiState

    override fun update(block: UI_STATE.() -> UI_STATE) {
        val newState = uiState.value.block()
        uiState.value = newState
        savedStateHandle[key] = newState
    }
}

fun <UI_STATE : Parcelable> SavedStateHandle.asUiStateMachine(
    fallback: UI_STATE,
    key: String = "ui_state",
): UiStateMachine<UI_STATE> = UiStateMachineImpl(
    savedStateHandle = this,
    key = key,
    fallback = fallback,
)
