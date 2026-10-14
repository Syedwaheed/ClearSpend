package com.edu.core.domain.model

import java.math.BigDecimal
import java.time.LocalDate

enum class StandingOrderFrequency {
    WEEKLY,
    MONTHLY,
    ANNUALLY,
    OTHER,
}

data class StandingOrder(
    val standingOrderId: String,
    val accountId: String,
    val payee: String,
    val reference: String?,
    val frequency: StandingOrderFrequency,
    val nextPaymentDate: LocalDate?,
    val nextPaymentAmount: BigDecimal,
    val currency: String = "GBP",
)
