package com.edu.core.domain.usecase

import com.edu.core.domain.model.AccountWithBalance
import com.edu.core.domain.repository.AccountRepository
import com.edu.core.domain.repository.BalanceRepository
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * `GET /accounts`, then every account's `/balance` in parallel (DESIGN_SPEC
 * §4 · 09/10). A failed balance leaves that account's balance null; a 401 on
 * any call fails the whole load so the app can send the user to Connect Bank.
 * Shared by Home and Accounts, so it lives in core:domain.
 */
class GetAccountsWithBalancesUseCase(
    private val accountRepository: AccountRepository,
    private val balanceRepository: BalanceRepository,
) {

    suspend operator fun invoke(): Result<List<AccountWithBalance>, DataError> {
        val accounts = when (val result = accountRepository.getAccounts()) {
            is Result.Success -> result.data
            is Result.Error -> return result
        }
        val balances = coroutineScope {
            accounts.map { account -> async { balanceRepository.getBalance(account.accountId) } }.awaitAll()
        }
        if (balances.any { it is Result.Error && it.error == DataError.NetworkError.UNAUTHORIZED }) {
            return Result.Error(DataError.NetworkError.UNAUTHORIZED)
        }
        return Result.Success(
            accounts.zip(balances) { account, balance ->
                AccountWithBalance(account, (balance as? Result.Success)?.data)
            },
        )
    }
}
