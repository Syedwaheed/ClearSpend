package com.edu.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val accountId: String,
    val accountType: String,
    val displayName: String,
    val currency: String,
    val providerId: String,
    val providerDisplayName: String,
    val providerLogoUri: String?,
    val iban: String?,
    val sortCode: String?,
    val number: String?,
)
