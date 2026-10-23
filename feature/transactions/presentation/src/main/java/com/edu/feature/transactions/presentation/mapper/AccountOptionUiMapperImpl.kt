package com.edu.feature.transactions.presentation.mapper

import androidx.compose.ui.graphics.toArgb
import com.edu.core.domain.model.Account
import com.edu.core.domain.model.AccountType
import com.edu.core.presentation.designsystem.AccountTypeBusinessSavings
import com.edu.core.presentation.designsystem.AccountTypeBusinessTransaction
import com.edu.core.presentation.designsystem.AccountTypeSavings
import com.edu.core.presentation.designsystem.AccountTypeTransaction
import com.edu.feature.transactions.presentation.model.AccountOptionUi

class AccountOptionUiMapperImpl : AccountOptionUiMapper {

    override fun map(domain: Account): AccountOptionUi = AccountOptionUi(
        accountId = domain.accountId,
        displayName = domain.displayName,
        providerName = domain.provider.displayName,
        initial = (domain.provider.displayName.firstOrNull { it.isLetterOrDigit() }
            ?: domain.displayName.firstOrNull { it.isLetterOrDigit() })?.uppercase() ?: "•",
        colorArgb = when (domain.accountType) {
            AccountType.TRANSACTION -> AccountTypeTransaction
            AccountType.SAVINGS -> AccountTypeSavings
            AccountType.BUSINESS_TRANSACTION -> AccountTypeBusinessTransaction
            AccountType.BUSINESS_SAVINGS -> AccountTypeBusinessSavings
        }.toArgb(),
    )
}
