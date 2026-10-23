package com.edu.feature.transactions.domain.usecase

import com.edu.feature.transactions.domain.model.DateWindow
import com.edu.feature.transactions.domain.model.PageWindow
import com.edu.feature.transactions.domain.model.PeriodBounds
import java.time.LocalDate

/**
 * Date-window pagination (DESIGN_SPEC §4 · 12: "load 30 days, extend on
 * scroll"). The first page is the newest [PAGE_DAYS] days of the period; each
 * next page is the [PAGE_DAYS] days before the oldest loaded day, clipped to
 * the period's start.
 */
class GetPageWindowUseCase {

    /**
     * @param loadedFrom oldest day already loaded, or null for the first page.
     * @return null when the period is fully loaded.
     */
    operator fun invoke(bounds: PeriodBounds, loadedFrom: LocalDate?): PageWindow? {
        val to = if (loadedFrom == null) bounds.latest else loadedFrom.minusDays(1)
        if (to < bounds.earliest) return null
        val from = maxOf(bounds.earliest, to.minusDays(PAGE_DAYS - 1))
        return PageWindow(DateWindow(from, to), hasMore = from > bounds.earliest)
    }

    companion object {
        const val PAGE_DAYS = 30L
    }
}
