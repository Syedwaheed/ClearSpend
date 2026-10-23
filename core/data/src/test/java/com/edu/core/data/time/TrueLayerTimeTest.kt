package com.edu.core.data.time

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class TrueLayerTimeTest {

    @Test
    fun `Should parse a Z timestamp`() {
        assertEquals(Instant.parse("2026-09-16T10:00:00Z"), parseTrueLayerInstant("2026-09-16T10:00:00Z"))
    }

    @Test
    fun `Should parse an offset timestamp`() {
        assertEquals(Instant.parse("2026-09-15T23:00:00Z"), parseTrueLayerInstant("2026-09-16T00:00:00+01:00"))
    }

    @Test
    fun `Should parse fractional seconds`() {
        assertEquals(Instant.parse("2026-09-16T10:00:00.123Z"), parseTrueLayerInstant("2026-09-16T10:00:00.123Z"))
    }

    @Test
    fun `Should parse a bare date as midnight UTC`() {
        assertEquals(Instant.parse("2026-09-16T00:00:00Z"), parseTrueLayerInstant("2026-09-16"))
    }

    @Test
    fun `Should return null for garbage`() {
        assertNull(parseTrueLayerInstant("not a date"))
    }

    @Test
    fun `Should take the date part of a timestamp`() {
        assertEquals(LocalDate.of(2026, 9, 28), parseTrueLayerDate("2026-09-28T00:00:00+00:00"))
    }

    @Test
    fun `Should return null date for null`() {
        assertNull(parseTrueLayerDate(null))
    }
}
