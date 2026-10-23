package com.edu.core.presentation.ui.mapper

import androidx.compose.ui.graphics.toArgb
import com.edu.core.domain.model.Card
import com.edu.core.domain.model.CardBalance
import com.edu.core.domain.model.CardSummary
import com.edu.core.domain.model.CardUtilisation
import com.edu.core.domain.model.Provider
import com.edu.core.domain.model.UtilisationBand
import com.edu.core.presentation.designsystem.Error
import com.edu.core.presentation.designsystem.Success
import com.edu.core.presentation.designsystem.Warning
import com.edu.core.presentation.ui.formatter.AmountFormatterImpl
import com.edu.core.presentation.ui.formatter.DateFormatterImpl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Locale

class CardSummaryUiMapperImplTest {

    private val mapper = CardSummaryUiMapperImpl(
        AmountFormatterImpl(Locale.UK),
        DateFormatterImpl(Locale.UK, Clock.fixed(Instant.parse("2026-09-16T00:00:00Z"), ZoneOffset.UTC)),
    )

    private val card = Card(
        cardId = "card-1",
        cardNetwork = "Mastercard",
        cardType = "CREDIT",
        displayName = "ClearSpend Rewards",
        partialCardNumber = "4471",
        nameOnCard = "J Smith",
        provider = Provider("p", "Bank"),
    )

    private val balance = CardBalance(
        cardId = "card-1",
        available = BigDecimal("3420.00"),
        current = BigDecimal("1580.00"),
        creditLimit = BigDecimal("5000.00"),
        lastStatementBalance = BigDecimal("1850.00"),
        paymentDue = BigDecimal("210.00"),
        paymentDueDate = LocalDate.of(2026, 9, 28),
    )

    private fun summary(band: UtilisationBand = UtilisationBand.LOW) =
        CardSummary(card, balance, CardUtilisation(BigDecimal("0.316"), 32, band))

    @Test
    fun `map masks the card number`() {
        assertEquals("•••• 4471", mapper.map(summary()).maskedNumber)
    }

    @Test
    fun `map upper-cases the network`() {
        assertEquals("MASTERCARD", mapper.map(summary()).network)
    }

    @Test
    fun `map formats balances`() {
        val ui = mapper.map(summary())

        assertEquals(listOf("£3,420.00", "£1,580.00", "£5,000.00", "£1,850.00"),
            listOf(ui.formattedAvailable, ui.formattedCurrent, ui.formattedCreditLimit, ui.formattedLastStatementBalance))
    }

    @Test
    fun `map formats payment due and its date`() {
        val ui = mapper.map(summary())

        assertEquals("£210.00 · 28 Sep 2026", "${ui.formattedPaymentDue} · ${ui.formattedPaymentDueDate}")
    }

    @Test
    fun `map labels utilisation as a percentage`() {
        assertEquals("32%", mapper.map(summary()).utilisationLabel)
    }

    @Test
    fun `map colours low utilisation green`() {
        assertEquals(Success.toArgb(), mapper.map(summary(UtilisationBand.LOW)).utilisationColorArgb)
    }

    @Test
    fun `map colours medium utilisation amber`() {
        assertEquals(Warning.toArgb(), mapper.map(summary(UtilisationBand.MEDIUM)).utilisationColorArgb)
    }

    @Test
    fun `map colours high utilisation red`() {
        assertEquals(Error.toArgb(), mapper.map(summary(UtilisationBand.HIGH)).utilisationColorArgb)
    }

    @Test
    fun `map hides payment due When nothing is due`() {
        val ui = mapper.map(CardSummary(card, balance.copy(paymentDue = BigDecimal.ZERO), null))

        assertFalse(ui.hasPaymentDue)
    }

    @Test
    fun `map leaves balances empty When the balance call failed`() {
        assertNull(mapper.map(CardSummary(card, null, null)).formattedCurrent)
    }
}
