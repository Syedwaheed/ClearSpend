package com.edu.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "direct_debits")
data class DirectDebitEntity(
    @PrimaryKey val directDebitId: String,
    val accountId: String,
    val name: String,
    val status: String,
    val previousPaymentAmount: String?,
    val previousPaymentTimestampEpochMillis: Long?,
    val currency: String,
)
