package com.edu.clearspend.splash

sealed interface SplashPartialState {
    data class ResolvingChanged(val isResolving: Boolean) : SplashPartialState
}
