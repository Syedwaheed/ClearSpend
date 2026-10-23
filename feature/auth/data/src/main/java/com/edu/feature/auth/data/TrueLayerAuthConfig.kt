package com.edu.feature.auth.data

/**
 * TrueLayer client settings. Built in :app from BuildConfig, which reads
 * local.properties (sandbox only; see CLAUDE.md §1 security note).
 */
data class TrueLayerAuthConfig(
    val authBaseUrl: String,
    val clientId: String,
    val clientSecret: String,
    val redirectUri: String,
    val scopes: List<String> = DEFAULT_SCOPES,
) {
    override fun toString(): String =
        "TrueLayerAuthConfig(authBaseUrl=$authBaseUrl, clientId=$clientId, clientSecret=***, redirectUri=$redirectUri)"

    companion object {
        /** DESIGN_SPEC §1: `info` and `cards` are needed by Profile and Card screens. */
        val DEFAULT_SCOPES = listOf(
            "info",
            "accounts",
            "balance",
            "cards",
            "transactions",
            "direct_debits",
            "standing_orders",
            "offline_access",
        )
    }
}
