package com.edu.core.domain.model

data class Account(
    val id: String,
    val accountType: String,
    val displayName: String,
    val currency: String = "GBP",
    val provider: Provider,
    val accountNumber: AccountNumber
)

data class Provider(
    val providerId: String,
    val displayName: String,
    val logoUrl: String? = null
)

data class AccountNumber(
    val iban: String? = null,
    val sortCode: String? = null,
    val number: String? = null
)
