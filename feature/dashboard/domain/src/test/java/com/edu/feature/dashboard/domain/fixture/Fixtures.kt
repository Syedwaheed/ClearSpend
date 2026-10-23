package com.edu.feature.dashboard.domain.fixture

import com.edu.core.domain.model.Account
import com.edu.core.domain.model.AccountNumber
import com.edu.core.domain.model.AccountType
import com.edu.core.domain.model.AccountWithBalance
import com.edu.core.domain.model.Balance
import com.edu.core.domain.model.DirectDebit
import com.edu.core.domain.model.DirectDebitStatus
import com.edu.core.domain.model.Provider
import com.edu.core.domain.model.StandingOrder
import com.edu.core.domain.model.StandingOrderFrequency
import com.edu.core.domain.model.Transaction
import com.edu.core.domain.model.TransactionCategory
import com.edu.core.domain.model.TransactionType
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/** Wednesday 16 Sep 2026, 09:30 UTC. */
val NOW: Instant = Instant.parse("2026-09-16T09:30:00Z")
val TODAY: LocalDate = LocalDate.of(2026, 9, 16)
val CLOCK: Clock = Clock.fixed(NOW, ZoneOffset.UTC)

fun clockAt(iso: String): Clock = Clock.fixed(Instant.parse(iso), ZoneOffset.UTC)

fun account(id: String, type: AccountType = AccountType.TRANSACTION) = Account(
    accountId = id,
    accountType = type,
    displayName = id,
    provider = Provider("p", "Bank"),
    accountNumber = AccountNumber(),
)

fun withBalance(id: String, available: String, overdraft: String = "0", type: AccountType = AccountType.TRANSACTION) =
    AccountWithBalance(
        account(id, type),
        Balance(id, BigDecimal(available), BigDecimal(available), BigDecimal(overdraft), updateTimestamp = NOW),
    )

fun tx(
    id: String,
    date: String,
    amount: String,
    category: TransactionCategory = TransactionCategory.PURCHASE,
    classification: List<String> = emptyList(),
    merchant: String? = null,
    description: String = merchant ?: id,
    pending: Boolean = false,
    runningBalance: String? = null,
) = Transaction(
    transactionId = id,
    accountId = "acc",
    timestamp = Instant.parse(if (date.length == 10) "${date}T12:00:00Z" else date),
    description = description,
    merchantName = merchant,
    amount = BigDecimal(amount),
    transactionType = if (BigDecimal(amount).signum() < 0) TransactionType.DEBIT else TransactionType.CREDIT,
    transactionCategory = category,
    classification = classification,
    runningBalance = runningBalance?.let(::BigDecimal),
    isPending = pending,
)

fun salary(date: String, amount: String = "2000.00") =
    tx("sal-$date", date, amount, TransactionCategory.CREDIT, merchant = "ACME LTD")

fun directDebit(
    id: String,
    previous: String?,
    amount: String? = "24.50",
    status: DirectDebitStatus = DirectDebitStatus.ACTIVE,
) = DirectDebit(
    directDebitId = id,
    accountId = "acc",
    name = id,
    status = status,
    previousPaymentAmount = amount?.let(::BigDecimal),
    previousPaymentTimestamp = previous?.let { Instant.parse("${it}T00:00:00Z") },
)

fun standingOrder(
    id: String,
    next: String?,
    amount: String = "850.00",
    frequency: StandingOrderFrequency = StandingOrderFrequency.MONTHLY,
) = StandingOrder(
    standingOrderId = id,
    accountId = "acc",
    payee = id,
    reference = null,
    frequency = frequency,
    nextPaymentDate = next?.let(LocalDate::parse),
    nextPaymentAmount = BigDecimal(amount),
)
