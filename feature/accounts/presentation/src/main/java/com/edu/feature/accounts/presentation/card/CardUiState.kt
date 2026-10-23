package com.edu.feature.accounts.presentation.card

import android.os.Parcelable
import com.edu.core.presentation.ui.model.CardSummaryUi
import kotlinx.parcelize.Parcelize

@Parcelize
data class CardUiState(
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
    /** True once loaded with no card on the connection. */
    val hasNoCard: Boolean = false,
    val card: CardSummaryUi? = null,
    val offlineSyncTime: String? = null,
) : Parcelable
