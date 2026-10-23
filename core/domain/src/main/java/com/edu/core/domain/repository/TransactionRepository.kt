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

    /**
     * One transaction (settled or pending) by `transaction_id`, from the local
     * cache that every list load fills. The Data API has no single-transaction
     * endpoint. [DataError.NetworkError.NOT_FOUND] when it is not cached.
     */
    suspend fun getTransaction(transactionId: String): Result<Transaction, DataError>
}
