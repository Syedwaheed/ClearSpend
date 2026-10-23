package com.edu.clearspend.splash

import androidx.lifecycle.SavedStateHandle
import com.edu.core.presentation.ui.state.MviViewModel
import com.edu.feature.auth.domain.model.StartDestination
import com.edu.feature.auth.domain.usecase.DetermineStartDestinationUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlin.time.TimeSource

class SplashViewModel(
    savedStateHandle: SavedStateHandle,
    initialState: SplashUiState,
    private val determineStartDestination: DetermineStartDestinationUseCase,
    /** Keeps the brand splash on screen briefly even when routing is instant. */
    private val minimumDisplayMillis: Long = DEFAULT_MINIMUM_DISPLAY_MILLIS,
) : MviViewModel<SplashUiState, SplashPartialState, SplashAction>(
    savedStateHandle = savedStateHandle,
    initialState = initialState,
) {

    private val _effect = Channel<SplashEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        // Splash always resolves: its only job is routing, and a restored
        // splash has not navigated anywhere yet.
        onAction(SplashAction.ResolveStartDestination)
    }

    override fun handleAction(action: SplashAction): Flow<SplashPartialState> = when (action) {
        SplashAction.ResolveStartDestination -> flow {
            emit(SplashPartialState.ResolvingChanged(true))
            val started = TimeSource.Monotonic.markNow()
            val destination = determineStartDestination()
            val remaining = minimumDisplayMillis - started.elapsedNow().inWholeMilliseconds
            if (remaining > 0) delay(remaining)
            _effect.send(destination.toEffect())
            emit(SplashPartialState.ResolvingChanged(false))
        }
    }

    override fun reduceUiState(
        previousState: SplashUiState,
        partialState: SplashPartialState,
    ): SplashUiState = when (partialState) {
        is SplashPartialState.ResolvingChanged -> previousState.copy(isResolving = partialState.isResolving)
    }

    private fun StartDestination.toEffect(): SplashEffect = when (this) {
        StartDestination.ONBOARDING -> SplashEffect.NavigateToOnboarding
        StartDestination.CONNECT_BANK -> SplashEffect.NavigateToConnectBank
        StartDestination.BIOMETRIC -> SplashEffect.NavigateToBiometric
        StartDestination.DASHBOARD -> SplashEffect.NavigateToDashboard
    }

    companion object {
        const val DEFAULT_MINIMUM_DISPLAY_MILLIS = 900L
    }
}
