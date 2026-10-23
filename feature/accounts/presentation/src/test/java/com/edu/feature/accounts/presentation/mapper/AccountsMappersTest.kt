package com.edu.feature.accounts.presentation.mapper

import com.edu.core.domain.model.AccountType
import com.edu.core.domain.model.Transaction
import com.edu.core.domain.model.TransactionCategory
import com.edu.core.domain.model.TransactionType
import com.edu.core.presentation.ui.formatter.IbanFormatterImpl
import com.edu.core.presentation.ui.mapper.CardSummaryUiMapperImpl
import com.edu.core.presentation.ui.mapper.SyncTimeUiMapperImpl
import com.edu.core.presentation.ui.mapper.TransactionUiMapperImpl
import com.edu.feature.accounts.domain.model.AccountDetail
import com.edu.feature.accounts.presentation.fake.NOW
import com.edu.feature.accounts.presentation.fake.account
import com.edu.feature.accounts.presentation.fake.accountUiMapper
import com.edu.feature.accounts.presentation.fake.amountFormatter
import com.edu.feature.accounts.presentation.fake.balance
import com.edu.feature.accounts.presentation.fake.dateFormatter
import com.edu.feature.accounts.presentation.fake.group
import com.edu.feature.accounts.presentation.fake.item
import com.edu.feature.accounts.presentation.fake.overview
import com.edu.feature.accounts.presentation.model.AccountFilterUi
import com.edu.feature.accounts.presentation.model.ThirdStatUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class AccountRowUiMapperImplTest {

    private val mapper = AccountRowUiMapperImpl(accountUiMapper, amountFormatter)

    @Test
    fun `map formats the available balance`() {
        assertEquals("£44.01", mapper.map(item("a")).formattedAvailable)
    }

    @Test
    fun `map shows the overdraft for a current account`() {
        assertEquals("£100.00", mapper.map(item("a")).thirdStatValue)
    }

    @Test
    fun `map shows a dash for savings AER because the API has none`() {
        val row = mapper.map(item("s", AccountType.SAVINGS))

        assertEquals(ThirdStatUi.INTEREST_AER to "—", row.thirdStat to row.thirdStatValue)
    }

    @Test
    fun `map signs a negative 30-day change`() {
        assertEquals("-£312.45", mapper.map(item("a")).formattedThirtyDayChange)
    }

    @Test
    fun `map marks a negative change`() {
        assertFalse(mapper.map(item("a")).isThirtyDayChangePositive)
    }

    @Test
    fun `map leaves the change empty When history is unavailable`() {
        assertNull(mapper.map(item("a", change = null)).formattedThirtyDayChange)
    }

    @Test
    fun `map files business savings under the business chip`() {
        assertEquals(AccountFilterUi.BUSINESS, mapper.map(item("b", AccountType.BUSINESS_SAVINGS)).filter)
    }

    @Test
    fun `map shows no balance When it failed to load`() {
        assertNull(mapper.map(item("a").copy(balance = null)).formattedAvailable)
    }
}

class ProviderGroupUiMapperImplTest {

    private val mapper = ProviderGroupUiMapperImpl(AccountRowUiMapperImpl(accountUiMapper, amountFormatter), amountFormatter)

    @Test
    fun `map formats the subtotal`() {
        assertEquals("£2,194.01", mapper.map(group(item("a"))).formattedSubtotal)
    }

    @Test
    fun `map takes the provider initial as the logo fallback`() {
        assertEquals("M", mapper.map(group(item("a"))).initial)
    }

    @Test
    fun `map keeps the rows`() {
        assertEquals(2, mapper.map(group(item("a"), item("b"))).accounts.size)
    }
}

class NetPositionUiMapperImplTest {

    private val mapper = NetPositionUiMapperImpl(amountFormatter)

    @Test
    fun `map formats the net position`() {
        assertEquals("£3,894.76", mapper.map(overview()).formattedTotal)
    }

    @Test
    fun `map signs this month's change`() {
        assertEquals("+£1,127.85", mapper.map(overview()).formattedChangeThisMonth)
    }

    @Test
    fun `map scales the sparkline to the largest week`() {
        assertEquals(1f, mapper.map(overview()).sparkline.last(), 0.0001f)
    }

    @Test
    fun `map floors bars When everything is negative`() {
        val negative = overview().let { it.copy(history = it.history.copy(weeklyNetBalances = List(8) { BigDecimal("-5") })) }

        assertEquals(0.08f, mapper.map(negative).sparkline.first(), 0.0001f)
    }

    @Test
    fun `map formats the card owed`() {
        assertEquals("£1,580.00", mapper.map(overview()).formattedCardOwed)
    }
}

class AccountsContentUiMapperImplTest {

    private val mapper = AccountsContentUiMapperImpl(
        netPositionMapper = NetPositionUiMapperImpl(amountFormatter),
        groupMapper = ProviderGroupUiMapperImpl(AccountRowUiMapperImpl(accountUiMapper, amountFormatter), amountFormatter),
        cardMapper = CardSummaryUiMapperImpl(amountFormatter, dateFormatter),
        syncTimeMapper = SyncTimeUiMapperImpl(dateFormatter),
        amountFormatter = amountFormatter,
        dateFormatter = dateFormatter,
    )

    @Test
    fun `map shows consent days and when it last synced`() {
        val consent = mapper.map(overview()).consent!!

        assertEquals(68 to "2 min ago", consent.daysRemaining to consent.syncedAgo)
    }

    @Test
    fun `map hides the consent banner When no consent date is stored`() {
        assertNull(mapper.map(overview(consent = null)).consent)
    }

    @Test
    fun `map shows the offline time When served from cache`() {
        assertEquals("09:30", mapper.map(overview().copy(offlineSince = NOW)).offlineSyncTime)
    }
}

class AccountDetailUiMapperImplTest {

    private val mapper = AccountDetailUiMapperImpl(
        amountFormatter = amountFormatter,
        ibanFormatter = IbanFormatterImpl(),
        transactionMapper = TransactionUiMapperImpl(amountFormatter, dateFormatter),
        syncTimeMapper = SyncTimeUiMapperImpl(dateFormatter),
    )

    private val tx = Transaction(
        transactionId = "t", accountId = "a", timestamp = NOW, description = "TalkTalk", amount = BigDecimal("-46.82"),
        transactionType = TransactionType.DEBIT, transactionCategory = TransactionCategory.PURCHASE,
    )
    private val detail = AccountDetail(account("a"), balance("a"), listOf(tx), offlineSince = null)

    @Test
    fun `map formats the sort code`() {
        assertEquals("01-21-31", mapper.map(detail).sortCode)
    }

    @Test
    fun `map shows the full account number on the detail screen`() {
        assertEquals("00000000", mapper.map(detail).accountNumber)
    }

    @Test
    fun `map strips spaces from the IBAN`() {
        assertEquals("GB29MONZ01213100000000", mapper.map(detail).iban)
    }

    @Test
    fun `map formats the overdraft`() {
        assertEquals("£100.00", mapper.map(detail).formattedOverdraft)
    }

    @Test
    fun `map maps recent transactions`() {
        assertEquals("-£46.82", mapper.map(detail).recentTransactions.single().formattedAmount)
    }

    @Test
    fun `map leaves balances empty When the balance failed`() {
        assertNull(mapper.map(detail.copy(balance = null)).formattedAvailable)
    }

    @Test
    fun `map hides a missing IBAN`() {
        val noIban = detail.copy(account = account("a").copy(accountNumber = account("a").accountNumber.copy(iban = null)))

        assertTrue(mapper.map(noIban).iban == null)
    }
}
