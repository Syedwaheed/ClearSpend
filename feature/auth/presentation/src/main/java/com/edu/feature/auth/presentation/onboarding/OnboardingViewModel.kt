package com.edu.feature.auth.presentation.onboarding

import androidx.lifecycle.SavedStateHandle
import com.edu.core.domain.storage.UserPreferencesStorage
import com.edu.core.presentation.ui.state.MviViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow

class OnboardingViewModel(
    savedStateHandle: SavedStateHandle,
    initialState: OnboardingUiState,
    private val userPreferencesStorage: UserPreferencesStorage,
) : MviViewModel<OnboardingUiState, OnboardingPartialState, OnboardingAction>(
    savedStateHandle = savedStateHandle,
    initialState = initialState,
) {

    private val _effect = Channel<OnboardingEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    override fun handleAction(action: OnboardingAction): Flow<OnboardingPartialState> = when (action) {
        is OnboardingAction.PageSettled -> flow {
            emit(OnboardingPartialState.PageChanged(action.page.coerceIn(0, uiState.value.pageCount - 1)))
        }
        OnboardingAction.NextClicked -> {
            val state = uiState.value
            if (state.isLastPage) emptyFlow() else flow { emit(OnboardingPartialState.PageChanged(state.currentPage + 1)) }
        }
        OnboardingAction.GetStartedClicked -> finish(OnboardingEffect.NavigateToConnectBank)
        OnboardingAction.SignInClicked -> finish(OnboardingEffect.NavigateToSignIn)
    }

    override fun reduceUiState(
        previousState: OnboardingUiState,
        partialState: OnboardingPartialState,
    ): OnboardingUiState = when (partialState) {
        is OnboardingPartialState.PageChanged -> previousState.copy(currentPage = partialState.page)
    }

    private fun finish(effect: OnboardingEffect): Flow<OnboardingPartialState> = flow {
        userPreferencesStorage.setOnboardingCompleted(true)
        _effect.send(effect)
    }
}
