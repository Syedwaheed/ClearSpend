package com.edu.feature.dashboard.presentation.model

import android.os.Parcelable
import com.edu.core.presentation.ui.model.AccountUi
import com.edu.core.presentation.ui.model.BalanceUi
import com.edu.core.presentation.ui.model.CardSummaryUi
import com.edu.core.presentation.ui.model.TransactionUi
import kotlinx.parcelize.Parcelize

enum class GreetingPeriodUi { MORNING, AFTERNOON, EVENING }

@Parcelize
data class DashboardHeaderUi(
    val period: GreetingPeriodUi,
    val firstName: String?,
    /** Avatar letter; "•" when the name is unknown. */
    val avatarInitial: String,
    /** "Wednesday, 16 September 2026". */
    val formattedDate: String,
) : Parcelable

@Parcelize
data class DashboardAccountUi(
    val account: AccountUi,
    val balance: BalanceUi?,
) : Parcelable

@Parcelize
data class SafeToSpendUi(
    val formattedAmount: String,
    val formattedBillsDue: String,
    val formattedSpent: String,
    val billsFraction: Float,
    val spentFraction: Float,
    val yoursFraction: Float,
    /** Null when no salary was found (the "N days left" chip is hidden). */
    val daysToPayday: Int? = null,
) : Parcelable

@Parcelize
data class CashFlowUi(
    val formattedMoneyIn: String,
    val formattedMoneyOut: String,
    /** "Sep" for "Sep so far". */
    val monthLabel: String,
    /** "Aug" for "↓ 16% vs Aug". */
    val previousMonthLabel: String,
    /** Absolute % change of money out; null when there is nothing to compare. */
    val changePercent: Int?,
    /** True when spending went up (shown in red). */
    val isSpendingUp: Boolean,
) : Parcelable

@Parcelize
data class PendingUi(
    val count: Int,
    val formattedTotal: String,
) : Parcelable

enum class UpcomingPaymentTypeUi { DIRECT_DEBIT, STANDING_ORDER }

@Parcelize
data class UpcomingPaymentUi(
    val id: String,
    val name: String,
    val initial: String,
    val type: UpcomingPaymentTypeUi,
    val formattedAmount: String,
    /** 0 = today, 1 = tomorrow, n = "in n days". */
    val daysUntil: Int,
    val colorArgb: Int,
) : Parcelable

@Parcelize
data class CategorySpendUi(
    val label: String,
    val formattedAmount: String,
    val fraction: Float,
    val colorArgb: Int,
) : Parcelable

/** Everything Home (09) renders, display-ready. */
@Parcelize
data class DashboardContentUi(
    val header: DashboardHeaderUi,
    val balance: BalanceUi? = null,
    val accounts: List<DashboardAccountUi> = emptyList(),
    val safeToSpend: SafeToSpendUi? = null,
    val cashFlow: CashFlowUi? = null,
    val pending: PendingUi? = null,
    val upcomingPayments: List<UpcomingPaymentUi> = emptyList(),
    val categories: List<CategorySpendUi> = emptyList(),
    val card: CardSummaryUi? = null,
    val recentTransactions: List<TransactionUi> = emptyList(),
    /** "14:32" when showing cached data offline; null when fresh. */
    val offlineSyncTime: String? = null,
) : Parcelable
