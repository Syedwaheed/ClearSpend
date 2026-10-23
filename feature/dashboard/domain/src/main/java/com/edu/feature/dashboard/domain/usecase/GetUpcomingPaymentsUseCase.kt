package com.edu.feature.dashboard.domain.usecase

import com.edu.core.domain.model.DirectDebit
import com.edu.core.domain.model.DirectDebitStatus
import com.edu.core.domain.model.StandingOrder
import com.edu.core.domain.model.StandingOrderFrequency
import com.edu.feature.dashboard.domain.model.UpcomingPayment
import com.edu.feature.dashboard.domain.model.UpcomingPaymentType
import java.time.Clock
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Upcoming bills (DESIGN_SPEC §4 · 09): standing orders use
 * `next_payment_date`; direct debits have no next date in the API, so it is
 * `previous_payment_timestamp` + 1 month (rolled forward until it is not in
 * the past). Only active DDs count. Sorted by due date.
 */
class GetUpcomingPaymentsUseCase(
    private val clock: Clock,
) {

    operator fun invoke(
        directDebits: List<DirectDebit>,
        standingOrders: List<StandingOrder>,
        limit: Int = Int.MAX_VALUE,
    ): List<UpcomingPayment> {
        val today = LocalDate.now(clock)
        val fromDirectDebits = directDebits.mapNotNull { dd ->
            if (dd.status != DirectDebitStatus.ACTIVE) return@mapNotNull null
            val amount = dd.previousPaymentAmount ?: return@mapNotNull null
            val previous = dd.previousPaymentTimestamp?.atZone(clock.zone)?.toLocalDate() ?: return@mapNotNull null
            var next = previous.plusMonths(1)
            while (next.isBefore(today)) next = next.plusMonths(1)
            UpcomingPayment(dd.directDebitId, dd.name, UpcomingPaymentType.DIRECT_DEBIT, amount.abs(), next, daysBetween(today, next))
        }
        val fromStandingOrders = standingOrders.mapNotNull { so ->
            var next = so.nextPaymentDate ?: return@mapNotNull null
            while (next.isBefore(today)) next = advance(next, so.frequency) ?: return@mapNotNull null
            UpcomingPayment(so.standingOrderId, so.payee, UpcomingPaymentType.STANDING_ORDER, so.nextPaymentAmount.abs(), next, daysBetween(today, next))
        }
        return (fromDirectDebits + fromStandingOrders)
            .sortedWith(compareBy<UpcomingPayment> { it.dueDate }.thenByDescending { it.amount })
            .take(limit)
    }

    /** A stale `next_payment_date` is rolled forward by the order's frequency; unknown cadences are dropped. */
    private fun advance(date: LocalDate, frequency: StandingOrderFrequency): LocalDate? = when (frequency) {
        StandingOrderFrequency.WEEKLY -> date.plusWeeks(1)
        StandingOrderFrequency.MONTHLY -> date.plusMonths(1)
        StandingOrderFrequency.ANNUALLY -> date.plusYears(1)
        StandingOrderFrequency.OTHER -> null
    }

    private fun daysBetween(from: LocalDate, to: LocalDate): Int = ChronoUnit.DAYS.between(from, to).toInt()
}
