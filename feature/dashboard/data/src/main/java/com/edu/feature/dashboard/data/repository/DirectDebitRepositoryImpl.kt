package com.edu.feature.dashboard.data.repository

import com.edu.core.data.cache.networkFirstList
import com.edu.core.database.dao.DirectDebitDao
import com.edu.core.domain.model.DirectDebit
import com.edu.core.domain.repository.DirectDebitRepository
import com.edu.core.domain.storage.SyncStatusStorage
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.core.domain.util.map
import com.edu.feature.dashboard.data.mapper.DirectDebitMapper
import com.edu.feature.dashboard.data.remote.RecurringPaymentsApi

class DirectDebitRepositoryImpl(
    private val api: RecurringPaymentsApi,
    private val directDebitDao: DirectDebitDao,
    private val syncStatus: SyncStatusStorage,
    private val mapper: DirectDebitMapper = DirectDebitMapper(),
) : DirectDebitRepository {

    override suspend fun getDirectDebits(accountId: String): Result<List<DirectDebit>, DataError> = networkFirstList(
        syncStatus = syncStatus,
        fetch = { api.getDirectDebits(accountId).map { envelope -> envelope.results.mapNotNull { mapper.toDomain(accountId, it) } } },
        saveToCache = { list -> directDebitDao.replaceAll(accountId, list.map(mapper::toEntity)) },
        readCache = { directDebitDao.getDirectDebits(accountId).map(mapper::fromEntity) },
    )
}
