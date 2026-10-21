package com.edu.clearspend.splash

sealed interface SplashAction {
    data object ResolveStartDestination : SplashAction
}
