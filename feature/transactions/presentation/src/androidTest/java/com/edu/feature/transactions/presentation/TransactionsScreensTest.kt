package com.edu.feature.transactions.presentation

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.hasText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.edu.core.domain.model.Account
import com.edu.core.domain.model.AccountNumber
import com.edu.core.domain.model.AccountType
import com.edu.core.domain.model.Provider
import com.edu.core.domain.model.Transaction
import com.edu.core.domain.model.TransactionCategory
import com.edu.core.domain.model.TransactionType
import com.edu.core.presentation.designsystem.ClearSpendTheme
import com.edu.core.presentation.ui.formatter.AmountFormatterImpl
import com.edu.core.presentation.ui.formatter.DateFormatterImpl
import com.edu.core.presentation.ui.mapper.SyncTimeUiMapperImpl
import com.edu.core.presentation.ui.mapper.TransactionUiMapperImpl
import com.edu.feature.transactions.domain.model.CategoryFilter
import com.edu.feature.transactions.domain.model.MerchantInsight
import com.edu.feature.transactions.domain.model.TransactionDetail
import com.edu.feature.transactions.domain.model.TransactionPeriod
import com.edu.feature.transactions.domain.usecase.GetFilteredTransactionsUseCase
import com.edu.feature.transactions.domain.usecase.GetPeriodBoundsUseCase
import com.edu.feature.transactions.domain.usecase.GetTransactionDayGroupsUseCase
import com.edu.feature.transactions.domain.usecase.GetTransactionsOverviewUseCase
import com.edu.feature.transactions.domain.usecase.GetTransactionsSummaryUseCase
import com.edu.feature.transactions.presentation.detail.TransactionDetailAction
import com.edu.feature.transactions.presentation.detail.TransactionDetailContent
import com.edu.feature.transactions.presentation.detail.TransactionDetailUiState
import com.edu.feature.transactions.presentation.list.TransactionsAction
import com.edu.feature.transactions.presentation.list.TransactionsContent
import com.edu.feature.transactions.presentation.list.TransactionsTestTags
import com.edu.feature.transactions.presentation.list.TransactionsUiState
import com.edu.feature.transactions.presentation.mapper.TransactionDetailUiMapperImpl
import com.edu.feature.transactions.presentation.mapper.TransactionsContentUiMapperImpl
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneOffset
import java.util.Locale

/**
 * Renders the real Transactions (12/13) and Transaction Detail (14-16)
 * composables from test data run through the real use cases and mappers,
 * checks the key states, and saves a capture of each to the test app's
 * external files dir (`screens/`) for side-by-side comparison with
 * designs/screens. Test data only — nothing here ships.
 */
