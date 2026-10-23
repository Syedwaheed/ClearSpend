package com.edu.feature.dashboard.domain.usecase

import com.edu.core.domain.model.AccountWithBalance
import com.edu.core.domain.model.isCurrent
import com.edu.feature.dashboard.domain.model.Payday
import com.edu.feature.dashboard.domain.model.SafeToSpend
import com.edu.feature.dashboard.domain.model.UpcomingPayment
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth

/**
 * Safe to spend (DESIGN_SPEC §4 · 09) = Σ `available` of current accounts +
 * their `overdraft` − Σ upcoming bills due before payday. Without a detected
 * payday the horizon is the end of the current month. Also yields the Bills
 * due / Spent / Yours bar shares.
 */
class GetSafeToSpendUseCase(
    private val clock: Clock,
) {

    operator fun invoke(
        accounts: List<AccountWithBalance>,
        upcomingPayments: List<UpcomingPayment>,
        payday: Payday?,
        spentThisMonth: BigDecimal,
    ): SafeToSpend? {
        val balances = accounts.filter { it.account.accountType.isCurrent }.mapNotNull { it.balance }
        if (balances.isEmpty()) return null

        val spendable = balances.fold(BigDecimal.ZERO) { sum, b -> sum + b.available + b.overdraft.max(BigDecimal.ZERO) }
        val today = LocalDate.now(clock)
        val billsDue = upcomingPayments
            .filter { isBeforeHorizon(it.dueDate, today, payday) }
            .fold(BigDecimal.ZERO) { sum, p -> sum + p.amount }
        val safe = (spendable - billsDue).max(BigDecimal.ZERO)
        val spent = spentThisMonth.abs()

        val base = billsDue + spent + safe
        fun share(part: BigDecimal) =
            if (base.signum() == 0) BigDecimal.ZERO else part.divide(base, 4, RoundingMode.HALF_UP)

        return SafeToSpend(
            amount = safe,
            billsDue = billsDue,
            spentThisMonth = spent,
            billsShare = share(billsDue),
            spentShare = share(spent),
            yoursShare = share(safe),
        )
    }

    private fun isBeforeHorizon(dueDate: LocalDate, today: LocalDate, payday: Payday?): Boolean {
        if (dueDate.isBefore(today)) return false
        return if (payday != null) {
            dueDate.isBefore(payday.nextDate)
        } else {
            !dueDate.isAfter(YearMonth.from(today).atEndOfMonth())
        }
    }
}
