package com.edu.feature.auth.data.oauth

import com.edu.feature.auth.data.TrueLayerAuthConfig
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Builds the TrueLayer auth link (CLAUDE.md §8 Step 1, DESIGN_SPEC §1.1).
 * Space-separated lists (scope, providers) are percent-encoded as `%20`.
 */
class AuthUrlBuilder(
    private val config: TrueLayerAuthConfig,
) {
    fun build(providerIds: List<String>, state: String): String {
        val query = listOf(
            "response_type" to "code",
            "client_id" to config.clientId,
            "scope" to config.scopes.joinToString(" "),
            "redirect_uri" to config.redirectUri,
            "providers" to providerIds.joinToString(" "),
            "state" to state,
        ).joinToString("&") { (key, value) -> "$key=${encode(value)}" }
        return config.authBaseUrl.trimEnd('/') + "/?" + query
    }

    private fun encode(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8.name()).replace("+", "%20")
}
