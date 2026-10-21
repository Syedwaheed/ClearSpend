package com.edu.clearspend.deeplink

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/** Parameters of `clearspend://callback?code=…&state=…` (or `?error=…`). */
@Parcelize
data class PendingAuthRedirect(
    val code: String?,
    val state: String?,
    val error: String?,
) : Parcelable {
    override fun toString(): String = "PendingAuthRedirect(code=***, state=***, error=$error)"
}

/**
 * Pure-JVM parser (no android.net.Uri) so it is unit-testable. Returns null
 * for anything that is not our OAuth redirect.
 */
object AuthRedirectParser {
    private const val SCHEME = "clearspend"
    private const val HOST = "callback"

    fun parse(uriString: String?): PendingAuthRedirect? {
        if (uriString.isNullOrBlank()) return null
        val uri = try {
            URI(uriString)
        } catch (e: Exception) {
            return null
        }
        if (!SCHEME.equals(uri.scheme, ignoreCase = true) || !HOST.equals(uri.host, ignoreCase = true)) return null
        val params = parseQuery(uri.rawQuery) + parseQuery(uri.rawFragment)
        return PendingAuthRedirect(
            code = params["code"],
            state = params["state"],
            error = params["error"],
        )
    }

    private fun parseQuery(raw: String?): Map<String, String> {
        if (raw.isNullOrEmpty()) return emptyMap()
        return raw.split("&")
            .mapNotNull { pair ->
                val parts = pair.split("=", limit = 2)
                val key = decode(parts[0]).takeIf { it.isNotEmpty() } ?: return@mapNotNull null
                key to decode(parts.getOrElse(1) { "" })
            }
            .toMap()
    }

    private fun decode(value: String): String = URLDecoder.decode(value, StandardCharsets.UTF_8.name())
}
