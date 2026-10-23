package com.edu.feature.auth.presentation.connecting

sealed interface ConnectingEffect {
    data object NavigateToDashboard : ConnectingEffect
    data object NavigateToConnectBank : ConnectingEffect
}
