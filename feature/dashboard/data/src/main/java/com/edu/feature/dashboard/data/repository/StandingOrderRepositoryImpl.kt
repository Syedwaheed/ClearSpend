package com.edu.feature.dashboard.data.repository

import com.edu.core.data.cache.networkFirstList
import com.edu.core.database.dao.StandingOrderDao
import com.edu.core.domain.model.StandingOrder
import com.edu.core.domain.repository.StandingOrderRepository
import com.edu.core.domain.storage.SyncStatusStorage
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.core.domain.util.map
import com.edu.feature.dashboard.data.mapper.StandingOrderMapper
import com.edu.feature.dashboard.data.remote.RecurringPaymentsApi

class StandingOrderRepositoryImpl(
    private val api: RecurringPaymentsApi,
    private val standingOrderDao: StandingOrderDao,
    private val syncStatus: SyncStatusStorage,
    private val mapper: StandingOrderMapper = StandingOrderMapper(),
) : StandingOrderRepository {

    override suspend fun getStandingOrders(accountId: String): Result<List<StandingOrder>, DataError> = networkFirstList(
        syncStatus = syncStatus,
        fetch = { api.getStandingOrders(accountId).map { envelope -> envelope.results.mapNotNull { mapper.toDomain(accountId, it) } } },
        saveToCache = { list -> standingOrderDao.replaceAll(accountId, list.map(mapper::toEntity)) },
        readCache = { standingOrderDao.getStandingOrders(accountId).map(mapper::fromEntity) },
    )
}
