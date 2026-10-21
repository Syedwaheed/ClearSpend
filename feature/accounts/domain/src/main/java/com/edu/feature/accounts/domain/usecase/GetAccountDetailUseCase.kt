package com.edu.feature.accounts.domain.usecase

import com.edu.core.domain.repository.AccountRepository
import com.edu.core.domain.repository.BalanceRepository
import com.edu.core.domain.repository.TransactionRepository
import com.edu.core.domain.storage.SyncStatusStorage
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.feature.accounts.domain.model.AccountDetail
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.time.Clock
import java.time.LocalDate

/**
 * Account Detail (11): `GET /accounts/{id}` + `/balance` + `/transactions?from=<30d>`
 * in parallel, keeping the newest [RECENT_LIMIT] settled transactions.
 */
class GetAccountDetailUseCase(
    private val accountRepository: AccountRepository,
    private val balanceRepository: BalanceRepository,
    private val transactionRepository: TransactionRepository,
    private val syncStatus: SyncStatusStorage,
    private val clock: Clock,
) {

    suspend operator fun invoke(accountId: String): Result<AccountDetail, DataError> = coroutineScope {
        val today = LocalDate.now(clock)
        val accountDeferred = async { accountRepository.getAccount(accountId) }
        val balanceDeferred = async { balanceRepository.getBalance(accountId) }
        val transactionsDeferred = async {
            transactionRepository.getTransactions(accountId, today.minusDays(HISTORY_DAYS), today)
        }
        val account = when (val result = accountDeferred.await()) {
            is Result.Success -> result.data
            is Result.Error -> {
                balanceDeferred.cancel()
                transactionsDeferred.cancel()
                return@coroutineScope result
            }
        }
        val balance = balanceDeferred.await()
        val transactions = transactionsDeferred.await()
        if (listOf(balance, transactions).any { it is Result.Error && it.error == DataError.NetworkError.UNAUTHORIZED }) {
            return@coroutineScope Result.Error(DataError.NetworkError.UNAUTHORIZED)
        }
        Result.Success(
            AccountDetail(
                account = account,
                balance = (balance as? Result.Success)?.data,
                recentTransactions = (transactions as? Result.Success)?.data.orEmpty()
                    .filter { !it.isPending }
                    .sortedByDescending { it.timestamp }
                    .take(RECENT_LIMIT),
                offlineSince = syncStatus.getOfflineSince(),
            ),
        )
    }

    companion object {
        const val HISTORY_DAYS = 30L
        const val RECENT_LIMIT = 3
    }
}
