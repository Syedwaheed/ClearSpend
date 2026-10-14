package com.edu.core.presentation.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BalanceUi(
    val accountId: String,
    val formattedAvailable: String,
    val formattedCurrent: String,
    val formattedOverdraft: String?,
    val hasOverdraft: Boolean,
    val formattedUpdateDate: String,
) : Parcelable
