package com.edu.feature.accounts.presentation.card

import com.edu.core.presentation.ui.model.CardSummaryUi

sealed interface CardPartialState {
    data object Loading : CardPartialState
    data class CardLoaded(val card: CardSummaryUi?, val offlineSyncTime: String?) : CardPartialState
    data object LoadFailed : CardPartialState
}
