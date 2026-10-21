package com.edu.feature.accounts.domain.model

import com.edu.core.domain.model.Account
import com.edu.core.domain.model.Balance
import com.edu.core.domain.model.CardSummary
import com.edu.core.domain.model.Provider
import com.edu.core.domain.model.Transaction
import java.math.BigDecimal
import java.time.Instant

/** Net position = Σ available (all accounts) − Σ card current (DESIGN_SPEC §4 · 10). */
data class NetPosition(
    val total: BigDecimal,
    /** Σ available of TRANSACTION + BUSINESS_TRANSACTION accounts. */
    val current: BigDecimal,
    /** Σ available of SAVINGS + BUSINESS_SAVINGS accounts. */
    val savings: BigDecimal,
    /** Σ card `current` (what is owed). */
    val cardOwed: BigDecimal,
    val accountCount: Int,
)

/** Reconstructed from transaction `running_balance` (or back-calculated when it is null). */
data class BalanceHistory(
    /** 8 end-of-week net balances, oldest first; the last one is today. */
    val weeklyNetBalances: List<BigDecimal>,
    /** Net change since the end of last month. */
    val changeThisMonth: BigDecimal,
    /** Per account: balance today − balance 30 days ago. Missing when its history is unknown. */
    val thirtyDayChangeByAccount: Map<String, BigDecimal>,
)

data class AccountOverviewItem(
    val account: Account,
    val balance: Balance?,
    val thirtyDayChange: BigDecimal?,
)

data class ProviderGroup(
    val provider: Provider,
    val accounts: List<AccountOverviewItem>,
    /** Σ available of the group's accounts. */
    val subtotal: BigDecimal,
)

/** Everything the Accounts screen (10) shows, already derived. */
data class AccountsOverview(
    val groups: List<ProviderGroup>,
    val cards: List<CardSummary>,
    val netPosition: NetPosition,
    val history: BalanceHistory,
    /** Days until Open Banking consent must be renewed; null when no consent date is stored. */
    val consentDaysRemaining: Int?,
    val lastSyncedAt: Instant?,
    val offlineSince: Instant?,
)

/** Account Detail (11). */
data class AccountDetail(
    val account: Account,
    val balance: Balance?,
    val recentTransactions: List<Transaction>,
    val offlineSince: Instant?,
)

/** Card (21): the primary card, or null when the user has no card. */
data class CardDetail(
    val summary: CardSummary?,
    val offlineSince: Instant?,
)
