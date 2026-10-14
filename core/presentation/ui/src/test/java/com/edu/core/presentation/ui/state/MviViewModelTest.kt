package com.edu.core.presentation.ui.state

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.parcelize.Parcelize
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@Parcelize
private data class CounterUiState(val values: List<Int> = emptyList()) : Parcelable

private sealed interface CounterPartialState {
    data class ValueAppended(val value: Int) : CounterPartialState
}

private sealed interface CounterAction {
    data class Append(val value: Int, val delayMillis: Long) : CounterAction
}

private class CounterViewModel(
    savedStateHandle: SavedStateHandle,
    initialState: CounterUiState,
) : MviViewModel<CounterUiState, CounterPartialState, CounterAction>(savedStateHandle, initialState) {

    override fun handleAction(action: CounterAction): Flow<CounterPartialState> = when (action) {
        is CounterAction.Append -> flow {
            delay(action.delayMillis)
            emit(CounterPartialState.ValueAppended(action.value))
        }
    }

    override fun reduceUiState(
        previousState: CounterUiState,
        partialState: CounterPartialState,
    ): CounterUiState = when (partialState) {
        is CounterPartialState.ValueAppended ->
            previousState.copy(values = previousState.values + partialState.value)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MviViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `actions are reduced in submission order even When an earlier one resolves slower`() = runTest {
        val viewModel = CounterViewModel(SavedStateHandle(), CounterUiState())

        viewModel.onAction(CounterAction.Append(value = 1, delayMillis = 100))
        viewModel.onAction(CounterAction.Append(value = 2, delayMillis = 0))
        advanceUntilIdle()

        assertEquals(listOf(1, 2), viewModel.uiState.value.values)
    }
}
