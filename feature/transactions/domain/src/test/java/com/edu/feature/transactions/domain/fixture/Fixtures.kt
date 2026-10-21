package com.edu.feature.transactions.domain.fixture

import com.edu.core.domain.model.Account
import com.edu.core.domain.model.AccountNumber
import com.edu.core.domain.model.AccountType
import com.edu.core.domain.model.Provider
import com.edu.core.domain.model.Transaction
import com.edu.core.domain.model.TransactionCategory
import com.edu.core.domain.model.TransactionType
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

/** "Now" for every test: Monday 15 June 2026, 09:30 UTC. */
val NOW: Instant = Instant.parse("2026-06-15T09:30:00Z")
val TODAY: LocalDate = LocalDate.of(2026, 6, 15)
val CLOCK: Clock = Clock.fixed(NOW, ZoneOffset.UTC)

fun clockAt(instant: String, zone: ZoneId = ZoneOffset.UTC): Clock = Clock.fixed(Instant.parse(instant), zone)

/** A transaction; the type follows the sign of [amount] (debits are negative in the API). */
fun tx(
    id: String,
    timestamp: String = "2026-06-10T12:00:00Z",
    amount: String = "-10.00",
    merchant: String? = "Tesco",
    description: String = "CARD PAYMENT",
    category: TransactionCategory = TransactionCategory.PURCHASE,
    accountId: String = "acc-1",
    pending: Boolean = false,
): Transaction {
    val value = BigDecimal(amount)
    return Transaction(
        transactionId = id,
        accountId = accountId,
        timestamp = Instant.parse(timestamp),
        description = description,
        merchantName = merchant,
        amount = value,
        transactionType = if (value.signum() < 0) TransactionType.DEBIT else TransactionType.CREDIT,
        transactionCategory = category,
        isPending = pending,
    )
}

fun account(id: String, name: String = "Current $id") = Account(
    accountId = id,
    accountType = AccountType.TRANSACTION,
    displayName = name,
    provider = Provider("ob-mock", "Mock Bank"),
    accountNumber = AccountNumber(sortCode = "01-21-31", number = "12345678"),
)
