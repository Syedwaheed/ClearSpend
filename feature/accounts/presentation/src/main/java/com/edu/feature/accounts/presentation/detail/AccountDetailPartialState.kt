package com.edu.feature.accounts.presentation.detail

import com.edu.feature.accounts.presentation.model.AccountDetailUi
import com.edu.feature.accounts.presentation.model.CopiedFieldUi

sealed interface AccountDetailPartialState {
    data object Loading : AccountDetailPartialState
    data class DetailLoaded(val detail: AccountDetailUi) : AccountDetailPartialState
    data object LoadFailed : AccountDetailPartialState
    data class CopiedChanged(val field: CopiedFieldUi?) : AccountDetailPartialState
}
