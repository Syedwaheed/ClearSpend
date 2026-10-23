package com.edu.feature.accounts.presentation.card

sealed interface CardAction {
    data object LoadData : CardAction
    data object RetryClicked : CardAction
    data object BackClicked : CardAction
}
