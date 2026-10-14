package com.edu.core.domain.model

import java.math.BigDecimal
import java.time.Instant

enum class TransactionType {
    CREDIT,
    DEBIT,
}

enum class TransactionCategory {
    PURCHASE,
    DIRECT_DEBIT,
    BILL_PAYMENT,
    TRANSFER,
    ATM,
    CASH,
    CREDIT,
    OTHER,
}

data class Transaction(
    val transactionId: String,
    val accountId: String,
    val timestamp: Instant,
    val description: String,
    val merchantName: String? = null,
    val amount: BigDecimal,
    val currency: String = "GBP",
    val transactionType: TransactionType,
    val transactionCategory: TransactionCategory,
    val classification: List<String> = emptyList(),
    val runningBalance: BigDecimal? = null,
    val isPending: Boolean = false,
)
