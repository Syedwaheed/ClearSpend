package com.edu.core.presentation.ui.mapper

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.edu.core.domain.model.Account
import com.edu.core.domain.model.AccountType
import com.edu.core.presentation.designsystem.AccountTypeBusinessSavings
import com.edu.core.presentation.designsystem.AccountTypeBusinessTransaction
import com.edu.core.presentation.designsystem.AccountTypeSavings
import com.edu.core.presentation.designsystem.AccountTypeTransaction
import com.edu.core.presentation.ui.formatter.IbanFormatter
import com.edu.core.presentation.ui.model.AccountUi

class AccountUiMapperImpl(
    private val ibanFormatter: IbanFormatter,
) : AccountUiMapper {

    override fun map(domain: Account): AccountUi {
        val (badge, color) = badgeAndColorFor(domain.accountType)
        return AccountUi(
            accountId = domain.accountId,
            displayName = domain.displayName,
            currency = domain.currency,
            maskedIban = domain.accountNumber.iban?.let(ibanFormatter::mask),
            maskedAccountNumber = domain.accountNumber.number?.let(ibanFormatter::maskAccountNumber),
            formattedSortCode = domain.accountNumber.sortCode?.let(ibanFormatter::formatSortCode),
            providerDisplayName = domain.provider.displayName,
            providerLogoUri = domain.provider.logoUri,
            accountTypeBadge = badge,
            accountTypeColorArgb = color.toArgb(),
        )
    }

    private fun badgeAndColorFor(accountType: AccountType): Pair<String, Color> = when (accountType) {
        AccountType.TRANSACTION -> "CURRENT" to AccountTypeTransaction
        AccountType.SAVINGS -> "SAVINGS" to AccountTypeSavings
        AccountType.BUSINESS_TRANSACTION -> "BUSINESS" to AccountTypeBusinessTransaction
        AccountType.BUSINESS_SAVINGS -> "BUS. SAVINGS" to AccountTypeBusinessSavings
    }
}
