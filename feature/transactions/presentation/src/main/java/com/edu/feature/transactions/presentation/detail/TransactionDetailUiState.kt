package com.edu.feature.transactions.presentation.detail

import android.os.Parcelable
import com.edu.feature.transactions.presentation.model.TransactionDetailUi
import kotlinx.parcelize.Parcelize

@Parcelize
data class TransactionDetailUiState(
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
    val detail: TransactionDetailUi? = null,
    /** "Copied!" on the transaction ID; the Screen sends CopiedFeedbackElapsed after 1.2 s. */
    val isIdCopied: Boolean = false,
) : Parcelable
