package com.edu.feature.auth.presentation.connectbank

sealed interface ConnectBankEffect {
    data object NavigateToBankSelection : ConnectBankEffect
}
