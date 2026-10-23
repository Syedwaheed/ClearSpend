package com.edu.feature.transactions.presentation.mapper

import com.edu.feature.transactions.domain.model.CategoryFilter
import com.edu.feature.transactions.domain.model.TransactionPeriod
import com.edu.feature.transactions.presentation.model.CategoryChipUi
import com.edu.feature.transactions.presentation.model.PeriodChipUi
import com.edu.feature.transactions.presentation.model.PeriodSelectionUi
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class TransactionFilterMapperImpl : TransactionFilterMapper {

    override fun toPeriod(selection: PeriodSelectionUi): TransactionPeriod = when (selection.chip) {
        PeriodChipUi.ALL -> TransactionPeriod.All
        PeriodChipUi.THIS_MONTH -> TransactionPeriod.ThisMonth
        PeriodChipUi.LAST_MONTH -> TransactionPeriod.LastMonth
        PeriodChipUi.CUSTOM_RANGE -> {
            val start = selection.customStartUtcMillis
            val end = selection.customEndUtcMillis ?: start
            if (start == null || end == null) {
                TransactionPeriod.All
            } else {
                TransactionPeriod.Custom(start.toPickerDate(), end.toPickerDate())
            }
        }
    }

    override fun toCategory(chip: CategoryChipUi): CategoryFilter = when (chip) {
        CategoryChipUi.ALL -> CategoryFilter.ALL
        CategoryChipUi.PURCHASE -> CategoryFilter.PURCHASE
        CategoryChipUi.DIRECT_DEBIT -> CategoryFilter.DIRECT_DEBIT
        CategoryChipUi.BILL_PAYMENT -> CategoryFilter.BILL_PAYMENT
        CategoryChipUi.TRANSFER -> CategoryFilter.TRANSFER
        CategoryChipUi.CASH -> CategoryFilter.CASH
        CategoryChipUi.CREDIT -> CategoryFilter.CREDIT
    }

    /** DateRangePicker selections are UTC midnight of the picked calendar day. */
    private fun Long.toPickerDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
}
