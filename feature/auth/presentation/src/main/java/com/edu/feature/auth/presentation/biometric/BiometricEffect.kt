package com.edu.feature.auth.presentation.biometric

import com.edu.feature.auth.presentation.model.UnlockMethodUi

sealed interface BiometricEffect {
    data class ShowPrompt(val method: UnlockMethodUi) : BiometricEffect
    data object NavigateToDashboard : BiometricEffect
    data object NavigateToConnectBank : BiometricEffect
}
