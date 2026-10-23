package com.edu.feature.dashboard.data.mapper

import com.edu.core.domain.model.DirectDebitStatus
import com.edu.feature.dashboard.data.remote.dto.DirectDebitDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal
import java.time.Instant

class DirectDebitMapperTest {

    private val mapper = DirectDebitMapper()
    private val dto = DirectDebitDto(
        directDebitId = "dd-1",
        name = "EDF Energy",
        status = "Active",
        previousPaymentTimestamp = "2026-08-17T00:00:00+00:00",
        previousPaymentAmount = BigDecimal("24.50"),
    )

    @Test
    fun `Should map an active status`() {
        assertEquals(DirectDebitStatus.ACTIVE, mapper.toDomain("acc", dto)!!.status)
    }

    @Test
    fun `Should treat an unknown status as inactive`() {
        assertEquals(DirectDebitStatus.INACTIVE, mapper.toDomain("acc", dto.copy(status = "Cancelled"))!!.status)
    }

    @Test
    fun `Should parse the previous payment timestamp`() {
        assertEquals(Instant.parse("2026-08-17T00:00:00Z"), mapper.toDomain("acc", dto)!!.previousPaymentTimestamp)
    }

    @Test
    fun `Should store amounts as positive`() {
        assertEquals(BigDecimal("24.50"), mapper.toDomain("acc", dto.copy(previousPaymentAmount = BigDecimal("-24.50")))!!.previousPaymentAmount)
    }

    @Test
    fun `Should drop a row without a name`() {
        assertNull(mapper.toDomain("acc", dto.copy(name = " ")))
    }

    @Test
    fun `Should round trip through the entity`() {
        val domain = mapper.toDomain("acc", dto)!!

        assertEquals(domain, mapper.fromEntity(mapper.toEntity(domain)))
    }
}
