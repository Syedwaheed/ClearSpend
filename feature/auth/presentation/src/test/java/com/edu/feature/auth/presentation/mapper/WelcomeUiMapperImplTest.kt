package com.edu.feature.auth.presentation.mapper

import com.edu.core.presentation.ui.formatter.DateFormatterImpl
import com.edu.feature.auth.domain.model.LoginProfile
import com.edu.feature.auth.presentation.model.WelcomeUi
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

class WelcomeUiMapperImplTest {

    private val zone = ZoneId.of("Europe/London")
    private val clock = Clock.fixed(Instant.parse("2026-10-21T15:00:00Z"), zone)
    private val mapper = WelcomeUiMapperImpl(DateFormatterImpl(Locale.UK, clock), clock)

    @Test
    fun `Should greet with the first word of the full name`() {
        assertEquals(
            WelcomeUi(firstName = "Jane", avatarInitial = "J", lastLoginLabel = null),
            mapper.map(LoginProfile(accountHolderName = "  Jane   Mary Doe ", lastLogin = null)),
        )
    }

    @Test
    fun `Should label a login earlier today with Today and 24h time`() {
        // 06:42Z is 07:42 BST
        val ui = mapper.map(LoginProfile(null, Instant.parse("2026-10-21T06:42:00Z")))
        assertEquals("Today, 07:42", ui.lastLoginLabel)
    }

    @Test
    fun `Should label a login yesterday with Yesterday`() {
        val ui = mapper.map(LoginProfile(null, Instant.parse("2026-10-20T20:05:00Z")))
        assertEquals("Yesterday, 21:05", ui.lastLoginLabel)
    }

    @Test
    fun `Should use the UK date for older logins`() {
        val ui = mapper.map(LoginProfile(null, Instant.parse("2026-09-16T13:32:00Z")))
        assertEquals("16 Sep 2026, 14:32", ui.lastLoginLabel)
    }

    @Test
    fun `Should leave everything empty When nothing is known`() {
        assertEquals(WelcomeUi(null, null, null), mapper.map(LoginProfile(null, null)))
    }
}
