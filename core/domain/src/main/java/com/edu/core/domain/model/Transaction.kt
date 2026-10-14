package com.edu.core.domain.model

import java.time.LocalDate

data class Transaction(
    val id: String,
    val merchantName: String,
    val category: String,
    val amount: Double,
    val currency: String = "GBP",
    val date: LocalDate,
    val runningBalance: Double? = null
)
