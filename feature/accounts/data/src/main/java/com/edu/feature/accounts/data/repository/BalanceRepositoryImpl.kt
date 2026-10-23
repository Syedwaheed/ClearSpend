package com.edu.feature.accounts.data.repository

import com.edu.core.data.cache.networkFirst
import com.edu.core.database.dao.BalanceDao
import com.edu.core.domain.model.Balance
import com.edu.core.domain.repository.BalanceRepository
import com.edu.core.domain.storage.SyncStatusStorage
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.feature.accounts.data.mapper.BalanceMapper
import com.edu.feature.accounts.data.remote.AccountsApi
import java.time.Clock

/** `GET /data/v1/accounts/{id}/balance`, cached in Room for offline use. */
class BalanceRepositoryImpl(
    private val api: AccountsApi,
    private val balanceDao: BalanceDao,
    private val syncStatus: SyncStatusStorage,
    private val clock: Clock,
    private val mapper: BalanceMapper = BalanceMapper(),
) : BalanceRepository {

    override suspend fun getBalance(accountId: String): Result<Balance, DataError> = networkFirst(
        syncStatus = syncStatus,
        fetch = {
            when (val result = api.getBalance(accountId)) {
                is Result.Success -> result.data.results.firstOrNull()
                    ?.let { Result.Success(mapper.toDomain(accountId, it, clock.instant())) }
                    ?: Result.Error(DataError.NetworkError.NOT_FOUND)
                is Result.Error -> result
            }
        },
        saveToCache = { balance -> balanceDao.upsert(mapper.toEntity(balance)) },
        readCache = { balanceDao.getBalance(accountId)?.let(mapper::fromEntity) },
    )
}
