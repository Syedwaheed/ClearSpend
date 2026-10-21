package com.edu.feature.auth.presentation.bankselection

sealed interface BankSelectionEffect {
    data class OpenAuthLink(val url: String) : BankSelectionEffect
    data object NavigateBack : BankSelectionEffect
}
