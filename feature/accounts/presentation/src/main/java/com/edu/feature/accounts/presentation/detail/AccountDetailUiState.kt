package com.edu.feature.accounts.presentation.detail

import android.os.Parcelable
import com.edu.feature.accounts.presentation.model.AccountDetailUi
import com.edu.feature.accounts.presentation.model.CopiedFieldUi
import kotlinx.parcelize.Parcelize

@Parcelize
data class AccountDetailUiState(
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
    val detail: AccountDetailUi? = null,
    /** Shows "Copied!" on that row; the Screen sends CopiedFeedbackElapsed after 1.2 s. */
    val copiedField: CopiedFieldUi? = null,
) : Parcelable
