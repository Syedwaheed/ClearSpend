package com.edu.feature.auth.presentation.connectbank

sealed interface ConnectBankAction {
    data object LoadBanks : ConnectBankAction
    data object ConnectClicked : ConnectBankAction
    data object RetryClicked : ConnectBankAction
}
