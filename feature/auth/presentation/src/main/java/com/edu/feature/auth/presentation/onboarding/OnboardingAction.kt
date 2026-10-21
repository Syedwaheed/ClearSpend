package com.edu.feature.auth.presentation.onboarding

sealed interface OnboardingAction {
    /** The pager settled on [page] after a swipe. */
    data class PageSettled(val page: Int) : OnboardingAction
    data object NextClicked : OnboardingAction
    data object GetStartedClicked : OnboardingAction
    data object SignInClicked : OnboardingAction
}
