package com.edu.clearspend.splash

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SplashUiState(
    val isResolving: Boolean = false,
) : Parcelable
