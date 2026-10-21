package com.edu.feature.transactions.domain.usecase

import com.edu.core.domain.repository.AccountRepository
import com.edu.core.domain.repository.TransactionRepository
import com.edu.core.domain.storage.SyncStatusStorage
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.feature.transactions.domain.model.TransactionDetail
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.time.Clock
import java.time.LocalDate

/**
 * Transaction Detail (14-16), loaded by `transaction_id` only (never a domain
 * object passed through navigation):
 * 1. the transaction from the repository cache (settled or pending);
 * 2. in parallel, its account (`display_name` · `provider.display_name`) and
 *    the account's history covering the merchant-insight look-back.
 *
 * Only a missing transaction or a 401 fails the screen; a failed account or
 * history call degrades to "—" / a first-time insight.
 */
class GetTransactionDetailUseCase(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val getMerchantInsight: GetMerchantInsightUseCase,
    private val syncStatus: SyncStatusStorage,
    private val clock: Clock,
) {

    suspend operator fun invoke(transactionId: String): Result<TransactionDetail, DataError> {
        val transaction = when (val result = transactionRepository.getTransaction(transactionId)) {
            is Result.Success -> result.data
            is Result.Error -> return result
        }
        val today = LocalDate.now(clock)
        val transactionDate = transaction.timestamp.atZone(clock.zone).toLocalDate()
        val historyFrom = minOf(
            transactionDate.minusDays(GetMerchantInsightUseCase.LOOKBACK_DAYS.toLong()),
            transactionDate.withDayOfMonth(1),
        )
        val historyTo = maxOf(minOf(transactionDate.withDayOfMonth(transactionDate.lengthOfMonth()), today), historyFrom)

        val (account, history) = coroutineScope {
            val accountDeferred = async { accountRepository.getAccount(transaction.accountId) }
            val historyDeferred = async { transactionRepository.getTransactions(transaction.accountId, historyFrom, historyTo) }
            accountDeferred.await() to historyDeferred.await()
        }
        if (listOf(account, history).any { it is Result.Error && it.error == DataError.NetworkError.UNAUTHORIZED }) {
            return Result.Error(DataError.NetworkError.UNAUTHORIZED)
        }

        return Result.Success(
            TransactionDetail(
                transaction = transaction,
                account = (account as? Result.Success)?.data,
                insight = getMerchantInsight(transaction, (history as? Result.Success)?.data.orEmpty()),
                offlineSince = syncStatus.getOfflineSince(),
            ),
        )
    }
}
