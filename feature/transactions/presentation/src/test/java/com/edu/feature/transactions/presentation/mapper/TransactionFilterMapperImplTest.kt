package com.edu.feature.transactions.presentation.mapper

import androidx.compose.ui.graphics.toArgb
import com.edu.core.domain.model.AccountType
import com.edu.core.presentation.designsystem.AccountTypeSavings
import com.edu.feature.transactions.domain.model.CategoryFilter
import com.edu.feature.transactions.domain.model.TransactionPeriod
import com.edu.feature.transactions.presentation.fake.account
import com.edu.feature.transactions.presentation.model.CategoryChipUi
import com.edu.feature.transactions.presentation.model.PeriodChipUi
import com.edu.feature.transactions.presentation.model.PeriodSelectionUi
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class TransactionFilterMapperImplTest {

    private val mapper = TransactionFilterMapperImpl()

    private fun utcMillis(date: LocalDate) = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    @Test
    fun `Should map This Month`() {
        assertEquals(TransactionPeriod.ThisMonth, mapper.toPeriod(PeriodSelectionUi(PeriodChipUi.THIS_MONTH)))
    }

    @Test
    fun `Should map Last Month`() {
        assertEquals(TransactionPeriod.LastMonth, mapper.toPeriod(PeriodSelectionUi(PeriodChipUi.LAST_MONTH)))
    }

    @Test
    fun `Should map All`() {
        assertEquals(TransactionPeriod.All, mapper.toPeriod(PeriodSelectionUi()))
    }

    @Test
    fun `Should turn picker millis into calendar dates`() {
        val from = LocalDate.of(2026, 5, 21)
        val to = LocalDate.of(2026, 6, 1)

        assertEquals(
            TransactionPeriod.Custom(from, to),
            mapper.toPeriod(PeriodSelectionUi(PeriodChipUi.CUSTOM_RANGE, utcMillis(from), utcMillis(to))),
        )
    }

    @Test
    fun `Should treat a range without an end as one day`() {
        val day = LocalDate.of(2026, 6, 1)

        assertEquals(TransactionPeriod.Custom(day, day), mapper.toPeriod(PeriodSelectionUi(PeriodChipUi.CUSTOM_RANGE, utcMillis(day), null)))
    }

    @Test
    fun `Should fall back to All When a custom range has no dates`() {
        assertEquals(TransactionPeriod.All, mapper.toPeriod(PeriodSelectionUi(PeriodChipUi.CUSTOM_RANGE)))
    }

    @Test
    fun `Should map the Cash chip`() {
        assertEquals(CategoryFilter.CASH, mapper.toCategory(CategoryChipUi.CASH))
    }

    @Test
    fun `Should map the Income chip to credits`() {
        assertEquals(CategoryFilter.CREDIT, mapper.toCategory(CategoryChipUi.CREDIT))
    }
}

class AccountOptionUiMapperImplTest {

    private val mapper = AccountOptionUiMapperImpl()

    @Test
    fun `Should use the provider initial`() {
        assertEquals("M", mapper.map(account("a", provider = "monzo")).initial)
    }

    @Test
    fun `Should colour by account type`() {
        assertEquals(AccountTypeSavings.toArgb(), mapper.map(account("a", type = AccountType.SAVINGS)).colorArgb)
    }

    @Test
    fun `Should fall back to the account name initial When the provider has no name`() {
        assertEquals("A", mapper.map(account("a", provider = "")).initial)
    }

    @Test
    fun `Should carry the account id`() {
        assertEquals("a", mapper.map(account("a")).accountId)
    }
}
