package com.edu.feature.dashboard.data.mapper

import com.edu.core.domain.model.StandingOrderFrequency
import com.edu.feature.dashboard.data.remote.dto.StandingOrderDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

class StandingOrderMapperTest {

    private val mapper = StandingOrderMapper()
    private val dto = StandingOrderDto(
        frequency = "IntrvlMnthDay:01:19",
        status = "Active",
        nextPaymentDate = "2026-09-19T00:00:00+00:00",
        nextPaymentAmount = BigDecimal("850.00"),
        reference = "RENT",
        payee = "Rent — Landlord",
    )

    @Test
    fun `Should parse a monthly ISO frequency`() {
        assertEquals(StandingOrderFrequency.MONTHLY, mapper.toDomain("acc", dto)!!.frequency)
    }

    @Test
    fun `Should parse a weekly ISO frequency`() {
        assertEquals(StandingOrderFrequency.WEEKLY, mapper.toDomain("acc", dto.copy(frequency = "IntrvlWkDay:01:03"))!!.frequency)
    }

    @Test
    fun `Should fall back to other for an unknown frequency`() {
        assertEquals(StandingOrderFrequency.OTHER, mapper.toDomain("acc", dto.copy(frequency = "EvryWorkgDay"))!!.frequency)
    }

    @Test
    fun `Should parse the next payment date`() {
        assertEquals(LocalDate.of(2026, 9, 19), mapper.toDomain("acc", dto)!!.nextPaymentDate)
    }

    @Test
    fun `Should skip inactive orders`() {
        assertNull(mapper.toDomain("acc", dto.copy(status = "Inactive")))
    }

    @Test
    fun `Should derive a stable id`() {
        assertEquals(mapper.toDomain("acc", dto)!!.standingOrderId, mapper.toDomain("acc", dto)!!.standingOrderId)
    }

    @Test
    fun `Should round trip through the entity`() {
        val domain = mapper.toDomain("acc", dto)!!

        assertEquals(domain, mapper.fromEntity(mapper.toEntity(domain)))
    }
}
