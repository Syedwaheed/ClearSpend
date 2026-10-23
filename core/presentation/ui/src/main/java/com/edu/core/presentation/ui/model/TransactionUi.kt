package com.edu.core.presentation.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TransactionUi(
    val transactionId: String,
    val title: String,
    /**
     * Full row subtitle for the Transactions list: "Groceries · 14:32 · Card";
     * pending rows drop the time ("Coffee · Card").
     */
    val subtitle: String,
    val formattedAmount: String,
    val amountColorArgb: Int,
    val isDebit: Boolean,
    val formattedDate: String,
    val categoryIconId: String,
    val categoryColorArgb: Int,
    val formattedRunningBalance: String?,
    val isPending: Boolean,
    /** First letter of [title], shown in the row's tinted icon square. */
    val initial: String = "",
    /** Compact subtitle used on Home: "Shopping · Today". */
    val compactSubtitle: String = "",
    /** "Today" / "Yesterday" / "28 May" (Account Detail rows). */
    val relativeDay: String = "",
    /** Long payment-method label (DESIGN_SPEC §6), e.g. "Debit card", "Direct Debit". */
    val paymentMethod: String = "",
    /** `transaction_category` label, e.g. "Shopping", "Direct Debit", "Income". */
    val categoryLabel: String = "",
) : Parcelable
