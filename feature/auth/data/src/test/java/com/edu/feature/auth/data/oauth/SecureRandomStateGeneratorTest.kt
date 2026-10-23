package com.edu.feature.auth.data.oauth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SecureRandomStateGeneratorTest {

    private val generator = SecureRandomStateGenerator()

    @Test
    fun `Should generate url-safe values of 32 random bytes`() {
        val state = generator.generate()
        assertEquals(43, state.length)
        assertTrue(state.matches(Regex("[A-Za-z0-9_-]+")))
    }

    @Test
    fun `Should generate a different state each time`() {
        val states = List(50) { generator.generate() }.toSet()
        assertEquals(50, states.size)
    }
}
