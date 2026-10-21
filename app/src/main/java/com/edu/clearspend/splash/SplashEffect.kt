package com.edu.clearspend.splash

sealed interface SplashEffect {
    data object NavigateToOnboarding : SplashEffect
    data object NavigateToConnectBank : SplashEffect
    data object NavigateToBiometric : SplashEffect
    data object NavigateToDashboard : SplashEffect
}
