package com.edu.feature.transactions.domain.usecase

import com.edu.core.domain.model.Transaction
import com.edu.core.domain.repository.AccountRepository
import com.edu.core.domain.repository.TransactionRepository
import com.edu.core.domain.storage.SyncStatusStorage
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.feature.transactions.domain.model.DateWindow
import com.edu.feature.transactions.domain.model.TransactionsPage
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * One page of the Transactions list (DESIGN_SPEC §4 · 12):
 * `GET /accounts`, then `GET /accounts/{id}/transactions?from&to` (+ `/pending`
 * on the first page) for the selected account, or for every account when
 * [accountId] is null ("All accounts"), merged and sorted by `timestamp`.
 *
 * - A 401 anywhere fails the page with UNAUTHORIZED (→ Connect Bank).
 * - If every settled call fails, the first error is returned; otherwise the
 *   accounts that did load are shown.
 * - A failed `/pending` call only hides the pending section.
 */
class GetTransactionsPageUseCase(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val syncStatus: SyncStatusStorage,
) {

    suspend operator fun invoke(
        accountId: String?,
        window: DateWindow,
        includePending: Boolean,
    ): Result<TransactionsPage, DataError> {
        val accounts = when (val result = accountRepository.getAccounts()) {
            is Result.Success -> result.data
            is Result.Error -> return result
        }
        val accountIds = accountId?.let(::listOf) ?: accounts.map { it.accountId }

        val (settledResults, pendingResults) = coroutineScope {
            val settled = accountIds.map { id ->
                async { transactionRepository.getTransactions(id, window.from, window.to) }
            }
            val pending = if (includePending) {
                accountIds.map { id -> async { transactionRepository.getPendingTransactions(id) } }
            } else {
                emptyList()
            }
            settled.awaitAll() to pending.awaitAll()
        }

        val all = settledResults + pendingResults
        if (all.any { it is Result.Error && it.error == DataError.NetworkError.UNAUTHORIZED }) {
            return Result.Error(DataError.NetworkError.UNAUTHORIZED)
        }
        val firstSettledError = settledResults.filterIsInstance<Result.Error<DataError>>().firstOrNull()
        if (accountIds.isNotEmpty() && firstSettledError != null && settledResults.all { it is Result.Error }) {
            return firstSettledError
        }

        return Result.Success(
            TransactionsPage(
                accounts = accounts,
                settled = settledResults.merged().filter { !it.isPending },
                pending = pendingResults.merged(),
                offlineSince = syncStatus.getOfflineSince(),
            ),
        )
    }

    private fun List<Result<List<Transaction>, DataError>>.merged(): List<Transaction> =
        filterIsInstance<Result.Success<List<Transaction>>>()
            .flatMap { it.data }
            .distinctBy { it.transactionId }
            .sortedByDescending { it.timestamp }
}
