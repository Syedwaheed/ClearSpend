package com.edu.feature.auth.domain.model

import java.time.Instant

/** What the Biometric screen (08) greets the user with. */
data class LoginProfile(
    val accountHolderName: String?,
    val lastLogin: Instant?,
)
