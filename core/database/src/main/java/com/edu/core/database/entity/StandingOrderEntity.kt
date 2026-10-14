package com.edu.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "standing_orders")
data class StandingOrderEntity(
    @PrimaryKey val standingOrderId: String,
    val accountId: String,
    val payee: String,
    val reference: String?,
    val frequency: String,
    val nextPaymentDateEpochDay: Long?,
    val nextPaymentAmount: String,
    val currency: String,
)
