package com.edu.feature.auth.presentation.biometric

import com.edu.feature.auth.presentation.model.BiometricAvailabilityUi
import com.edu.feature.auth.presentation.model.UnlockFailureUi
import com.edu.feature.auth.presentation.model.WelcomeUi

sealed interface BiometricPartialState {
    data class LoadingChanged(val isLoading: Boolean) : BiometricPartialState
    data class WelcomeLoaded(val welcome: WelcomeUi) : BiometricPartialState
    data class AvailabilityChanged(val availability: BiometricAvailabilityUi) : BiometricPartialState
    data class FailureChanged(val failure: UnlockFailureUi?) : BiometricPartialState
}
