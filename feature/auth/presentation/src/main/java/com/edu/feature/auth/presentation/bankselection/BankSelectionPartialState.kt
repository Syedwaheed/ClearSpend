package com.edu.feature.auth.presentation.bankselection

import com.edu.feature.auth.presentation.model.BankUi

sealed interface BankSelectionPartialState {
    data class QueryChanged(val query: String) : BankSelectionPartialState
    data object Loading : BankSelectionPartialState
    data class BanksLoaded(val banks: List<BankUi>) : BankSelectionPartialState
    data object LoadFailed : BankSelectionPartialState
    data class OpeningBankChanged(val isOpening: Boolean) : BankSelectionPartialState
    data class LaunchErrorChanged(val hasError: Boolean) : BankSelectionPartialState
}
