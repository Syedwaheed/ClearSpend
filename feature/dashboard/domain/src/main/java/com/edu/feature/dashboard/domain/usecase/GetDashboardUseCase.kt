package com.edu.feature.dashboard.domain.usecase

import com.edu.core.domain.model.CardSummary
import com.edu.core.domain.model.DirectDebit
import com.edu.core.domain.model.StandingOrder
import com.edu.core.domain.model.Transaction
import com.edu.core.domain.model.UserInfo
import com.edu.core.domain.model.primaryAccount
import com.edu.core.domain.repository.DirectDebitRepository
import com.edu.core.domain.repository.StandingOrderRepository
import com.edu.core.domain.repository.TransactionRepository
import com.edu.core.domain.repository.UserInfoRepository
import com.edu.core.domain.storage.SyncStatusStorage
import com.edu.core.domain.usecase.GetAccountsWithBalancesUseCase
import com.edu.core.domain.usecase.GetCardSummariesUseCase
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.feature.dashboard.domain.model.Dashboard
import com.edu.feature.dashboard.domain.model.PendingSummary
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.math.BigDecimal
import java.time.Clock
import java.time.LocalDate

/**
 * Loads everything Home (09) needs, in parallel as DESIGN_SPEC §4 · 09 says
 * (accounts + balances, primary account transactions / pending / DDs / SOs,
 * cards + card balances, `/info`), then derives every value through the
 * single-purpose use cases. Returns one [Dashboard] so the ViewModel emits a
 * single result through its sequential MVI queue.
 *
 * Failure policy: accounts failing fails the screen; a 401 anywhere fails with
 * UNAUTHORIZED (→ Connect Bank); any other secondary call failing just leaves
 * its section empty.
 */
class GetDashboardUseCase(
    private val getAccountsWithBalances: GetAccountsWithBalancesUseCase,
    private val getCardSummaries: GetCardSummariesUseCase,
    private val transactionRepository: TransactionRepository,
    private val directDebitRepository: DirectDebitRepository,
    private val standingOrderRepository: StandingOrderRepository,
    private val userInfoRepository: UserInfoRepository,
    private val syncStatus: SyncStatusStorage,
    private val getGreeting: GetGreetingUseCase,
    private val getPayday: GetPaydayUseCase,
    private val getUpcomingPayments: GetUpcomingPaymentsUseCase,
    private val getSafeToSpend: GetSafeToSpendUseCase,
    private val getMonthlyCashFlow: GetMonthlyCashFlowUseCase,
    private val getPendingSummary: GetPendingSummaryUseCase,
    private val getSpendingByCategory: GetSpendingByCategoryUseCase,
    private val getRecentTransactions: GetRecentTransactionsUseCase,
    private val clock: Clock,
) {

    suspend operator fun invoke(): Result<Dashboard, DataError> = coroutineScope {
        val today = LocalDate.now(clock)
        val accountsDeferred = async { getAccountsWithBalances() }
        val cardsDeferred = async { getCardSummaries() }
        val userInfoDeferred = async { userInfoRepository.getUserInfo() }

        val accounts = when (val result = accountsDeferred.await()) {
            is Result.Success -> result.data
            is Result.Error -> {
                cardsDeferred.cancel()
                userInfoDeferred.cancel()
                return@coroutineScope result
            }
        }
        val primary = accounts.map { it.account }.primaryAccount()

        val transactionsDeferred = async {
            primary?.let { transactionRepository.getTransactions(it.accountId, today.minusDays(HISTORY_DAYS), today) }
        }
        val pendingDeferred = async { primary?.let { transactionRepository.getPendingTransactions(it.accountId) } }
        val directDebitsDeferred = async { primary?.let { directDebitRepository.getDirectDebits(it.accountId) } }
        val standingOrdersDeferred = async { primary?.let { standingOrderRepository.getStandingOrders(it.accountId) } }

        val transactionsResult = transactionsDeferred.await()
        val pendingResult = pendingDeferred.await()
        val directDebitsResult = directDebitsDeferred.await()
        val standingOrdersResult = standingOrdersDeferred.await()
        val cardsResult = cardsDeferred.await()
        val userInfoResult = userInfoDeferred.await()

        val secondary = listOf(transactionsResult, pendingResult, directDebitsResult, standingOrdersResult, cardsResult, userInfoResult)
        if (secondary.any { it is Result.Error && it.error == DataError.NetworkError.UNAUTHORIZED }) {
            return@coroutineScope Result.Error(DataError.NetworkError.UNAUTHORIZED)
        }

        val transactions: List<Transaction>? = transactionsResult.dataOrNull()
        val pending: List<Transaction> = pendingResult.dataOrNull().orEmpty()
        val directDebits: List<DirectDebit> = directDebitsResult.dataOrNull().orEmpty()
        val standingOrders: List<StandingOrder> = standingOrdersResult.dataOrNull().orEmpty()
        val cards: List<CardSummary> = cardsResult.dataOrNull().orEmpty()
        val userInfo: UserInfo? = userInfoResult.dataOrNull()

        val cashFlow = transactions?.let(getMonthlyCashFlow::invoke)
        val payday = transactions?.let(getPayday::invoke)
        val upcoming = getUpcomingPayments(directDebits, standingOrders)

        Result.Success(
            Dashboard(
                today = today,
                greeting = getGreeting(userInfo?.fullName),
                accounts = accounts,
                primaryAccount = primary,
                primaryBalance = accounts.firstOrNull { it.account.accountId == primary?.accountId }?.balance,
                safeToSpend = getSafeToSpend(accounts, upcoming, payday, cashFlow?.moneyOut ?: BigDecimal.ZERO),
                payday = payday,
                cashFlow = cashFlow,
                pending = if (primary == null) PendingSummary(0, BigDecimal.ZERO) else getPendingSummary(pending),
                upcomingPayments = upcoming.take(UPCOMING_LIMIT),
                spendingByCategory = transactions?.let(getSpendingByCategory::invoke).orEmpty(),
                card = cards.firstOrNull(),
                recentTransactions = transactions?.let { getRecentTransactions(it) }.orEmpty(),
                offlineSince = syncStatus.getOfflineSince(),
            ),
        )
    }

    private fun <T> Result<T, DataError>?.dataOrNull(): T? = (this as? Result.Success)?.data

    companion object {
        /** Covers the whole previous month (money out vs last month) and ≥ 2 monthly salaries. */
        const val HISTORY_DAYS = 90L
        const val UPCOMING_LIMIT = 3
    }
}
