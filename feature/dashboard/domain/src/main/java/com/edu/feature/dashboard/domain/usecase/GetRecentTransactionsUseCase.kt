package com.edu.feature.dashboard.domain.usecase

import com.edu.core.domain.model.Transaction

/**
 * Recent Transactions on Home (CLAUDE.md §4): the primary account's newest
 * [limit] settled transactions. It works on the list [GetDashboardUseCase]
 * already fetched for the primary account, so Home makes one transactions
 * call instead of two.
 */
class GetRecentTransactionsUseCase {
    operator fun invoke(primaryAccountTransactions: List<Transaction>, limit: Int = DEFAULT_LIMIT): List<Transaction> =
        primaryAccountTransactions
            .filter { !it.isPending }
            .sortedByDescending { it.timestamp }
            .take(limit)

    companion object {
        const val DEFAULT_LIMIT = 5
    }
}
