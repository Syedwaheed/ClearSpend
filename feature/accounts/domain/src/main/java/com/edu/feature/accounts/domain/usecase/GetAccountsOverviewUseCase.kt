package com.edu.feature.accounts.domain.usecase

import com.edu.core.domain.model.Transaction
import com.edu.core.domain.repository.TransactionRepository
import com.edu.core.domain.storage.SyncStatusStorage
import com.edu.core.domain.usecase.GetAccountsWithBalancesUseCase
import com.edu.core.domain.usecase.GetCardSummariesUseCase
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.feature.accounts.domain.model.AccountOverviewItem
import com.edu.feature.accounts.domain.model.AccountsOverview
import com.edu.feature.accounts.domain.model.ProviderGroup
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.math.BigDecimal
import java.time.Clock
import java.time.LocalDate

/**
 * Accounts screen (10): accounts + balances, cards, and 8 weeks of each
 * account's transactions, loaded in parallel; then net position, history,
 * provider groups and consent days are derived. Accounts failing fails the
 * screen, a 401 anywhere returns UNAUTHORIZED, other failures degrade.
 */
class GetAccountsOverviewUseCase(
    private val getAccountsWithBalances: GetAccountsWithBalancesUseCase,
    private val getCardSummaries: GetCardSummariesUseCase,
    private val transactionRepository: TransactionRepository,
    private val getNetPosition: GetNetPositionUseCase,
    private val getBalanceHistory: GetBalanceHistoryUseCase,
    private val getConsentDaysRemaining: GetConsentDaysRemainingUseCase,
    private val syncStatus: SyncStatusStorage,
    private val clock: Clock,
) {

    suspend operator fun invoke(): Result<AccountsOverview, DataError> = coroutineScope {
        val today = LocalDate.now(clock)
        val cardsDeferred = async { getCardSummaries() }
        val accounts = when (val result = getAccountsWithBalances()) {
            is Result.Success -> result.data
            is Result.Error -> {
                cardsDeferred.cancel()
                return@coroutineScope result
            }
        }
        val histories = accounts.map { item ->
            async {
                item.account.accountId to transactionRepository.getTransactions(
                    item.account.accountId,
                    today.minusDays(GetBalanceHistoryUseCase.HISTORY_DAYS),
                    today,
                )
            }
        }.awaitAll()
        val cardsResult = cardsDeferred.await()

        val failures = histories.map { it.second } + cardsResult
        if (failures.any { it is Result.Error && it.error == DataError.NetworkError.UNAUTHORIZED }) {
            return@coroutineScope Result.Error(DataError.NetworkError.UNAUTHORIZED)
        }

        val transactionsByAccount: Map<String, List<Transaction>> = histories
            .mapNotNull { (id, result) -> (result as? Result.Success)?.data?.let { id to it } }
            .toMap()
        val cards = (cardsResult as? Result.Success)?.data.orEmpty()
        val netPosition = getNetPosition(accounts, cards)
        val history = getBalanceHistory(accounts, transactionsByAccount, netPosition.cardOwed)

        val groups = accounts
            .groupBy { it.account.provider.providerId.ifEmpty { it.account.provider.displayName } }
            .values
            .map { items ->
                ProviderGroup(
                    provider = items.first().account.provider,
                    accounts = items.map {
                        AccountOverviewItem(it.account, it.balance, history.thirtyDayChangeByAccount[it.account.accountId])
                    },
                    subtotal = items.fold(BigDecimal.ZERO) { sum, item -> sum + (item.balance?.available ?: BigDecimal.ZERO) },
                )
            }

        Result.Success(
            AccountsOverview(
                groups = groups,
                cards = cards,
                netPosition = netPosition,
                history = history,
                consentDaysRemaining = getConsentDaysRemaining(),
                lastSyncedAt = syncStatus.getLastSyncedAt(),
                offlineSince = syncStatus.getOfflineSince(),
            ),
        )
    }
}
