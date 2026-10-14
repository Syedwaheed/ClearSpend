package com.edu.core.domain.repository

import com.edu.core.domain.model.Balance
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result

interface BalanceRepository {
    suspend fun getBalance(accountId: String): Result<Balance, DataError>
}
