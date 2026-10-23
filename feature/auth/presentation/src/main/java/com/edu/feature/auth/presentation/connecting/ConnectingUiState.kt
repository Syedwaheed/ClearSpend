package com.edu.feature.auth.presentation.connecting

import android.os.Parcelable
import com.edu.feature.auth.presentation.model.ConnectionErrorUi
import kotlinx.parcelize.Parcelize

/**
 * The redirect's `code` and `state` live here so that a process death in the
 * middle of the exchange is recoverable (UiStateMachine → SavedStateHandle).
 * The code is cleared as soon as it can no longer be used.
 */
@Parcelize
data class ConnectingUiState(
    val code: String? = null,
    val state: String? = null,
    val status: ConnectingStatus = ConnectingStatus.WAITING_FOR_REDIRECT,
    val error: ConnectionErrorUi? = null,
) : Parcelable {
    val canRetry: Boolean get() = status == ConnectingStatus.FAILED && code != null && error?.isRetryable == true

    override fun toString(): String =
        "ConnectingUiState(code=${code?.let { "***" }}, state=${state?.let { "***" }}, status=$status, error=$error)"
}

enum class ConnectingStatus {
    WAITING_FOR_REDIRECT,
    VALIDATING,
    EXCHANGING,
    CONNECTED,
    FAILED,
}
