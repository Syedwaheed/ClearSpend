package com.edu.feature.auth.domain.model

/** Parameters TrueLayer appends to `clearspend://callback`. */
data class AuthRedirect(
    val code: String?,
    val state: String?,
    val error: String?,
)
