package com.edu.feature.transactions.data.mapper

import com.edu.core.domain.model.TransactionCategory
import com.edu.core.domain.model.TransactionType
import com.edu.feature.transactions.data.remote.dto.RunningBalanceDto
import com.edu.feature.transactions.data.remote.dto.TransactionDto
import com.edu.feature.transactions.data.remote.dto.TransactionMetaDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.Instant

class TransactionMapperTest {

    private val mapper = TransactionMapper()

    private val dto = TransactionDto(
        transactionId = "tx-1",
        timestamp = "2026-09-16T14:32:00+00:00",
        description = "TESCO STORES 3297",
        amount = BigDecimal("-39.99"),
        currency = "GBP",
        transactionType = "DEBIT",
        transactionCategory = "PURCHASE",
        transactionClassification = listOf("Shopping", "Groceries"),
        merchantName = "Tesco",
        runningBalance = RunningBalanceDto(BigDecimal("1200.00"), "GBP"),
    )

    @Test
    fun `Should keep debit amounts negative`() {
        assertEquals(BigDecimal("-39.99"), mapper.toDomain("acc", dto, false)!!.amount)
    }

    @Test
    fun `Should parse the timestamp`() {
        assertEquals(Instant.parse("2026-09-16T14:32:00Z"), mapper.toDomain("acc", dto, false)!!.timestamp)
    }

    @Test
    fun `Should map the category`() {
        assertEquals(TransactionCategory.PURCHASE, mapper.toDomain("acc", dto, false)!!.transactionCategory)
    }

    @Test
    fun `Should map an unknown category to other`() {
        assertEquals(TransactionCategory.OTHER, mapper.toDomain("acc", dto.copy(transactionCategory = "INTEREST"), false)!!.transactionCategory)
    }

    @Test
    fun `Should infer the type from the sign When missing`() {
        assertEquals(TransactionType.CREDIT, mapper.toDomain("acc", dto.copy(transactionType = null, amount = BigDecimal("420")), false)!!.transactionType)
    }

    @Test
    fun `Should allow a null running balance`() {
        assertNull(mapper.toDomain("acc", dto.copy(runningBalance = null), false)!!.runningBalance)
    }

    @Test
    fun `Should drop a row without an amount`() {
        assertNull(mapper.toDomain("acc", dto.copy(amount = null), false))
    }

    @Test
    fun `Should drop a row without a timestamp`() {
        assertNull(mapper.toDomain("acc", dto.copy(timestamp = "garbage"), false))
    }

    @Test
    fun `Should derive a stable id When the id is missing`() {
        val first = mapper.toDomain("acc", dto.copy(transactionId = null), true)!!.transactionId
        val second = mapper.toDomain("acc", dto.copy(transactionId = null), true)!!.transactionId

        assertEquals(first, second)
    }

    @Test
    fun `Should mark pending rows`() {
        assertTrue(mapper.toDomain("acc", dto, true)!!.isPending)
    }

    @Test
    fun `Should round trip through the entity`() {
        val domain = mapper.toDomain("acc", dto, false)!!

        assertEquals(domain, mapper.fromEntity(mapper.toEntity(domain)))
    }

    @Test
    fun `Should keep credit amounts positive`() {
        val credit = dto.copy(amount = BigDecimal("420.00"), transactionType = "CREDIT", transactionCategory = "CREDIT")

        assertEquals(BigDecimal("420.00"), mapper.toDomain("acc", credit, false)!!.amount)
    }

    @Test
    fun `Should read the provider reference from meta`() {
        val withMeta = dto.copy(meta = TransactionMetaDto(providerReference = " RENT JUNE "))

        assertEquals("RENT JUNE", mapper.toDomain("acc", withMeta, false)!!.providerReference)
    }

    @Test
    fun `Should leave the provider reference null When meta is missing`() {
        assertNull(mapper.toDomain("acc", dto, false)!!.providerReference)
    }

    @Test
    fun `Should round trip the provider reference through the entity`() {
        val domain = mapper.toDomain("acc", dto.copy(meta = TransactionMetaDto("REF-1")), false)!!

        assertEquals("REF-1", mapper.fromEntity(mapper.toEntity(domain)).providerReference)
    }
}