@RunWith(AndroidJUnit4::class)
class TransactionsScreensTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val clock = Clock.fixed(Instant.parse("2026-06-01T09:30:00Z"), ZoneOffset.UTC)
    private val amountFormatter = AmountFormatterImpl(Locale.UK)
    private val dateFormatter = DateFormatterImpl(Locale.UK, clock)
    private val rowMapper = TransactionUiMapperImpl(amountFormatter, dateFormatter)
    private val contentMapper = TransactionsContentUiMapperImpl(rowMapper, amountFormatter, dateFormatter, SyncTimeUiMapperImpl(dateFormatter), Locale.UK)
    private val detailMapper = TransactionDetailUiMapperImpl(rowMapper, amountFormatter, dateFormatter, SyncTimeUiMapperImpl(dateFormatter))
    private val overview = GetTransactionsOverviewUseCase(
        GetPeriodBoundsUseCase(clock),
        GetFilteredTransactionsUseCase(),
        GetTransactionDayGroupsUseCase(clock),
        GetTransactionsSummaryUseCase(),
        clock,
    )

    private fun tx(
        id: String,
        time: String,
        amount: String,
        merchant: String,
        category: TransactionCategory,
        classification: List<String>,
        balance: String? = null,
        pending: Boolean = false,
    ) = Transaction(
        transactionId = id,
        accountId = "acc-1",
        timestamp = Instant.parse(time),
        description = merchant.uppercase(),
        merchantName = merchant,
        amount = BigDecimal(amount),
        transactionType = if (amount.startsWith("-")) TransactionType.DEBIT else TransactionType.CREDIT,
        transactionCategory = category,
        classification = classification,
        runningBalance = balance?.let(::BigDecimal),
        isPending = pending,
    )

    private val settled = listOf(
        tx("t1", "2026-06-01T06:00:00Z", "-46.82", "TalkTalk Telecom", TransactionCategory.PURCHASE, listOf("Bills and Utilities", "Internet"), "44.01"),
        tx("t2", "2026-06-01T06:00:00Z", "-0.01", "Account Overdraft Fee", TransactionCategory.BILL_PAYMENT, listOf("Fees and Charges", "Bank fee"), "90.83"),
        tx("t3", "2026-06-01T06:00:00Z", "-227.00", "Mr John Smith", TransactionCategory.TRANSFER, listOf("Transfers"), "90.84"),
        tx("t4", "2026-06-01T06:00:00Z", "-39.99", "Tesco", TransactionCategory.PURCHASE, listOf("Shopping", "Groceries"), "317.84"),
        tx("t5", "2026-05-28T06:00:00Z", "420.00", "TSB Cleveleys", TransactionCategory.CASH, listOf("Cash", "Deposit"), "357.83"),
        tx("t6", "2026-05-27T09:14:00Z", "-24.50", "EDF Energy", TransactionCategory.DIRECT_DEBIT, listOf("Bills and Utilities", "Energy"), "-62.17"),
    )
    private val pending = listOf(
        tx("p1", "2026-06-01T08:41:00Z", "-4.65", "Costa Coffee", TransactionCategory.PURCHASE, listOf("Food and Dining", "Coffee"), pending = true),
        tx("p2", "2026-06-01T08:00:00Z", "-14.90", "Uber", TransactionCategory.PURCHASE, listOf("Transport", "Taxi"), pending = true),
    )

    private fun listState(query: String = "") = TransactionsUiState(
        query = query,
        content = contentMapper.map(overview(settled, pending, TransactionPeriod.All, query, CategoryFilter.ALL, null)),
    )

    private val account = Account(
        accountId = "acc-1",
        accountType = AccountType.TRANSACTION,
        displayName = "Transaction Account 1",
        provider = Provider("ob-monzo", "Monzo"),
        accountNumber = AccountNumber(),
    )

    private fun detailState(transaction: Transaction, insight: MerchantInsight) =
        TransactionDetailUiState(detail = detailMapper.map(TransactionDetail(transaction, account, insight, null)))

    private fun capture(name: String) {
        composeRule.waitForIdle()
        val bitmap = composeRule.onRoot().captureToImage().asAndroidBitmap()
        val dir = InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir("screens") ?: return
        File(dir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test
    fun transactionsListShowsPendingAndDayGroups() {
        composeRule.setContent { ClearSpendTheme(darkTheme = false) { TransactionsContent(listState(), onAction = {}) } }

        composeRule.onNodeWithTag(TransactionsTestTags.PENDING).assertIsDisplayed()
        capture("12-transactions-top")
        composeRule.onNodeWithTag(TransactionsTestTags.LIST).performScrollToNode(hasText("WEDNESDAY — 27 MAY 2026"))
        capture("12-transactions-groups")
    }

    @Test
    fun noSearchResultsShowsTheEmptyState() {
        composeRule.setContent { ClearSpendTheme(darkTheme = false) { TransactionsContent(listState(query = "zzzz"), onAction = {}) } }

        composeRule.onNodeWithText("No transactions found").assertIsDisplayed()
        capture("13-transactions-empty")
    }

    @Test
    fun clearFiltersSendsTheAction() {
        val actions = mutableListOf<TransactionsAction>()
        composeRule.setContent { ClearSpendTheme(darkTheme = false) { TransactionsContent(listState(query = "zzzz")) { actions += it } } }

        composeRule.onNodeWithText("Clear filters").performClick()

        assertEquals(TransactionsAction.ClearFiltersClicked, actions.last())
    }

    @Test
    fun loadingShowsTheSkeleton() {
        composeRule.setContent { ClearSpendTheme(darkTheme = false) { TransactionsContent(TransactionsUiState(isLoading = true), onAction = {}) } }

        composeRule.onNodeWithTag(TransactionsTestTags.SKELETON).assertIsDisplayed()
    }

    @Test
    fun settledDetailShowsTheSettledBadge() {
        val state = detailState(
            settled[3],
            MerchantInsight.MonthlyTotal("Tesco", YearMonth.of(2026, 6), BigDecimal("156.99"), 8, isTopMerchant = true),
        )
        composeRule.setContent { ClearSpendTheme(darkTheme = false) { TransactionDetailContent(state, onAction = {}) } }

        composeRule.onNodeWithText("Settled").assertIsDisplayed()
        capture("14-detail-settled")
    }

    @Test
    fun pendingDetailHasNoBalanceAfter() {
        val state = detailState(pending[0], MerchantInsight.FirstTime("Costa Coffee", 90))
        composeRule.setContent { ClearSpendTheme(darkTheme = false) { TransactionDetailContent(state, onAction = {}) } }

        composeRule.onNodeWithText("Pending").assertIsDisplayed()
        capture("15-detail-pending")
    }

    @Test
    fun directDebitDetailOffersManageInBills() {
        val actions = mutableListOf<TransactionDetailAction>()
        val state = detailState(
            settled[5],
            MerchantInsight.MonthlyTotal("EDF Energy", YearMonth.of(2026, 5), BigDecimal("24.50"), 1, isTopMerchant = true),
        )
        composeRule.setContent { ClearSpendTheme(darkTheme = false) { TransactionDetailContent(state) { actions += it } } }
        capture("16-detail-direct-debit")

        composeRule.onNodeWithText("Recurring Direct Debit · manage in Bills").performClick()

        assertEquals(TransactionDetailAction.ManageInBillsClicked, actions.last())
    }
}
