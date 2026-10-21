package com.edu.feature.auth.presentation.biometric

import com.edu.feature.auth.presentation.model.UnlockFailureUi

sealed interface BiometricAction {
    data object Load : BiometricAction
    data object UnlockClicked : BiometricAction
    data object UsePinClicked : BiometricAction
    data object SignInDifferentlyClicked : BiometricAction
    data object AuthenticationSucceeded : BiometricAction
    data class AuthenticationFailed(val failure: UnlockFailureUi) : BiometricAction
}
