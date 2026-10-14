package com.edu.core.domain.model

import java.math.BigDecimal
import java.time.Instant

enum class DirectDebitStatus {
    ACTIVE,
    INACTIVE,
    FAILURE_PENDING,
}

data class DirectDebit(
    val directDebitId: String,
    val accountId: String,
    val name: String,
    val status: DirectDebitStatus,
    val previousPaymentAmount: BigDecimal?,
    val previousPaymentTimestamp: Instant?,
    val currency: String = "GBP",
)
