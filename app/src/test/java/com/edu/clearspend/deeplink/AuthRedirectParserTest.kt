package com.edu.clearspend.deeplink

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthRedirectParserTest {

    @Test
    fun `Should read code and state from the callback`() {
        assertEquals(
            PendingAuthRedirect(code = "abc", state = "s1", error = null),
            AuthRedirectParser.parse("clearspend://callback?code=abc&state=s1"),
        )
    }

    @Test
    fun `Should decode percent-encoded values`() {
        assertEquals("a b/c", AuthRedirectParser.parse("clearspend://callback?code=a%20b%2Fc&state=s")?.code)
    }

    @Test
    fun `Should read an error redirect`() {
        assertEquals("access_denied", AuthRedirectParser.parse("clearspend://callback?error=access_denied&state=s")?.error)
    }

    @Test
    fun `Should read parameters from a fragment`() {
        assertEquals("abc", AuthRedirectParser.parse("clearspend://callback#code=abc&state=s")?.code)
    }

    @Test
    fun `Should ignore other schemes`() {
        assertNull(AuthRedirectParser.parse("https://callback?code=abc&state=s"))
    }

    @Test
    fun `Should ignore other hosts`() {
        assertNull(AuthRedirectParser.parse("clearspend://evil?code=abc&state=s"))
    }

    @Test
    fun `Should ignore null and malformed input`() {
        assertNull(AuthRedirectParser.parse(null))
        assertNull(AuthRedirectParser.parse("::not a uri::"))
    }

    @Test
    fun `Should not print the code`() {
        assertEquals(false, PendingAuthRedirect("secret-code", "s", null).toString().contains("secret-code"))
    }
}
