package com.edu.core.domain.model

data class Balance(
    val available: Double,
    val current: Double,
    val overdraft: Double = 0.0,
    val currency: String = "GBP"
)
