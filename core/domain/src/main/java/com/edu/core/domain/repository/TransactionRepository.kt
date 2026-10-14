package com.edu.core.domain.repository

import com.edu.core.domain.model.Transaction
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import java.time.LocalDate

interface TransactionRepository {
    suspend fun getTransactions(
        accountId: String,
        from: LocalDate,
        to: LocalDate,
    ): Result<List<Transaction>, DataError>

    suspend fun getPendingTransactions(accountId: String): Result<List<Transaction>, DataError>
}
