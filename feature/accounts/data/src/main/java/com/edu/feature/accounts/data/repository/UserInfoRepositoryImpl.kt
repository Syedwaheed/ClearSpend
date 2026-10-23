package com.edu.feature.accounts.data.repository

import com.edu.core.data.cache.networkFirst
import com.edu.core.domain.model.UserInfo
import com.edu.core.domain.repository.UserInfoRepository
import com.edu.core.domain.storage.SecureDataStorage
import com.edu.core.domain.storage.SyncStatusStorage
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.feature.accounts.data.mapper.UserInfoMapper
import com.edu.feature.accounts.data.remote.InfoApi

/**
 * `GET /data/v1/info`. The holder's name is personal data, so the offline
 * copy lives in Tink-encrypted [SecureDataStorage], not in Room.
 */
class UserInfoRepositoryImpl(
    private val api: InfoApi,
    private val secureStorage: SecureDataStorage,
    private val syncStatus: SyncStatusStorage,
    private val mapper: UserInfoMapper = UserInfoMapper(),
) : UserInfoRepository {

    override suspend fun getUserInfo(): Result<UserInfo, DataError> = networkFirst(
        syncStatus = syncStatus,
        fetch = {
            when (val result = api.getInfo()) {
                is Result.Success -> result.data.results.firstNotNullOfOrNull(mapper::toDomain)
                    ?.let { Result.Success(it) }
                    ?: Result.Error(DataError.NetworkError.NOT_FOUND)
                is Result.Error -> result
            }
        },
        saveToCache = { info -> secureStorage.putString(KEY_FULL_NAME, info.fullName) },
        readCache = { secureStorage.getString(KEY_FULL_NAME)?.let { UserInfo(fullName = it) } },
    )

    private companion object {
        const val KEY_FULL_NAME = "user_info_full_name"
    }
}
