package com.edu.feature.auth.domain.model

import com.edu.core.domain.util.Error

/** Failures of the OAuth redirect itself (before any network call). */
enum class AuthError : Error {
    /** `state` missing, unknown or different from the one we generated. */
    STATE_MISMATCH,

    /** Redirect carried neither a code nor an error. */
    MISSING_CODE,

    /** The user cancelled or the bank refused consent (`error=` on the redirect). */
    ACCESS_DENIED,
}
