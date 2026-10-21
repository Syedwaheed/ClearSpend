package com.edu.feature.auth.presentation.connectbank

import com.edu.feature.auth.presentation.model.BankUi

sealed interface ConnectBankPartialState {
    data object BanksLoading : ConnectBankPartialState
    data class BanksLoaded(val banks: List<BankUi>) : ConnectBankPartialState
    data object BanksFailed : ConnectBankPartialState
}
