package com.edu.feature.dashboard.presentation.home

import android.os.Parcelable
import com.edu.feature.dashboard.presentation.model.DashboardContentUi
import kotlinx.parcelize.Parcelize

@Parcelize
data class DashboardUiState(
    /** First load with nothing on screen yet → skeleton. */
    val isLoading: Boolean = false,
    /** Reload while content is already showing. */
    val isRefreshing: Boolean = false,
    val hasError: Boolean = false,
    val content: DashboardContentUi? = null,
) : Parcelable {
    /** Connected, but the bank returned no accounts. */
    val isEmpty: Boolean get() = !isLoading && !hasError && content != null && content.accounts.isEmpty()
}
