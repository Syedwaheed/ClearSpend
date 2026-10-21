package com.edu.feature.auth.presentation.bankselection

import android.os.Parcelable
import com.edu.feature.auth.presentation.model.BankUi
import kotlinx.parcelize.Parcelize

@Parcelize
data class BankSelectionUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val banks: List<BankUi> = emptyList(),
    val hasError: Boolean = false,
    /** A bank was tapped and the auth link is being prepared. */
    val isOpeningBank: Boolean = false,
    /** The Custom Tab (or any browser) could not be opened. */
    val hasLaunchError: Boolean = false,
) : Parcelable {
    val isEmpty: Boolean get() = !isLoading && !hasError && banks.isEmpty()
}
