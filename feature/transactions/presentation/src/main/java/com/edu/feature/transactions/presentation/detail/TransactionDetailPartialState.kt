package com.edu.feature.transactions.presentation.detail

import com.edu.feature.transactions.presentation.model.TransactionDetailUi

sealed interface TransactionDetailPartialState {
    data object Loading : TransactionDetailPartialState
    data class DetailLoaded(val detail: TransactionDetailUi) : TransactionDetailPartialState
    data object LoadFailed : TransactionDetailPartialState
    data class IdCopiedChanged(val copied: Boolean) : TransactionDetailPartialState
}
