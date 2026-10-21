package com.edu.feature.auth.presentation.model

enum class BiometricAvailabilityUi {
    UNKNOWN,
    /** Fingerprint/face enrolled: "Touch to unlock" + PIN fallback. */
    BIOMETRIC_READY,
    /** No usable biometrics, but a PIN/pattern/password exists. */
    DEVICE_CREDENTIAL_ONLY,
    /** No lock screen at all: only "Sign in differently" remains. */
    NONE,
}

/** Which authenticator the BiometricPrompt should offer. */
enum class UnlockMethodUi {
    BIOMETRIC,
    DEVICE_CREDENTIAL,
}

/** Prompt outcomes the screen reports back (mapped from BiometricPrompt error codes). */
enum class UnlockFailureUi {
    CANCELLED,
    LOCKED_OUT,
    UNAVAILABLE,
    OTHER,
}
