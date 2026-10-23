package com.edu.feature.accounts.domain.fixture

import com.edu.core.domain.model.Account
import com.edu.core.domain.model.AccountNumber
import com.edu.core.domain.model.AccountType
import com.edu.core.domain.model.AccountWithBalance
import com.edu.core.domain.model.Balance
import com.edu.core.domain.model.Card
import com.edu.core.domain.model.CardBalance
import com.edu.core.domain.model.CardSummary
import com.edu.core.domain.model.Provider
import com.edu.core.domain.model.Transaction
import com.edu.core.domain.model.TransactionCategory
import com.edu.core.domain.model.TransactionType
import com.edu.core.domain.storage.UserPreferencesStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

val NOW: Instant = Instant.parse("2026-09-16T09:30:00Z")
val TODAY: LocalDate = LocalDate.of(2026, 9, 16)
val CLOCK: Clock = Clock.fixed(NOW, ZoneOffset.UTC)

fun account(id: String, type: AccountType = AccountType.TRANSACTION, provider: String = "monzo") = Account(
    accountId = id,
    accountType = type,
    displayName = id,
    provider = Provider(provider, provider.replaceFirstChar { it.uppercase() }),
    accountNumber = AccountNumber(),
)

fun balance(id: String, available: String, current: String = available) =
    Balance(id, BigDecimal(available), BigDecimal(current), updateTimestamp = NOW)

fun withBalance(id: String, available: String, type: AccountType = AccountType.TRANSACTION, provider: String = "monzo", current: String = available) =
    AccountWithBalance(account(id, type, provider), balance(id, available, current))

fun tx(id: String, date: String, amount: String, runningBalance: String? = null, accountId: String = "a", pending: Boolean = false) = Transaction(
    transactionId = id,
    accountId = accountId,
    timestamp = Instant.parse("${date}T12:00:00Z"),
    description = id,
    amount = BigDecimal(amount),
    transactionType = if (BigDecimal(amount).signum() < 0) TransactionType.DEBIT else TransactionType.CREDIT,
    transactionCategory = TransactionCategory.PURCHASE,
    runningBalance = runningBalance?.let(::BigDecimal),
    isPending = pending,
)

fun cardSummary(id: String, current: String, type: String = "CREDIT") = CardSummary(
    card = Card(id, "MASTERCARD", type, id, "4471", "J", Provider("p", "Bank")),
    balance = CardBalance(id, BigDecimal("5000") - BigDecimal(current), BigDecimal(current), BigDecimal("5000")),
    utilisation = null,
)

class FakeUserPreferencesStorage(consentEpochDay: Long? = null) : UserPreferencesStorage {
    private val consent = MutableStateFlow(consentEpochDay)
    override suspend fun setConsentDateEpochDay(epochDay: Long) { consent.value = epochDay }
    override fun observeConsentDateEpochDay(): Flow<Long?> = consent
    override suspend fun setLastLoginEpochMillis(epochMillis: Long) = Unit
    override fun observeLastLoginEpochMillis(): Flow<Long?> = MutableStateFlow(null)
    override suspend fun setBiometricEnabled(enabled: Boolean) = Unit
    override fun observeBiometricEnabled(): Flow<Boolean> = MutableStateFlow(false)
    override suspend fun setNotificationsEnabled(enabled: Boolean) = Unit
    override fun observeNotificationsEnabled(): Flow<Boolean> = MutableStateFlow(true)
    override suspend fun setOnboardingCompleted(completed: Boolean) = Unit
    override fun observeOnboardingCompleted(): Flow<Boolean> = MutableStateFlow(true)
}
