package com.edu.feature.accounts.presentation.model

import android.os.Parcelable
import com.edu.core.presentation.ui.model.AccountUi
import com.edu.core.presentation.ui.model.CardSummaryUi
import com.edu.core.presentation.ui.model.TransactionUi
import kotlinx.parcelize.Parcelize

/** Filter chips on Accounts (10). Each row carries the one it belongs to. */
enum class AccountFilterUi { ALL, CURRENT, SAVINGS, BUSINESS }

/** Middle stat cell: overdraft for current accounts, AER for savings (not in the API → "—"). */
enum class ThirdStatUi { OVERDRAFT, INTEREST_AER }

@Parcelize
data class AccountRowUi(
    val account: AccountUi,
    val filter: AccountFilterUi,
    /** Null when the balance could not be loaded. */
    val formattedAvailable: String?,
    val formattedCurrent: String?,
    val thirdStat: ThirdStatUi,
    val thirdStatValue: String,
    /** "+£200.00" / "-£312.45"; null when history is unavailable. */
    val formattedThirtyDayChange: String?,
    val isThirtyDayChangePositive: Boolean,
) : Parcelable

@Parcelize
data class ProviderGroupUi(
    val id: String,
    val name: String,
    val logoUri: String?,
    val initial: String,
    val fallbackColorArgb: Int,
    val formattedSubtotal: String,
    val accounts: List<AccountRowUi>,
) : Parcelable

@Parcelize
data class NetPositionUi(
    val formattedTotal: String,
    val accountCount: Int,
    /** "+£1,127.85". */
    val formattedChangeThisMonth: String,
    val isChangePositive: Boolean,
    /** 8 bar heights, 0f..1f, oldest first; the last bar is today. */
    val sparkline: List<Float>,
    val formattedCurrent: String,
    val formattedSavings: String,
    val formattedCardOwed: String,
) : Parcelable

@Parcelize
data class ConsentUi(
    val daysRemaining: Int,
    /** "2 min ago"; null when never synced. */
    val syncedAgo: String?,
) : Parcelable

@Parcelize
data class AccountsContentUi(
    val netPosition: NetPositionUi,
    val consent: ConsentUi? = null,
    val groups: List<ProviderGroupUi> = emptyList(),
    val cards: List<CardSummaryUi> = emptyList(),
    val formattedCardsOwed: String = "",
    val offlineSyncTime: String? = null,
) : Parcelable

@Parcelize
data class AccountDetailUi(
    val accountId: String,
    val title: String,
    val formattedAvailable: String?,
    val formattedCurrent: String?,
    val formattedOverdraft: String?,
    /** "01-21-31" — shown in full on this screen only. */
    val sortCode: String?,
    val accountNumber: String?,
    val iban: String?,
    val currency: String,
    val recentTransactions: List<TransactionUi> = emptyList(),
    val offlineSyncTime: String? = null,
) : Parcelable

/** Which detail row last showed "Copied!". */
enum class CopiedFieldUi { SORT_CODE, ACCOUNT_NUMBER }
