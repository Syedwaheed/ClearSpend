package com.edu.feature.dashboard.presentation.mapper

import com.edu.core.presentation.ui.mapper.AccountUiMapper
import com.edu.core.presentation.ui.mapper.BalanceUiMapper
import com.edu.core.presentation.ui.mapper.CardSummaryUiMapper
import com.edu.core.presentation.ui.mapper.SyncTimeUiMapper
import com.edu.core.presentation.ui.mapper.TransactionUiMapper
import com.edu.core.presentation.ui.mapper.mapList
import com.edu.feature.dashboard.domain.model.Dashboard
import com.edu.feature.dashboard.presentation.model.DashboardAccountUi
import com.edu.feature.dashboard.presentation.model.DashboardContentUi

class DashboardContentUiMapperImpl(
    private val headerMapper: DashboardHeaderUiMapper,
    private val accountMapper: AccountUiMapper,
    private val balanceMapper: BalanceUiMapper,
    private val safeToSpendMapper: SafeToSpendUiMapper,
    private val cashFlowMapper: CashFlowUiMapper,
    private val pendingMapper: PendingUiMapper,
    private val upcomingPaymentMapper: UpcomingPaymentUiMapper,
    private val categorySpendMapper: CategorySpendUiMapper,
    private val cardSummaryMapper: CardSummaryUiMapper,
    private val transactionMapper: TransactionUiMapper,
    private val syncTimeMapper: SyncTimeUiMapper,
) : DashboardContentUiMapper {

    override fun map(domain: Dashboard): DashboardContentUi = DashboardContentUi(
        header = headerMapper.map(domain),
        balance = domain.primaryBalance?.let(balanceMapper::map),
        accounts = domain.accounts.map { DashboardAccountUi(accountMapper.map(it.account), it.balance?.let(balanceMapper::map)) },
        safeToSpend = domain.safeToSpend?.let(safeToSpendMapper::map)?.copy(daysToPayday = domain.payday?.daysUntil),
        cashFlow = domain.cashFlow?.let(cashFlowMapper::map),
        pending = domain.pending.takeIf { it.count > 0 }?.let(pendingMapper::map),
        upcomingPayments = upcomingPaymentMapper.mapList(domain.upcomingPayments),
        categories = categorySpendMapper.mapList(domain.spendingByCategory),
        card = domain.card?.let(cardSummaryMapper::map),
        recentTransactions = transactionMapper.mapList(domain.recentTransactions),
        offlineSyncTime = domain.offlineSince?.let(syncTimeMapper::map),
    )
}
