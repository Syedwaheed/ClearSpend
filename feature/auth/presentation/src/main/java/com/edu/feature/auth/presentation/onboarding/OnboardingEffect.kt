package com.edu.feature.auth.presentation.onboarding

sealed interface OnboardingEffect {
    data object NavigateToConnectBank : OnboardingEffect
    data object NavigateToSignIn : OnboardingEffect
}
