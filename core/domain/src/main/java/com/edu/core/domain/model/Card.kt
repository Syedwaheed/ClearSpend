package com.edu.core.domain.model

import java.math.BigDecimal
import java.time.LocalDate

data class Card(
    val cardId: String,
    val cardNetwork: String,
    val cardType: String,
    val displayName: String,
    val partialCardNumber: String,
    val nameOnCard: String,
    val provider: Provider,
)

data class CardBalance(
    val cardId: String,
    val available: BigDecimal,
    val current: BigDecimal,
    val creditLimit: BigDecimal,
    val lastStatementBalance: BigDecimal? = null,
    val paymentDue: BigDecimal? = null,
    val paymentDueDate: LocalDate? = null,
    val currency: String = "GBP",
)
