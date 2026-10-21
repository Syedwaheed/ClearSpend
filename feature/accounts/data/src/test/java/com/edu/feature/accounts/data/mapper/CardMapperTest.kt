package com.edu.feature.accounts.data.mapper

import com.edu.feature.accounts.data.remote.dto.CardBalanceDto
import com.edu.feature.accounts.data.remote.dto.CardDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

class CardMapperTest {

    private val mapper = CardMapper()

    @Test
    fun `Should map the card network`() {
        assertEquals("VISA", mapper.toDomain(CardDto(accountId = "c", cardNetwork = "VISA")).cardNetwork)
    }

    @Test
    fun `Should derive available from limit minus current When missing`() {
        val dto = CardBalanceDto(current = BigDecimal("1580"), creditLimit = BigDecimal("5000"))

        assertEquals(BigDecimal("3420"), mapper.toDomain("c", dto).available)
    }

    @Test
    fun `Should leave the due date empty When absent`() {
        assertNull(mapper.toDomain("c", CardBalanceDto()).paymentDueDate)
    }

    @Test
    fun `Should round trip the balance through the entity`() {
        val domain = mapper.toDomain(
            "c",
            CardBalanceDto(
                available = BigDecimal("1"), current = BigDecimal("2"), creditLimit = BigDecimal("3"),
                lastStatementBalance = BigDecimal("4"), paymentDue = BigDecimal("5"), paymentDueDate = "2026-09-28",
            ),
        )

        assertEquals(domain, mapper.fromEntity(mapper.toEntity(domain)))
    }

    @Test
    fun `Should round trip the card through the entity`() {
        val domain = mapper.toDomain(CardDto(accountId = "c", displayName = "Rewards", partialCardNumber = "4471"))

        assertEquals(domain, mapper.fromEntity(mapper.toEntity(domain)))
    }

    @Test
    fun `Should parse the date part of a due timestamp`() {
        assertEquals(LocalDate.of(2026, 9, 28), mapper.toDomain("c", CardBalanceDto(paymentDueDate = "2026-09-28T00:00:00Z")).paymentDueDate)
    }
}
