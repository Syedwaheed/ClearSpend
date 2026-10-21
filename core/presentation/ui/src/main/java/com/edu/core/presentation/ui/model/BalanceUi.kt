package com.edu.core.presentation.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BalanceUi(
    val accountId: String,
    val formattedAvailable: String,
    val formattedCurrent: String,
    /** Null when the account has no overdraft facility. */
    val formattedOverdraft: String?,
    val hasOverdraft: Boolean,
    val formattedUpdateDate: String,
    /** Overdraft facility, "£0.00" when there is none (stat cells on screens 10/11). */
    val formattedOverdraftLimit: String = "",
    /** "just now" / "5 min ago" from `update_timestamp` (Home hero card). */
    val formattedUpdatedAgo: String = "",
) : Parcelable
