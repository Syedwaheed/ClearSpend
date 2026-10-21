package com.edu.feature.auth.presentation.connecting

sealed interface ConnectingAction {
    /** Forwarded by :app from the `clearspend://callback` deep link. */
    data class RedirectReceived(val code: String?, val state: String?, val error: String?) : ConnectingAction
    data object RetryClicked : ConnectingAction
    data object StartAgainClicked : ConnectingAction
    /** Re-emits navigation after process death restored a finished connection. */
    data object ContinueAfterRestore : ConnectingAction
}
