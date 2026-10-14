package com.edu.core.domain.model

enum class AccountType {
    TRANSACTION,
    SAVINGS,
    BUSINESS_TRANSACTION,
    BUSINESS_SAVINGS,
}

data class Provider(
    val providerId: String,
    val displayName: String,
    val logoUri: String? = null,
)

data class AccountNumber(
    val iban: String? = null,
    val sortCode: String? = null,
    val number: String? = null,
)

data class Account(
    val accountId: String,
    val accountType: AccountType,
    val displayName: String,
    val currency: String = "GBP",
    val provider: Provider,
    val accountNumber: AccountNumber,
)
