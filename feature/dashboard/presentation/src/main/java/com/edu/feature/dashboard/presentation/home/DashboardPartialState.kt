package com.edu.feature.dashboard.presentation.home

import com.edu.feature.dashboard.presentation.model.DashboardContentUi

sealed interface DashboardPartialState {
    data class LoadingChanged(val isLoading: Boolean) : DashboardPartialState
    data class RefreshingChanged(val isRefreshing: Boolean) : DashboardPartialState
    data class ContentLoaded(val content: DashboardContentUi) : DashboardPartialState
    data object LoadFailed : DashboardPartialState
}
