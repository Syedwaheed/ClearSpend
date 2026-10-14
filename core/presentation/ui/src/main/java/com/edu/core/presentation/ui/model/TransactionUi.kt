package com.edu.core.presentation.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TransactionUi(
    val transactionId: String,
    val title: String,
    val subtitle: String,
    val formattedAmount: String,
    val amountColorArgb: Int,
    val isDebit: Boolean,
    val formattedDate: String,
    val categoryIconId: String,
    val categoryColorArgb: Int,
    val formattedRunningBalance: String?,
    val isPending: Boolean,
) : Parcelable
