package com.edu.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val transactionId: String,
    val accountId: String,
    val timestampEpochMillis: Long,
    val description: String,
    val merchantName: String?,
    val amount: String,
    val currency: String,
    val transactionType: String,
    val transactionCategory: String,
    val classification: List<String>,
    val runningBalance: String?,
    val isPending: Boolean,
)
