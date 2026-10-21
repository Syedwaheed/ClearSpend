package com.edu.feature.auth.presentation.mapper

import com.edu.core.domain.util.DataError
import com.edu.feature.auth.domain.model.AuthError
import com.edu.feature.auth.presentation.model.ConnectionErrorUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConnectionErrorUiMapperImplTest {

    private val mapper = ConnectionErrorUiMapperImpl()

    @Test
    fun `Should treat a state mismatch as a non-retryable security failure`() {
        val ui = mapper.map(AuthError.STATE_MISMATCH)
        assertEquals(ConnectionErrorUi.SECURITY_CHECK_FAILED, ui)
        assertFalse(ui.isRetryable)
    }

    @Test
    fun `Should map access denied to cancelled`() {
        assertEquals(ConnectionErrorUi.CANCELLED, mapper.map(AuthError.ACCESS_DENIED))
    }

    @Test
    fun `Should treat an unauthorized exchange as a rejected code`() {
        assertEquals(ConnectionErrorUi.CODE_REJECTED, mapper.map(DataError.NetworkError.UNAUTHORIZED))
    }

    @Test
    fun `Should make offline retryable`() {
        val ui = mapper.map(DataError.NetworkError.NO_INTERNET)
        assertEquals(ConnectionErrorUi.NO_INTERNET, ui)
        assertTrue(ui.isRetryable)
    }

    @Test
    fun `Should map 5xx and rate limiting to server`() {
        assertEquals(ConnectionErrorUi.SERVER, mapper.map(DataError.NetworkError.SERVER_ERROR))
        assertEquals(ConnectionErrorUi.SERVER, mapper.map(DataError.NetworkError.TOO_MANY_REQUESTS))
    }

    @Test
    fun `Should map anything else to unknown`() {
        assertEquals(ConnectionErrorUi.UNKNOWN, mapper.map(DataError.LocalError.DISKFULL))
    }
}
