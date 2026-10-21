package com.edu.feature.accounts.data.repository

import com.edu.core.data.cache.networkFirst
import com.edu.core.data.cache.networkFirstList
import com.edu.core.database.dao.AccountDao
import com.edu.core.domain.model.Account
import com.edu.core.domain.repository.AccountRepository
import com.edu.core.domain.storage.SyncStatusStorage
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.core.domain.util.map
import com.edu.feature.accounts.data.mapper.AccountMapper
import com.edu.feature.accounts.data.remote.AccountsApi

/** `GET /data/v1/accounts[/{id}]`, cached in Room for offline use. */
class AccountRepositoryImpl(
    private val api: AccountsApi,
    private val accountDao: AccountDao,
    private val syncStatus: SyncStatusStorage,
    private val mapper: AccountMapper = AccountMapper(),
) : AccountRepository {

    override suspend fun getAccounts(): Result<List<Account>, DataError> = networkFirstList(
        syncStatus = syncStatus,
        fetch = { api.getAccounts().map { envelope -> envelope.results.map(mapper::toDomain) } },
        saveToCache = { accounts -> accountDao.replaceAll(accounts.map(mapper::toEntity)) },
        readCache = { accountDao.getAccounts().map(mapper::fromEntity) },
    )

    override suspend fun getAccount(accountId: String): Result<Account, DataError> = networkFirst(
        syncStatus = syncStatus,
        fetch = {
            when (val result = api.getAccount(accountId)) {
                is Result.Success -> result.data.results.firstOrNull()?.let { Result.Success(mapper.toDomain(it)) }
                    ?: Result.Error(DataError.NetworkError.NOT_FOUND)
                is Result.Error -> result
            }
        },
        saveToCache = { account -> accountDao.upsertAll(listOf(mapper.toEntity(account))) },
        readCache = { accountDao.getAccount(accountId)?.let(mapper::fromEntity) },
    )
}
