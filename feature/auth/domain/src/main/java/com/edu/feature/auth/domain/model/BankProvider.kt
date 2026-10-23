package com.edu.feature.auth.domain.model

/**
 * A bank the user can pick on screen 06. [providerId] is the TrueLayer
 * provider id that goes into the auth link's `providers=` parameter.
 */
data class BankProvider(
    val providerId: String,
    val displayName: String,
    val segment: BankSegment = BankSegment.PERSONAL,
)

enum class BankSegment {
    PERSONAL,
    BUSINESS,
}
