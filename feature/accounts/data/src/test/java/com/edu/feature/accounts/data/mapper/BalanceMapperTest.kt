package com.edu.feature.accounts.data.mapper

import com.edu.feature.accounts.data.remote.dto.BalanceDto
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.time.Instant

class BalanceMapperTest {

    private val mapper = BalanceMapper()
    private val fetchedAt = Instant.parse("2026-09-16T12:00:00Z")

    @Test
    fun `Should parse the update timestamp`() {
        val dto = BalanceDto(available = BigDecimal.ONE, current = BigDecimal.ONE, updateTimestamp = "2026-09-16T10:00:00Z")

        assertEquals(Instant.parse("2026-09-16T10:00:00Z"), mapper.toDomain("a", dto, fetchedAt).updateTimestamp)
    }

    @Test
    fun `Should fall back to the fetch time When the timestamp is missing`() {
        val dto = BalanceDto(available = BigDecimal.ONE, current = BigDecimal.ONE)

        assertEquals(fetchedAt, mapper.toDomain("a", dto, fetchedAt).updateTimestamp)
    }

    @Test
    fun `Should default a missing overdraft to zero`() {
        val dto = BalanceDto(available = BigDecimal.ONE, current = BigDecimal.ONE)

        assertEquals(BigDecimal.ZERO, mapper.toDomain("a", dto, fetchedAt).overdraft)
    }

    @Test
    fun `Should keep a negative balance`() {
        val dto = BalanceDto(available = BigDecimal("-50.25"), current = BigDecimal("-50.25"))

        assertEquals(BigDecimal("-50.25"), mapper.toDomain("a", dto, fetchedAt).current)
    }

    @Test
    fun `Should round trip through the entity without losing precision`() {
        val domain = mapper.toDomain("a", BalanceDto(available = BigDecimal("44.01"), current = BigDecimal("2.00")), fetchedAt)

        assertEquals(domain, mapper.fromEntity(mapper.toEntity(domain)))
    }
}
