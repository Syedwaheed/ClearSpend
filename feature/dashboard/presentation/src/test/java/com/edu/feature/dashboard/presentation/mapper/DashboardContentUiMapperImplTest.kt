package com.edu.feature.dashboard.presentation.mapper

import com.edu.core.presentation.ui.formatter.IbanFormatterImpl
import com.edu.core.presentation.ui.mapper.AccountUiMapperImpl
import com.edu.core.presentation.ui.mapper.BalanceUiMapperImpl
import com.edu.core.presentation.ui.mapper.CardSummaryUiMapperImpl
import com.edu.core.presentation.ui.mapper.SyncTimeUiMapperImpl
import com.edu.core.presentation.ui.mapper.TransactionUiMapperImpl
import com.edu.feature.dashboard.domain.model.Payday
import com.edu.feature.dashboard.domain.model.PendingSummary
import com.edu.feature.dashboard.domain.model.SafeToSpend
import com.edu.feature.dashboard.presentation.fake.NOW
import com.edu.feature.dashboard.presentation.fake.amountFormatter
import com.edu.feature.dashboard.presentation.fake.dashboard
import com.edu.feature.dashboard.presentation.fake.dateFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

class DashboardContentUiMapperImplTest {

    private val mapper = DashboardContentUiMapperImpl(
        headerMapper = DashboardHeaderUiMapperImpl(dateFormatter),
        accountMapper = AccountUiMapperImpl(IbanFormatterImpl()),
        balanceMapper = BalanceUiMapperImpl(amountFormatter, dateFormatter),
        safeToSpendMapper = SafeToSpendUiMapperImpl(amountFormatter),
        cashFlowMapper = CashFlowUiMapperImpl(amountFormatter, dateFormatter),
        pendingMapper = PendingUiMapperImpl(amountFormatter),
        upcomingPaymentMapper = UpcomingPaymentUiMapperImpl(amountFormatter),
        categorySpendMapper = CategorySpendUiMapperImpl(amountFormatter),
        cardSummaryMapper = CardSummaryUiMapperImpl(amountFormatter, dateFormatter),
        transactionMapper = TransactionUiMapperImpl(amountFormatter, dateFormatter),
        syncTimeMapper = SyncTimeUiMapperImpl(dateFormatter),
    )

    private val safe = SafeToSpend(BigDecimal.TEN, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE)

    @Test
    fun `map attaches the days to payday to safe to spend`() {
        val ui = mapper.map(dashboard().copy(safeToSpend = safe, payday = Payday(LocalDate.of(2026, 9, 28), 12, BigDecimal.TEN)))

        assertEquals(12, ui.safeToSpend?.daysToPayday)
    }

    @Test
    fun `map hides the pending banner When nothing is pending`() {
        assertNull(mapper.map(dashboard().copy(pending = PendingSummary(0, BigDecimal.ZERO))).pending)
    }

    @Test
    fun `map shows the offline sync time When data is cached`() {
        assertEquals("09:30", mapper.map(dashboard().copy(offlineSince = NOW)).offlineSyncTime)
    }

    @Test
    fun `map pairs each account with its balance`() {
        assertEquals("£1.00", mapper.map(dashboard()).accounts.single().balance?.formattedAvailable)
    }
}
