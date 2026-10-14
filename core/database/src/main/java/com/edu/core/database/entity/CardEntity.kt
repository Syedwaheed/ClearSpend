package com.edu.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey val cardId: String,
    val cardNetwork: String,
    val cardType: String,
    val displayName: String,
    val partialCardNumber: String,
    val nameOnCard: String,
    val providerId: String,
    val providerDisplayName: String,
    val providerLogoUri: String?,
)

@Entity(tableName = "card_balances")
data class CardBalanceEntity(
    @PrimaryKey val cardId: String,
    val available: String,
    val current: String,
    val creditLimit: String,
    val lastStatementBalance: String?,
    val paymentDue: String?,
    val paymentDueDateEpochDay: Long?,
    val currency: String,
)
