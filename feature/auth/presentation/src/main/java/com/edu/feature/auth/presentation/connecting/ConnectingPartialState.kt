package com.edu.feature.auth.presentation.connecting

import com.edu.feature.auth.presentation.model.ConnectionErrorUi

sealed interface ConnectingPartialState {
    data class RedirectStored(val code: String?, val state: String?) : ConnectingPartialState
    data object ExchangeStarted : ConnectingPartialState
    data object Connected : ConnectingPartialState
    data class Failed(val error: ConnectionErrorUi, val discardCode: Boolean) : ConnectingPartialState
}
