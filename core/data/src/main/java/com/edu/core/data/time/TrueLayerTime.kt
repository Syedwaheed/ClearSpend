package com.edu.core.data.time

import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException

/**
 * TrueLayer sends ISO-8601 timestamps in several shapes
 * (`2026-09-16T10:00:00Z`, `2018-03-06T00:00:00+00:00`, fractional seconds,
 * or a bare date). Returns null instead of throwing so DTO mappers stay total.
 */
fun parseTrueLayerInstant(value: String?): Instant? {
    if (value.isNullOrBlank()) return null
    return try {
        OffsetDateTime.parse(value).toInstant()
    } catch (e: DateTimeParseException) {
        try {
            Instant.parse(value)
        } catch (e: DateTimeParseException) {
            parseTrueLayerDate(value)?.atStartOfDay(java.time.ZoneOffset.UTC)?.toInstant()
        }
    }
}

/** A calendar date from either a bare date or the date part of a timestamp. */
fun parseTrueLayerDate(value: String?): LocalDate? {
    if (value.isNullOrBlank()) return null
    return try {
        LocalDate.parse(value.take(10))
    } catch (e: DateTimeParseException) {
        null
    }
}
