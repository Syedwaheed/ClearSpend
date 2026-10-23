package com.edu.feature.auth.domain.model

/** What the device currently holds for the TrueLayer session. */
enum class SessionState {
    /** No refresh token stored: the user has never connected (or signed out). */
    NO_SESSION,

    /** Tokens stored and the access token is still valid. */
    ACTIVE,

    /** A refresh token exists but the access token has expired. */
    ACCESS_TOKEN_EXPIRED,
}
