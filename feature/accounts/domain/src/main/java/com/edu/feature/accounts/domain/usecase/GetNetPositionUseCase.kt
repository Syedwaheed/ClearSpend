package com.edu.feature.accounts.domain.usecase

import com.edu.core.domain.model.AccountWithBalance
import com.edu.core.domain.model.CardSummary
import com.edu.core.domain.model.isCurrent
import com.edu.core.domain.model.isSavings
import com.edu.feature.accounts.domain.model.NetPosition
import java.math.BigDecimal

/**
 * Net position (DESIGN_SPEC §4 · 10) = Σ `available` of all accounts − Σ card
 * `current`, split into Current / Savings / Card owed by `account_type`.
 */
class GetNetPositionUseCase {

    operator fun invoke(accounts: List<AccountWithBalance>, cards: List<CardSummary>): NetPosition {
        fun sumOf(predicate: (AccountWithBalance) -> Boolean) = accounts
            .filter(predicate)
            .fold(BigDecimal.ZERO) { sum, item -> sum + (item.balance?.available ?: BigDecimal.ZERO) }

        val current = sumOf { it.account.accountType.isCurrent }
        val savings = sumOf { it.account.accountType.isSavings }
        val cardOwed = cards.fold(BigDecimal.ZERO) { sum, card -> sum + (card.balance?.current?.max(BigDecimal.ZERO) ?: BigDecimal.ZERO) }
        return NetPosition(
            total = current + savings - cardOwed,
            current = current,
            savings = savings,
            cardOwed = cardOwed,
            accountCount = accounts.size,
        )
    }
}
