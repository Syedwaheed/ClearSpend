package com.edu.core.domain.repository

import com.edu.core.domain.model.UserInfo
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result

interface UserInfoRepository {
    suspend fun getUserInfo(): Result<UserInfo, DataError>
}
