package com.edu.feature.accounts.presentation.mapper

import com.edu.core.presentation.ui.formatter.AmountFormatter
import com.edu.feature.accounts.domain.model.AccountsOverview
import com.edu.feature.accounts.presentation.model.NetPositionUi
import java.math.BigDecimal

class NetPositionUiMapperImpl(
    private val amountFormatter: AmountFormatter,
) : NetPositionUiMapper {

    override fun map(domain: AccountsOverview): NetPositionUi {
        val net = domain.netPosition
        val change = domain.history.changeThisMonth
        return NetPositionUi(
            formattedTotal = amountFormatter.format(net.total),
            accountCount = net.accountCount,
            formattedChangeThisMonth = amountFormatter.formatSigned(change),
            isChangePositive = change >= BigDecimal.ZERO,
            sparkline = sparkline(domain.history.weeklyNetBalances),
            formattedCurrent = amountFormatter.format(net.current),
            formattedSavings = amountFormatter.format(net.savings),
            formattedCardOwed = amountFormatter.format(net.cardOwed),
        )
    }

    /** Bar height = value ÷ max (as the design), floored so a bar never disappears. */
    private fun sparkline(values: List<BigDecimal>): List<Float> {
        val max = values.maxOrNull()?.toFloat() ?: return emptyList()
        if (max <= 0f) return values.map { MIN_BAR }
        return values.map { (it.toFloat() / max).coerceIn(MIN_BAR, 1f) }
    }

    private companion object {
        const val MIN_BAR = 0.08f
    }
}
