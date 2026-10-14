package com.edu.core.domain.model

data class UserInfo(
    val fullName: String,
    val emails: List<String> = emptyList(),
)
