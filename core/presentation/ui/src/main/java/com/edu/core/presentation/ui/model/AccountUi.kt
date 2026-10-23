package com.edu.core.presentation.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AccountUi(
    val accountId: String,
    val displayName: String,
    val currency: String,
    val maskedIban: String?,
    val maskedAccountNumber: String?,
    val formattedSortCode: String?,
    val providerDisplayName: String,
    val providerLogoUri: String?,
    val accountTypeBadge: String,
    val accountTypeColorArgb: Int,
    /** "house" / "star" / "briefcase" (DESIGN_SPEC §6 account_type → icon). */
    val accountTypeIconId: String = "",
) : Parcelable
