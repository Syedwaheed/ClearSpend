package com.edu.feature.accounts.presentation.card

sealed interface CardEffect {
    data object NavigateBack : CardEffect
    data object NavigateToConnectBank : CardEffect
}
