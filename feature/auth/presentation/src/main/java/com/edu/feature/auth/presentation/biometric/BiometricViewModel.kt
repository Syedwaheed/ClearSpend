package com.edu.feature.auth.presentation.biometric

import androidx.lifecycle.SavedStateHandle
import com.edu.core.presentation.ui.state.MviViewModel
import com.edu.feature.auth.domain.repository.AuthRepository
import com.edu.feature.auth.domain.usecase.GetLoginProfileUseCase
import com.edu.feature.auth.domain.usecase.RecordSuccessfulLoginUseCase
import com.edu.feature.auth.presentation.mapper.WelcomeUiMapper
import com.edu.feature.auth.presentation.model.BiometricAvailabilityUi
import com.edu.feature.auth.presentation.model.UnlockFailureUi
import com.edu.feature.auth.presentation.model.UnlockMethodUi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow

class BiometricViewModel(
    savedStateHandle: SavedStateHandle,
    initialState: BiometricUiState,
    private val getLoginProfile: GetLoginProfileUseCase,
    private val recordSuccessfulLogin: RecordSuccessfulLoginUseCase,
    private val authRepository: AuthRepository,
    private val availabilityChecker: BiometricAvailabilityChecker,
    private val welcomeMapper: WelcomeUiMapper,
) : MviViewModel<BiometricUiState, BiometricPartialState, BiometricAction>(
    savedStateHandle = savedStateHandle,
    initialState = initialState,
) {

    private val _effect = Channel<BiometricEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        if (!isStateRestored) onAction(BiometricAction.Load)
    }

    override fun handleAction(action: BiometricAction): Flow<BiometricPartialState> = when (action) {
        BiometricAction.Load -> load()
        BiometricAction.UnlockClicked -> prompt(preferBiometric = true)
        BiometricAction.UsePinClicked -> prompt(preferBiometric = false)
        BiometricAction.AuthenticationSucceeded -> flow {
            emit(BiometricPartialState.FailureChanged(null))
            recordSuccessfulLogin()
            _effect.send(BiometricEffect.NavigateToDashboard)
        }
        is BiometricAction.AuthenticationFailed -> flow {
            // Dismissing the prompt is not an error worth showing.
            emit(BiometricPartialState.FailureChanged(action.failure.takeIf { it != UnlockFailureUi.CANCELLED }))
            if (action.failure == UnlockFailureUi.UNAVAILABLE) {
                emit(BiometricPartialState.AvailabilityChanged(availabilityChecker.check()))
            }
        }
        BiometricAction.SignInDifferentlyClicked -> flow {
            authRepository.signOut()
            _effect.send(BiometricEffect.NavigateToConnectBank)
        }
    }

    override fun reduceUiState(
        previousState: BiometricUiState,
        partialState: BiometricPartialState,
    ): BiometricUiState = when (partialState) {
        is BiometricPartialState.LoadingChanged -> previousState.copy(isLoading = partialState.isLoading)
        is BiometricPartialState.WelcomeLoaded -> previousState.copy(welcome = partialState.welcome)
        is BiometricPartialState.AvailabilityChanged -> previousState.copy(availability = partialState.availability)
        is BiometricPartialState.FailureChanged -> previousState.copy(failure = partialState.failure)
    }

    private fun load(): Flow<BiometricPartialState> = flow {
        emit(BiometricPartialState.LoadingChanged(true))
        emit(BiometricPartialState.WelcomeLoaded(welcomeMapper.map(getLoginProfile())))
        val availability = availabilityChecker.check()
        emit(BiometricPartialState.AvailabilityChanged(availability))
        emit(BiometricPartialState.LoadingChanged(false))
        // Open the prompt straight away when a fingerprint/face is enrolled.
        if (availability == BiometricAvailabilityUi.BIOMETRIC_READY) {
            _effect.send(BiometricEffect.ShowPrompt(UnlockMethodUi.BIOMETRIC))
        }
    }

    private fun prompt(preferBiometric: Boolean): Flow<BiometricPartialState> {
        val state = uiState.value
        val method = when {
            preferBiometric && state.canUseBiometric -> UnlockMethodUi.BIOMETRIC
            state.canUseDeviceCredential -> UnlockMethodUi.DEVICE_CREDENTIAL
            else -> return emptyFlow()
        }
        return flow {
            emit(BiometricPartialState.FailureChanged(null))
            _effect.send(BiometricEffect.ShowPrompt(method))
        }
    }
}
