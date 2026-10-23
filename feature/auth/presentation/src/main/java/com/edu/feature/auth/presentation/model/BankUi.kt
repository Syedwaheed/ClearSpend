package com.edu.feature.auth.presentation.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** A bank tile/chip on screens 05 and 06. [brandColorArgb] is the badge colour. */
@Parcelize
data class BankUi(
    val providerId: String,
    val name: String,
    val initial: String,
    val brandColorArgb: Long,
    val segmentLabel: String,
) : Parcelable
