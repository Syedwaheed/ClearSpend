package com.edu.feature.auth.data.oauth

import java.security.SecureRandom
import java.util.Base64

/** Produces the unguessable OAuth `state` value (CSRF protection). */
fun interface StateGenerator {
    fun generate(): String
}

class SecureRandomStateGenerator(
    private val random: SecureRandom = SecureRandom(),
) : StateGenerator {
    override fun generate(): String {
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}
