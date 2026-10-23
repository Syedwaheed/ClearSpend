package com.edu.feature.accounts.data.mapper

import com.edu.core.domain.model.UserInfo
import com.edu.feature.accounts.data.remote.dto.UserInfoDto

/** UserInfoDto → domain [UserInfo]; null when the API returned no name. */
class UserInfoMapper {
    fun toDomain(dto: UserInfoDto): UserInfo? {
        val name = dto.fullName?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        return UserInfo(fullName = name, emails = dto.emails)
    }
}
