package com.edu.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "balances")
data class BalanceEntity(
    @PrimaryKey val accountId: String,
    val available: String,
    val current: String,
    val overdraft: String,
    val currency: String,
    val updateTimestampEpochMillis: Long,
)
