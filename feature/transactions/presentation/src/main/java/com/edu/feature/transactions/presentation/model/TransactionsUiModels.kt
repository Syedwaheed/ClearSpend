package com.edu.feature.transactions.presentation.model

import android.os.Parcelable
import com.edu.core.presentation.ui.model.TransactionUi
import kotlinx.parcelize.Parcelize

/** Period chips on Transactions (12). */
enum class PeriodChipUi { ALL, THIS_MONTH, LAST_MONTH, CUSTOM_RANGE }

/** Category chips on Transactions (12), in screen order (DESIGN_SPEC §4 · 12). */
enum class CategoryChipUi { ALL, PURCHASE, DIRECT_DEBIT, BILL_PAYMENT, TRANSFER, CASH, CREDIT }

/**
 * The selected period. [customStartUtcMillis]/[customEndUtcMillis] are the
 * Material 3 DateRangePicker values (UTC midnight), set only for CUSTOM_RANGE.
 */
@Parcelize
data class PeriodSelectionUi(
    val chip: PeriodChipUi = PeriodChipUi.ALL,
    val customStartUtcMillis: Long? = null,
    val customEndUtcMillis: Long? = null,
) : Parcelable

/** One entry of the account switcher dropdown. */
@Parcelize
data class AccountOptionUi(
    val accountId: String,
    val displayName: String,
    /** "Monzo" — shown under the name in the dropdown. */
    val providerName: String,
    /** First letter of the provider, shown in the tinted square. */
    val initial: String,
    /** `account_type` colour (DESIGN_SPEC §6). */
    val colorArgb: Int,
) : Parcelable

/** One daily-spending bar: height as a fraction of the tallest bar. */
@Parcelize
data class DailyBarUi(
    val heightFraction: Float,
    /** The newest day is drawn in the highlight colour. */
    val isLatest: Boolean,
) : Parcelable

/** The navy summary card. */
@Parcelize
data class TransactionsSummaryUi(
    /** True for "All time"; otherwise [periodLabel] holds "June 2026" / "21 May – 1 Jun 2026". */
    val isAllTime: Boolean,
    val periodLabel: String,
    val transactionCount: Int,
    val formattedMoneyIn: String,
    val formattedMoneyOut: String,
    /** Always signed: "+£1,357.40" / "-£12.00". */
    val formattedNet: String,
    val dailyBars: List<DailyBarUi>,
) : Parcelable

/** A date-group header ("TODAY — 1 JUNE 2026", "-£313.82") and its rows. */
@Parcelize
data class TransactionDayGroupUi(
    val key: String,
    val label: String,
    val formattedNet: String,
    val isNetPositive: Boolean,
    val transactions: List<TransactionUi>,
) : Parcelable

/** Everything below the filters on Transactions (12/13). */
@Parcelize
data class TransactionsContentUi(
    val summary: TransactionsSummaryUi,
    val pending: List<TransactionUi>,
    val formattedPendingTotal: String,
    val groups: List<TransactionDayGroupUi>,
    /** "Showing data from {time}" when served from the Room cache. */
    val offlineSyncTime: String?,
) : Parcelable {
    val isEmpty: Boolean get() = pending.isEmpty() && groups.isEmpty()
}

/** Merchant insight card on Transaction Detail (14-16); the Screen fills in the copy. */
sealed interface MerchantInsightUi : Parcelable {
    /** "First time at {merchant}" · "No earlier payments in the last {days} days". */
    @Parcelize
    data class FirstTime(val merchant: String, val lookbackDays: Int) : MerchantInsightUi

    /** "{total} at {merchant} this month" · "{count} payments[ · one of your top merchants]". */
    @Parcelize
    data class MonthlyTotal(
        val merchant: String,
        val formattedTotal: String,
        val count: Int,
        val isTopMerchant: Boolean,
    ) : MerchantInsightUi
}

/** Transaction Detail (14-16). */
@Parcelize
data class TransactionDetailUi(
    val transactionId: String,
    val title: String,
    val initial: String,
    val categoryColorArgb: Int,
    /** Signed: "-£39.99" / "+£420.00". */
    val formattedAmount: String,
    val isCredit: Boolean,
    val isPending: Boolean,
    /** "1 Jun 2026 · 06:00". */
    val dateTime: String,
    /** `transaction_classification[]`, or the category label when the API sends none. */
    val classifications: List<String>,
    /** "Transaction Account 1 · Monzo"; null when the account could not be loaded. */
    val accountLabel: String?,
    val paymentMethod: String,
    val reference: String,
    /** Null for pending rows and when the API omits `running_balance` (shown as "—"). */
    val formattedBalanceAfter: String?,
    val insight: MerchantInsightUi,
    /** "Manage in Bills" only for `transaction_category == DIRECT_DEBIT`. */
    val showManageInBills: Boolean,
    val offlineSyncTime: String?,
) : Parcelable
