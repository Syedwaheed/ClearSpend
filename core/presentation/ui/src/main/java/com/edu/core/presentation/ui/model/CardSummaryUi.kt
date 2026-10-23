package com.edu.core.presentation.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** Display-ready card + card balance. Shared by Home (09), Accounts (10) and Card (21). */
@Parcelize
data class CardSummaryUi(
    val cardId: String,
    val displayName: String,
    /** "•••• 4471". */
    val maskedNumber: String,
    /** "••••  ••••  ••••  4471" for the card visual. */
    val cardFaceNumber: String,
    /** "MASTERCARD". */
    val network: String,
    val nameOnCard: String,
    val formattedAvailable: String? = null,
    val formattedCurrent: String? = null,
    val formattedCreditLimit: String? = null,
    val formattedLastStatementBalance: String? = null,
    val formattedPaymentDue: String? = null,
    /** "28 Sep 2026". */
    val formattedPaymentDueDate: String? = null,
    /** "32%". Null when the card has no credit limit. */
    val utilisationLabel: String? = null,
    /** 0f..1f for the utilisation bar. */
    val utilisationFraction: Float = 0f,
    val utilisationColorArgb: Int = 0,
) : Parcelable {
    val hasPaymentDue: Boolean get() = formattedPaymentDue != null && formattedPaymentDueDate != null
}
