package com.edu.core.domain.usecase

import com.edu.core.domain.model.CardBalance
import com.edu.core.domain.model.UtilisationBand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

class GetCardUtilisationUseCaseTest {

    private val useCase = GetCardUtilisationUseCase()

    private fun balance(current: String, limit: String) = CardBalance(
        cardId = "card-1",
        available = BigDecimal(limit) - BigDecimal(current),
        current = BigDecimal(current),
        creditLimit = BigDecimal(limit),
    )

    @Test
    fun `Should round utilisation to whole percent`() {
        assertEquals(32, useCase(balance("1580.00", "5000.00"))!!.percent)
    }

    @Test
    fun `Should be low band When under 40 percent`() {
        assertEquals(UtilisationBand.LOW, useCase(balance("1999.99", "5000"))!!.band)
    }

    @Test
    fun `Should be medium band When exactly 40 percent`() {
        assertEquals(UtilisationBand.MEDIUM, useCase(balance("2000", "5000"))!!.band)
    }

    @Test
    fun `Should be high band When exactly 70 percent`() {
        assertEquals(UtilisationBand.HIGH, useCase(balance("3500", "5000"))!!.band)
    }

    @Test
    fun `Should clamp ratio to one When over the limit`() {
        assertEquals(0, BigDecimal.ONE.compareTo(useCase(balance("6000", "5000"))!!.ratio))
    }

    @Test
    fun `Should treat a credit balance as zero utilisation`() {
        assertEquals(0, useCase(balance("-20", "5000"))!!.percent)
    }

    @Test
    fun `Should return null When the card has no credit limit`() {
        assertNull(useCase(balance("10", "0")))
    }
}
