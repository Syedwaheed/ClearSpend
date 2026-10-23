package com.edu.core.domain.model

/** An account and its balance; [balance] is null when the balance call failed. */
data class AccountWithBalance(
    val account: Account,
    val balance: Balance?,
)

/** `TRANSACTION` and `BUSINESS_TRANSACTION` count as current accounts (DESIGN_SPEC §4 · 10). */
val AccountType.isCurrent: Boolean
    get() = this == AccountType.TRANSACTION || this == AccountType.BUSINESS_TRANSACTION

/** `SAVINGS` and `BUSINESS_SAVINGS` count as savings accounts (DESIGN_SPEC §4 · 10). */
val AccountType.isSavings: Boolean
    get() = this == AccountType.SAVINGS || this == AccountType.BUSINESS_SAVINGS

/** The primary account is the first current (TRANSACTION) account, else the first account. */
fun List<Account>.primaryAccount(): Account? =
    firstOrNull { it.accountType == AccountType.TRANSACTION }
        ?: firstOrNull { it.accountType.isCurrent }
        ?: firstOrNull()
