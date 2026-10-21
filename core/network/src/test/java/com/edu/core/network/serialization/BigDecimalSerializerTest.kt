package com.edu.core.network.serialization

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class BigDecimalSerializerTest {

    @Serializable
    private data class Money(
        @Serializable(with = BigDecimalSerializer::class) val amount: BigDecimal,
    )

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    @Test
    fun `Should decode a negative JSON number without precision loss`() {
        assertEquals(BigDecimal("-46.82"), json.decodeFromString<Money>("""{"amount":-46.82}""").amount)
    }

    @Test
    fun `Should keep many decimal places exactly`() {
        assertEquals(BigDecimal("0.1000000000000000055"), json.decodeFromString<Money>("""{"amount":0.1000000000000000055}""").amount)
    }

    @Test
    fun `Should decode a quoted number`() {
        assertEquals(BigDecimal("12.50"), json.decodeFromString<Money>("""{"amount":"12.50"}""").amount)
    }

    @Test
    fun `Should decode an integer`() {
        assertEquals(BigDecimal("100"), json.decodeFromString<Money>("""{"amount":100}""").amount)
    }
}
