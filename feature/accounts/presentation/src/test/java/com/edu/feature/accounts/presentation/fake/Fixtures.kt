package com.edu.feature.accounts.presentation.fake

import com.edu.core.domain.model.Account
import com.edu.core.domain.model.AccountNumber
import com.edu.core.domain.model.AccountType
import com.edu.core.domain.model.Balance
import com.edu.core.domain.model.Card
import com.edu.core.domain.model.CardSummary
import com.edu.core.domain.model.Provider
import com.edu.core.presentation.ui.formatter.AmountFormatterImpl
import com.edu.core.presentation.ui.formatter.DateFormatterImpl
import com.edu.core.presentation.ui.formatter.IbanFormatterImpl
import com.edu.core.presentation.ui.mapper.AccountUiMapperImpl
import com.edu.core.presentation.ui.mapper.CardSummaryUiMapper
import com.edu.core.presentation.ui.mapper.SyncTimeUiMapper
import com.edu.core.presentation.ui.model.CardSummaryUi
import com.edu.feature.accounts.domain.model.AccountDetail
import com.edu.feature.accounts.domain.model.AccountOverviewItem
import com.edu.feature.accounts.domain.model.AccountsOverview
import com.edu.feature.accounts.domain.model.BalanceHistory
import com.edu.feature.accounts.domain.model.NetPosition
import com.edu.feature.accounts.domain.model.ProviderGroup
import com.edu.feature.accounts.presentation.mapper.AccountDetailUiMapper
import com.edu.feature.accounts.presentation.mapper.AccountsContentUiMapper
import com.edu.feature.accounts.presentation.model.AccountDetailUi
import com.edu.feature.accounts.presentation.model.AccountsContentUi
import com.edu.feature.accounts.presentation.model.NetPositionUi
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.Locale

val NOW: Instant = Instant.parse("2026-09-16T09:30:00Z")
val CLOCK: Clock = Clock.fixed(NOW, ZoneOffset.UTC)
val amountFormatter = AmountFormatterImpl(Locale.UK)
val dateFormatter = DateFormatterImpl(Locale.UK, CLOCK)
val accountUiMapper = AccountUiMapperImpl(IbanFormatterImpl())

fun account(id: String, type: AccountType = AccountType.TRANSACTION) = Account(
    accountId = id,
    accountType = type,
    displayName = id,
    provider = Provider("ob-monzo", "Monzo"),
    accountNumber = AccountNumber(iban = "GB29 MONZ 0121 3100 0000 00", sortCode = "012131", number = "00000000"),
)

fun balance(id: String, available: String = "44.01", overdraft: String = "100") =
    Balance(id, BigDecimal(available), BigDecimal("2.00"), BigDecimal(overdraft), updateTimestamp = NOW)

fun overview(groups: List<ProviderGroup> = emptyList(), consent: Int? = 68) = AccountsOverview(
    groups = groups,
    cards = emptyList(),
    netPosition = NetPosition(BigDecimal("3894.76"), BigDecimal("3324.76"), BigDecimal("2150"), BigDecimal("1580"), 3),
    history = BalanceHistory(
        weeklyNetBalances = listOf("2980", "3140", "3020", "3310", "3480", "3390", "3720", "3894.76").map(::BigDecimal),
        changeThisMonth = BigDecimal("1127.85"),
        thirtyDayChangeByAccount = emptyMap(),
    ),
    consentDaysRemaining = consent,
    lastSyncedAt = NOW.minusSeconds(120),
    offlineSince = null,
)

fun group(vararg items: AccountOverviewItem) = ProviderGroup(Provider("ob-monzo", "Monzo"), items.toList(), BigDecimal("2194.01"))

fun item(id: String, type: AccountType = AccountType.TRANSACTION, change: String? = "-312.45") =
    AccountOverviewItem(account(id, type), balance(id), change?.let(::BigDecimal))

fun fakeNetPositionUi() = NetPositionUi("£1.00", 1, "+£0.00", true, emptyList(), "", "", "")

class FakeAccountsContentUiMapper : AccountsContentUiMapper {
    override fun map(domain: AccountsOverview) = AccountsContentUi(netPosition = fakeNetPositionUi().copy(accountCount = domain.groups.size))
}

fun fakeDetailUi(id: String) = AccountDetailUi(id, id, null, null, null, "01-21-31", "00000000", null, "GBP")

class FakeAccountDetailUiMapper : AccountDetailUiMapper {
    override fun map(domain: AccountDetail) = fakeDetailUi(domain.account.accountId)
}

fun fakeCardUi(id: String) = CardSummaryUi(id, id, "", "", "", "")

class FakeCardSummaryUiMapper : CardSummaryUiMapper {
    override fun map(domain: CardSummary) = fakeCardUi(domain.card.cardId)
}

class FakeSyncTimeUiMapper : SyncTimeUiMapper {
    override fun map(domain: Instant) = "synced"
}

fun card(id: String) = Card(id, "VISA", "CREDIT", id, "4471", "J", Provider("p", "Bank"))
