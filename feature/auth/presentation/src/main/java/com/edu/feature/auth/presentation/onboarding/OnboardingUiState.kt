package com.edu.feature.auth.presentation.onboarding

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OnboardingUiState(
    val currentPage: Int = 0,
    val pageCount: Int = ONBOARDING_PAGE_COUNT,
) : Parcelable {
    val isLastPage: Boolean get() = currentPage >= pageCount - 1
}

const val ONBOARDING_PAGE_COUNT = 3
