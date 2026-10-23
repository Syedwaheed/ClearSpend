package com.edu.feature.auth.presentation.model

/** Why connecting (screen 07) failed, and whether retrying the same code can help. */
enum class ConnectionErrorUi(val isRetryable: Boolean) {
    /** `state` did not match: possible CSRF, always start over. */
    SECURITY_CHECK_FAILED(isRetryable = false),
    /** User cancelled at the bank or consent was refused. */
    CANCELLED(isRetryable = false),
    /** The code was rejected (expired / already used) or the client is misconfigured. */
    CODE_REJECTED(isRetryable = false),
    NO_INTERNET(isRetryable = true),
    TIMEOUT(isRetryable = true),
    SERVER(isRetryable = true),
    UNKNOWN(isRetryable = true),
}
