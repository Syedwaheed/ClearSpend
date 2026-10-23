package com.edu.feature.transactions.presentation.fake

import com.edu.core.domain.model.Account
import com.edu.core.domain.model.AccountNumber
import com.edu.core.domain.model.AccountType
import com.edu.core.domain.model.Provider
import com.edu.core.domain.model.Transaction
import com.edu.core.domain.model.TransactionCategory
import com.edu.core.domain.model.TransactionType
import com.edu.core.presentation.ui.formatter.AmountFormatterImpl
import com.edu.core.presentation.ui.formatter.DateFormatterImpl
import com.edu.core.presentation.ui.mapper.SyncTimeUiMapperImpl
import com.edu.core.presentation.ui.mapper.TransactionUiMapperImpl
import com.edu.core.presentation.ui.model.TransactionUi
import com.edu.feature.transactions.presentation.mapper.AccountOptionUiMapper
import com.edu.feature.transactions.presentation.mapper.TransactionDetailUiMapper
import com.edu.feature.transactions.presentation.mapper.TransactionsContentUiMapper
import com.edu.feature.transactions.domain.model.TransactionDetail
import com.edu.feature.transactions.domain.model.TransactionsOverview
import com.edu.feature.transactions.presentation.model.AccountOptionUi
import com.edu.feature.transactions.presentation.model.MerchantInsightUi
import com.edu.feature.transactions.presentation.model.TransactionDayGroupUi
import com.edu.feature.transactions.presentation.model.TransactionDetailUi
import com.edu.feature.transactions.presentation.model.TransactionsContentUi
import com.edu.feature.transactions.presentation.model.TransactionsSummaryUi
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Locale

/** "Now" for every test: Monday 15 June 2026, 09:30 UTC. */
val NOW: Instant = Instant.parse("2026-06-15T09:30:00Z")
val TODAY: LocalDate = LocalDate.of(2026, 6, 15)
val CLOCK: Clock = Clock.fixed(NOW, ZoneOffset.UTC)

val amountFormatter = AmountFormatterImpl(Locale.UK)
val dateFormatter = DateFormatterImpl(Locale.UK, CLOCK)
val transactionUiMapper = TransactionUiMapperImpl(amountFormatter, dateFormatter)
val syncTimeMapper = SyncTimeUiMapperImpl(dateFormatter)

fun tx(
    id: String,
    timestamp: String = "2026-06-10T06:00:00Z",
    amount: String = "-39.99",
    merchant: String? = "Tesco",
    description: String = "TESCO STORES",
    category: TransactionCategory = TransactionCategory.PURCHASE,
    accountId: String = "acc-1",
    pending: Boolean = false,
    runningBalance: String? = "317.84",
    classification: List<String> = listOf("Shopping", "Groceries"),
    providerReference: String? = null,
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
        classification = classification,
        runningBalance = runningBalance?.let(::BigDecimal),
        isPending = pending,
        providerReference = providerReference,
    )
}

fun account(id: String, type: AccountType = AccountType.TRANSACTION, provider: String = "Monzo") = Account(
    accountId = id,
    accountType = type,
    displayName = "Account $id",
    provider = Provider("ob-$provider", provider),
    accountNumber = AccountNumber(sortCode = "01-21-31", number = "00000000"),
)

// --- Fakes: deterministic, no formatting (CLAUDE.md §4 testing) --------------

fun fakeTransactionUi(id: String) = TransactionUi(
    transactionId = id,
    title = id,
    subtitle = "",
    formattedAmount = id,
    amountColorArgb = 0,
    isDebit = true,
    formattedDate = "",
    categoryIconId = "",
    categoryColorArgb = 0,
    formattedRunningBalance = null,
    isPending = false,
)

/** One group per settled row so tests can read back exactly which rows are shown. */
class FakeTransactionsContentUiMapper : TransactionsContentUiMapper {
    override fun map(domain: TransactionsOverview) = TransactionsContentUi(
        summary = TransactionsSummaryUi(
            isAllTime = false,
            periodLabel = domain.period.toString(),
            transactionCount = domain.summary.count,
            formattedMoneyIn = "",
            formattedMoneyOut = "",
            formattedNet = "",
            dailyBars = emptyList(),
        ),
        pending = domain.pending.map { fakeTransactionUi(it.transactionId) },
        formattedPendingTotal = "",
        groups = domain.groups.flatMap { it.transactions }.map {
            TransactionDayGroupUi(it.transactionId, "", "", false, listOf(fakeTransactionUi(it.transactionId)))
        },
        offlineSyncTime = domain.offlineSince?.toString(),
    )
}

/** The ids of the settled rows a content shows, newest first. */
val TransactionsContentUi.settledIds: List<String> get() = groups.flatMap { g -> g.transactions.map { it.transactionId } }

class FakeAccountOptionUiMapper : AccountOptionUiMapper {
    override fun map(domain: Account) = AccountOptionUi(domain.accountId, domain.accountId, "", "", 0)
}

fun fakeDetailUi(id: String, showManageInBills: Boolean = false) = TransactionDetailUi(
    transactionId = id,
    title = id,
    initial = "",
    categoryColorArgb = 0,
    formattedAmount = "",
    isCredit = false,
    isPending = false,
    dateTime = "",
    classifications = emptyList(),
    accountLabel = null,
    paymentMethod = "",
    reference = "",
    formattedBalanceAfter = null,
    insight = MerchantInsightUi.FirstTime(id, 90),
    showManageInBills = showManageInBills,
    offlineSyncTime = null,
)

class FakeTransactionDetailUiMapper : TransactionDetailUiMapper {
    override fun map(domain: TransactionDetail) = fakeDetailUi(
        id = domain.transaction.transactionId,
        showManageInBills = domain.transaction.transactionCategory == TransactionCategory.DIRECT_DEBIT,
    )
}
