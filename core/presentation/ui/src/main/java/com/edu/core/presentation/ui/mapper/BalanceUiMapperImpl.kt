package com.edu.core.presentation.ui.mapper

import com.edu.core.domain.model.Balance
import com.edu.core.presentation.ui.formatter.AmountFormatter
import com.edu.core.presentation.ui.formatter.DateFormatter
import com.edu.core.presentation.ui.model.BalanceUi
import java.math.BigDecimal

class BalanceUiMapperImpl(
    private val amountFormatter: AmountFormatter,
    private val dateFormatter: DateFormatter,
) : BalanceUiMapper {

    override fun map(domain: Balance): BalanceUi = BalanceUi(
        accountId = domain.accountId,
        formattedAvailable = amountFormatter.format(domain.available),
        formattedCurrent = amountFormatter.format(domain.current),
        formattedOverdraft = domain.overdraft
            .takeIf { it > BigDecimal.ZERO }
            ?.let(amountFormatter::format),
        hasOverdraft = domain.overdraft > BigDecimal.ZERO,
        formattedUpdateDate = dateFormatter.formatDate(domain.updateTimestamp),
    )
}
