package com.edu.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_payees")
data class SavedPayeeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountHolderName: String,
    val sortCode: String,
    val accountNumber: String,
    val nickname: String?,
    val createdAtEpochMillis: Long,
)
