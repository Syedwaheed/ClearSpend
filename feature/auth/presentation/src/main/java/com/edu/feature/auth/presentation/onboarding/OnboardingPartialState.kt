package com.edu.feature.auth.presentation.onboarding

sealed interface OnboardingPartialState {
    data class PageChanged(val page: Int) : OnboardingPartialState
}
