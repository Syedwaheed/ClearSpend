package com.edu.feature.transactions.data.repository

import com.edu.core.data.cache.networkFirstList
import com.edu.core.database.dao.TransactionDao
import com.edu.core.domain.model.Transaction
import com.edu.core.domain.repository.TransactionRepository
import com.edu.core.domain.storage.SyncStatusStorage
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.core.domain.util.map
import com.edu.feature.transactions.data.mapper.TransactionMapper
import com.edu.feature.transactions.data.remote.TransactionsApi
import java.time.Clock
import java.time.LocalDate

/**
 * Settled + pending transactions, cached in Room for offline use. Results
 * are returned newest first.
 */
class TransactionRepositoryImpl(
    private val api: TransactionsApi,
    private val transactionDao: TransactionDao,
    private val syncStatus: SyncStatusStorage,
    private val clock: Clock,
    private val mapper: TransactionMapper = TransactionMapper(),
) : TransactionRepository {

    override suspend fun getTransactions(
        accountId: String,
        from: LocalDate,
        to: LocalDate,
    ): Result<List<Transaction>, DataError> = networkFirstList(
        syncStatus = syncStatus,
        fetch = {
            api.getTransactions(accountId, from, to).map { envelope ->
                envelope.results
                    .mapNotNull { mapper.toDomain(accountId, it, isPending = false) }
                    .sortedByDescending { it.timestamp }
            }
        },
        saveToCache = { transactions -> transactionDao.upsertAll(transactions.map(mapper::toEntity)) },
        readCache = {
            val zone = clock.zone
            transactionDao.getTransactions(
                accountId = accountId,
                fromEpochMillis = from.atStartOfDay(zone).toInstant().toEpochMilli(),
                toEpochMillis = to.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1,
            ).map(mapper::fromEntity)
        },
    )

    override suspend fun getPendingTransactions(accountId: String): Result<List<Transaction>, DataError> =
        networkFirstList(
            syncStatus = syncStatus,
            fetch = {
                api.getPendingTransactions(accountId).map { envelope ->
                    envelope.results
                        .mapNotNull { mapper.toDomain(accountId, it, isPending = true) }
                        .sortedByDescending { it.timestamp }
                }
            },
            saveToCache = { transactions -> transactionDao.replacePending(accountId, transactions.map(mapper::toEntity)) },
            readCache = { transactionDao.getPendingTransactions(accountId).map(mapper::fromEntity) },
        )
}
