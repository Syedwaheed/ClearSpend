package com.edu.feature.accounts.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** One entry of `GET /data/v1/info` → `results[]`. */
@Serializable
data class UserInfoDto(
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("emails") val emails: List<String> = emptyList(),
)
