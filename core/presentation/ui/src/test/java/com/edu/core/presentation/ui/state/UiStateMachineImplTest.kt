package com.edu.core.presentation.ui.state

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.StateFlow
import kotlinx.parcelize.Parcelize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@Parcelize
private data class FakeUiState(val count: Int = 0) : Parcelable

private class StateHolder(savedStateHandle: SavedStateHandle, fallback: FakeUiState) {
    private val machine: UiStateMachine<FakeUiState> = savedStateHandle.asUiStateMachine(fallback)
    val state: StateFlow<FakeUiState> by machine
    val isStateRestored: Boolean get() = machine.isStateRestored
    fun update(block: FakeUiState.() -> FakeUiState) = machine.update(block)
}

class UiStateMachineImplTest {

    @Test
    fun `isStateRestored is false When nothing was previously saved`() {
        val holder = StateHolder(SavedStateHandle(), FakeUiState(count = 0))

        assertFalse(holder.isStateRestored)
        assertEquals(FakeUiState(count = 0), holder.state.value)
    }

    @Test
    fun `isStateRestored is true When SavedStateHandle already has a value for the key`() {
        val savedStateHandle = SavedStateHandle()
        savedStateHandle["ui_state"] = FakeUiState(count = 5)

        val holder = StateHolder(savedStateHandle, FakeUiState(count = 0))

        assertTrue(holder.isStateRestored)
        assertEquals(FakeUiState(count = 5), holder.state.value)
    }

    @Test
    fun `update applies the block and persists the new value into SavedStateHandle`() {
        val savedStateHandle = SavedStateHandle()
        val holder = StateHolder(savedStateHandle, FakeUiState(count = 0))

        holder.update { copy(count = count + 1) }

        assertEquals(FakeUiState(count = 1), holder.state.value)
        assertEquals(FakeUiState(count = 1), savedStateHandle.get<FakeUiState>("ui_state"))
    }
}
