package com.edu.core.domain.model

import java.math.BigDecimal
import java.time.Instant

data class Balance(
    val accountId: String,
    val available: BigDecimal,
    val current: BigDecimal,
    val overdraft: BigDecimal = BigDecimal.ZERO,
    val currency: String = "GBP",
    val updateTimestamp: Instant,
)
