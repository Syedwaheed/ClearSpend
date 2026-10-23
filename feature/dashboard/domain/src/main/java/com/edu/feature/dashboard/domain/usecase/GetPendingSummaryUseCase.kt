package com.edu.feature.dashboard.domain.usecase

import com.edu.core.domain.model.Transaction
import com.edu.feature.dashboard.domain.model.PendingSummary
import java.math.BigDecimal

/** Pending banner: count of `/transactions/pending` and |Σ amount| not yet settled. */
class GetPendingSummaryUseCase {
    operator fun invoke(pending: List<Transaction>): PendingSummary = PendingSummary(
        count = pending.size,
        total = pending.fold(BigDecimal.ZERO) { sum, t -> sum + t.amount }.abs(),
    )
}
