package com.edu.feature.auth.presentation.connectbank

import android.os.Parcelable
import com.edu.feature.auth.presentation.model.BankUi
import kotlinx.parcelize.Parcelize

@Parcelize
data class ConnectBankUiState(
    val isLoadingBanks: Boolean = false,
    val featuredBanks: List<BankUi> = emptyList(),
    val hasBanksError: Boolean = false,
) : Parcelable
