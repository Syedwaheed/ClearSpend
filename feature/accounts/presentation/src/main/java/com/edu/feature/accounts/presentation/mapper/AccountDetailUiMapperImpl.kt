package com.edu.feature.accounts.presentation.mapper

import com.edu.core.presentation.ui.formatter.AmountFormatter
import com.edu.core.presentation.ui.formatter.IbanFormatter
import com.edu.core.presentation.ui.mapper.SyncTimeUiMapper
import com.edu.core.presentation.ui.mapper.TransactionUiMapper
import com.edu.core.presentation.ui.mapper.mapList
import com.edu.feature.accounts.domain.model.AccountDetail
import com.edu.feature.accounts.presentation.model.AccountDetailUi
import java.math.BigDecimal

class AccountDetailUiMapperImpl(
    private val amountFormatter: AmountFormatter,
    private val ibanFormatter: IbanFormatter,
    private val transactionMapper: TransactionUiMapper,
    private val syncTimeMapper: SyncTimeUiMapper,
) : AccountDetailUiMapper {

    override fun map(domain: AccountDetail): AccountDetailUi {
        val number = domain.account.accountNumber
        return AccountDetailUi(
            accountId = domain.account.accountId,
            title = domain.account.displayName,
            formattedAvailable = domain.balance?.available?.let(amountFormatter::format),
            formattedCurrent = domain.balance?.current?.let(amountFormatter::format),
            formattedOverdraft = domain.balance?.overdraft?.max(BigDecimal.ZERO)?.let(amountFormatter::format),
            sortCode = number.sortCode?.takeIf { it.isNotBlank() }?.let(ibanFormatter::formatSortCode),
            accountNumber = number.number?.takeIf { it.isNotBlank() },
            iban = number.iban?.replace(" ", "")?.takeIf { it.isNotBlank() },
            currency = domain.account.currency,
            recentTransactions = transactionMapper.mapList(domain.recentTransactions),
            offlineSyncTime = domain.offlineSince?.let(syncTimeMapper::map),
        )
    }
}
