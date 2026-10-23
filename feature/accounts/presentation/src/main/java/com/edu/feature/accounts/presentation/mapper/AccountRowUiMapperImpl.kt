package com.edu.feature.accounts.presentation.mapper

import com.edu.core.domain.model.AccountType
import com.edu.core.domain.model.isSavings
import com.edu.core.presentation.ui.formatter.AmountFormatter
import com.edu.core.presentation.ui.mapper.AccountUiMapper
import com.edu.feature.accounts.domain.model.AccountOverviewItem
import com.edu.feature.accounts.presentation.model.AccountFilterUi
import com.edu.feature.accounts.presentation.model.AccountRowUi
import com.edu.feature.accounts.presentation.model.ThirdStatUi
import java.math.BigDecimal

class AccountRowUiMapperImpl(
    private val accountMapper: AccountUiMapper,
    private val amountFormatter: AmountFormatter,
) : AccountRowUiMapper {

    override fun map(domain: AccountOverviewItem): AccountRowUi {
        val type = domain.account.accountType
        val isSavings = type.isSavings
        val change = domain.thirtyDayChange
        return AccountRowUi(
            account = accountMapper.map(domain.account),
            filter = when (type) {
                AccountType.TRANSACTION -> AccountFilterUi.CURRENT
                AccountType.SAVINGS -> AccountFilterUi.SAVINGS
                AccountType.BUSINESS_TRANSACTION, AccountType.BUSINESS_SAVINGS -> AccountFilterUi.BUSINESS
            },
            formattedAvailable = domain.balance?.available?.let(amountFormatter::format),
            formattedCurrent = domain.balance?.current?.let(amountFormatter::format),
            thirdStat = if (isSavings) ThirdStatUi.INTEREST_AER else ThirdStatUi.OVERDRAFT,
            // AER is not in the TrueLayer API (DESIGN_SPEC §4 · 10 / §8).
            thirdStatValue = if (isSavings) NOT_AVAILABLE else domain.balance?.overdraft?.let(amountFormatter::format) ?: NOT_AVAILABLE,
            formattedThirtyDayChange = change?.let(amountFormatter::formatSigned),
            isThirtyDayChangePositive = change == null || change >= BigDecimal.ZERO,
        )
    }

    private companion object {
        const val NOT_AVAILABLE = "—"
    }
}
