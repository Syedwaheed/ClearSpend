package com.edu.feature.dashboard.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.edu.core.presentation.designsystem.Blue
import com.edu.core.presentation.designsystem.ClearSpendTheme
import com.edu.core.presentation.designsystem.Error
import com.edu.core.presentation.designsystem.Mint
import com.edu.core.presentation.designsystem.Navy
import com.edu.core.presentation.designsystem.Navy2
import com.edu.core.presentation.designsystem.OnNavy
import com.edu.core.presentation.designsystem.OnSurface
import com.edu.core.presentation.designsystem.OnSurfaceVariant
import com.edu.core.presentation.designsystem.Outline
import com.edu.core.presentation.designsystem.RobotoMono
import com.edu.core.presentation.designsystem.Success
import com.edu.core.presentation.designsystem.Surface
import com.edu.core.presentation.designsystem.SurfaceVariant
import com.edu.core.presentation.designsystem.Warning
import com.edu.core.presentation.ui.AccountCard
import com.edu.core.presentation.ui.BalanceCard
import com.edu.core.presentation.ui.EmptyStateScreen
import com.edu.core.presentation.ui.ErrorScreen
import com.edu.core.presentation.ui.ObserveAsEvent
import com.edu.core.presentation.ui.OfflineBanner
import com.edu.core.presentation.ui.SkeletonBlock
import com.edu.core.presentation.ui.TransactionRow
import com.edu.core.presentation.ui.dashedBorder
import com.edu.core.presentation.ui.model.CardSummaryUi
import com.edu.core.presentation.ui.shimmer
import com.edu.feature.dashboard.presentation.R
import com.edu.feature.dashboard.presentation.model.CashFlowUi
import com.edu.feature.dashboard.presentation.model.CategorySpendUi
import com.edu.feature.dashboard.presentation.model.DashboardContentUi
import com.edu.feature.dashboard.presentation.model.DashboardHeaderUi
import com.edu.feature.dashboard.presentation.model.GreetingPeriodUi
import com.edu.feature.dashboard.presentation.model.PendingUi
import com.edu.feature.dashboard.presentation.model.SafeToSpendUi
import com.edu.feature.dashboard.presentation.model.UpcomingPaymentTypeUi
import com.edu.feature.dashboard.presentation.model.UpcomingPaymentUi
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

/** Bottom padding so the last card scrolls clear of the floating nav (~80dp + FAB). */
private val BottomNavClearance = 112.dp

@Composable
fun DashboardScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToAccount: (accountId: String) -> Unit,
    onNavigateToBankSelection: () -> Unit,
    onOpenSendMoney: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToBills: () -> Unit,
    onNavigateToCard: () -> Unit,
    onNavigateToTransaction: (transactionId: String) -> Unit,
    onNavigateToConnectBank: () -> Unit,
    viewModel: DashboardViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val comingSoon = stringResource(R.string.home_coming_soon)
    ObserveAsEvent(viewModel.effect) { effect ->
        when (effect) {
            DashboardEffect.NavigateToSettings -> onNavigateToSettings()
            DashboardEffect.NavigateToNotifications -> onNavigateToNotifications()
            is DashboardEffect.NavigateToAccount -> onNavigateToAccount(effect.accountId)
            DashboardEffect.NavigateToBankSelection -> onNavigateToBankSelection()
            DashboardEffect.OpenSendMoney -> onOpenSendMoney()
            DashboardEffect.NavigateToAnalytics -> onNavigateToAnalytics()
            DashboardEffect.NavigateToTransactions -> onNavigateToTransactions()
            DashboardEffect.NavigateToBills -> onNavigateToBills()
            DashboardEffect.NavigateToCard -> onNavigateToCard()
            is DashboardEffect.NavigateToTransaction -> onNavigateToTransaction(effect.transactionId)
            DashboardEffect.NavigateToConnectBank -> onNavigateToConnectBank()
            DashboardEffect.ShowComingSoon -> scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(comingSoon)
            }
        }
    }
    Box(Modifier.fillMaxSize()) {
        DashboardContent(state = state, onAction = viewModel::onAction)
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = BottomNavClearance),
        )
    }
}

@Composable
fun DashboardContent(
    state: DashboardUiState,
    onAction: (DashboardAction) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface),
    ) {
        val content = state.content
        when {
            state.isLoading && content == null -> DashboardSkeleton()
            state.hasError -> ErrorScreen(
                onRetry = { onAction(DashboardAction.RetryClicked) },
                modifier = Modifier.statusBarsPadding(),
            )
            state.isEmpty -> EmptyStateScreen(
                title = stringResource(R.string.home_empty_title),
                subtitle = stringResource(R.string.home_empty_body),
                icon = Icons.Outlined.AccountBalance,
                ctaText = stringResource(R.string.home_empty_cta),
                onCtaClick = { onAction(DashboardAction.ConnectAnotherBankClicked) },
                modifier = Modifier.statusBarsPadding(),
            )
            content != null -> DashboardBody(content = content, isRefreshing = state.isRefreshing, onAction = onAction)
            else -> DashboardSkeleton()
        }
    }
}

@Composable
private fun DashboardBody(
    content: DashboardContentUi,
    isRefreshing: Boolean,
    onAction: (DashboardAction) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag(DashboardTestTags.CONTENT),
        contentPadding = PaddingValues(bottom = BottomNavClearance),
    ) {
        item(key = "header") {
            Header(header = content.header, content = content, isRefreshing = isRefreshing, onAction = onAction)
        }
        content.offlineSyncTime?.let { syncTime ->
            item(key = "offline") { OfflineBanner(syncTime, Modifier.padding(start = 20.dp, end = 20.dp, top = 14.dp)) }
        }
        item(key = "accounts") {
            AccountCarousel(content = content, onAction = onAction)
        }
        item(key = "quick-actions") {
            QuickActions(onAction = onAction, modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(24.dp))
        }
        content.safeToSpend?.let { safe ->
            item(key = "safe-to-spend") {
                SafeToSpendCard(safe, Modifier.padding(horizontal = 20.dp))
                Spacer(Modifier.height(14.dp))
            }
        }
        content.cashFlow?.let { flow ->
            item(key = "cash-flow") {
                CashFlowTiles(flow, onClick = { onAction(DashboardAction.AnalyticsClicked) }, modifier = Modifier.padding(horizontal = 20.dp))
                Spacer(Modifier.height(14.dp))
            }
        }
        content.pending?.let { pending ->
            item(key = "pending") {
                PendingBanner(pending, onClick = { onAction(DashboardAction.PendingClicked) }, modifier = Modifier.padding(horizontal = 20.dp))
            }
        }
        item(key = "upcoming") {
            Spacer(Modifier.height(8.dp))
            SectionTitle(
                title = stringResource(R.string.home_upcoming_title),
                action = stringResource(R.string.home_see_all),
                onAction = { onAction(DashboardAction.UpcomingPaymentsClicked) },
            )
            UpcomingPayments(content.upcomingPayments, onClick = { onAction(DashboardAction.UpcomingPaymentsClicked) })
            Spacer(Modifier.height(22.dp))
        }
        item(key = "categories") {
            SectionTitle(
                title = stringResource(R.string.home_categories_title),
                action = stringResource(R.string.home_details),
                onAction = { onAction(DashboardAction.AnalyticsClicked) },
            )
            Categories(content.categories, onClick = { onAction(DashboardAction.AnalyticsClicked) })
            Spacer(Modifier.height(22.dp))
        }
        content.card?.let { card ->
            item(key = "card") {
                CardSummary(card, onClick = { onAction(DashboardAction.CardClicked) }, modifier = Modifier.padding(horizontal = 20.dp))
                Spacer(Modifier.height(22.dp))
            }
        }
        item(key = "recent") {
            SectionTitle(
                title = stringResource(R.string.home_recent_title),
                action = stringResource(R.string.home_see_all),
                onAction = { onAction(DashboardAction.SeeAllTransactionsClicked) },
            )
            GroupCard(Modifier.padding(horizontal = 20.dp)) {
                if (content.recentTransactions.isEmpty()) {
                    EmptyLine(stringResource(R.string.home_recent_empty))
                }
                content.recentTransactions.forEachIndexed { index, transaction ->
                    if (index > 0) HorizontalDivider(color = Outline)
                    TransactionRow(
                        transaction = transaction,
                        onClick = { onAction(DashboardAction.TransactionSelected(transaction.transactionId)) },
                    )
                }
            }
        }
    }
}

// --- Header -------------------------------------------------------------------

@Composable
private fun Header(
    header: DashboardHeaderUi,
    content: DashboardContentUi,
    isRefreshing: Boolean,
    onAction: (DashboardAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Navy, RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 28.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Navy2)
                    .clickable(role = Role.Button, onClickLabel = stringResource(R.string.home_avatar_description)) {
                        onAction(DashboardAction.AvatarClicked)
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(header.avatarInitial, color = Blue, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                text = greetingText(header),
                color = OnNavy,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Navy2)
                    .clickable(role = Role.Button, onClick = { onAction(DashboardAction.NotificationsClicked) }),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.NotificationsNone,
                    contentDescription = stringResource(R.string.home_notifications_description),
                    tint = OnNavy,
                    modifier = Modifier.size(18.dp),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 7.dp, end = 8.dp)
                        .size(8.dp)
                        .background(Navy, CircleShape)
                        .padding(1.5.dp)
                        .background(Error, CircleShape),
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(header.formattedDate, color = OnNavy.copy(alpha = 0.45f), fontSize = 12.sp)
        Spacer(Modifier.height(18.dp))
        if (content.balance != null) {
            BalanceCard(balance = content.balance)
        } else {
            Text(stringResource(R.string.home_balance_unavailable), color = OnNavy.copy(alpha = 0.6f), fontSize = 13.sp)
        }
        if (isRefreshing) {
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(2.dp),
                color = Mint,
                trackColor = Navy2,
            )
        }
    }
}

@Composable
private fun greetingText(header: DashboardHeaderUi): String {
    val greeting = stringResource(
        when (header.period) {
            GreetingPeriodUi.MORNING -> R.string.home_greeting_morning
            GreetingPeriodUi.AFTERNOON -> R.string.home_greeting_afternoon
            GreetingPeriodUi.EVENING -> R.string.home_greeting_evening
        },
    )
    return header.firstName?.let { stringResource(R.string.home_greeting_with_name, greeting, it) } ?: greeting
}

// --- Accounts carousel ---------------------------------------------------------

@Composable
private fun AccountCarousel(content: DashboardContentUi, onAction: (DashboardAction) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 22.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        itemsIndexed(content.accounts, key = { _, item -> item.account.accountId }) { index, item ->
            AccountCard(
                account = item.account,
                balance = item.balance,
                highlighted = index % 2 == 1,
                onClick = { onAction(DashboardAction.AccountSelected(item.account.accountId)) },
            )
        }
        item(key = "connect") {
            Column(
                modifier = Modifier
                    .width(140.dp)
                    .height(128.dp)
                    .dashedBorder()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable(role = Role.Button) { onAction(DashboardAction.ConnectAnotherBankClicked) }
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier.size(32.dp).background(SurfaceVariant, CircleShape),
                    contentAlignment = Alignment.Center,
                ) { Text("+", color = Blue, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.home_connect_another_bank),
                    color = OnSurfaceVariant,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}

// --- Quick actions ----------------------------------------------------------------

@Composable
private fun QuickActions(onAction: (DashboardAction) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        QuickAction(Icons.AutoMirrored.Filled.ArrowForward, Blue, stringResource(R.string.home_action_send)) { onAction(DashboardAction.SendClicked) }
        QuickAction(Icons.AutoMirrored.Filled.ArrowBack, Mint, stringResource(R.string.home_action_request)) { onAction(DashboardAction.RequestClicked) }
        QuickAction(Icons.Outlined.BarChart, Blue, stringResource(R.string.home_action_analytics)) { onAction(DashboardAction.AnalyticsClicked) }
        QuickAction(Icons.Outlined.BookmarkBorder, Blue, stringResource(R.string.home_action_statements)) { onAction(DashboardAction.StatementsClicked) }
    }
}

@Composable
private fun QuickAction(icon: ImageVector, tint: Color, label: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(48.dp).background(SurfaceVariant, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp)) }
        Spacer(Modifier.height(7.dp))
        Text(label, color = OnSurface, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

// --- Safe to spend ------------------------------------------------------------------

@Composable
private fun SafeToSpendCard(safe: SafeToSpendUi, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Navy, RoundedCornerShape(20.dp))
            .padding(18.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(if (safe.daysToPayday != null) R.string.home_safe_to_spend_title else R.string.home_safe_to_spend_month_title),
                    color = OnNavy.copy(alpha = 0.55f),
                    fontSize = 11.5.sp,
                )
                Spacer(Modifier.height(4.dp))
                Text(safe.formattedAmount, color = OnNavy, fontSize = 26.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp)
            }
            safe.daysToPayday?.let { days ->
                Text(
                    text = if (days == 0) stringResource(R.string.home_payday_today) else pluralStringResource(R.plurals.home_days_left, days, days),
                    color = Mint,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(Mint.copy(alpha = 0.14f), RoundedCornerShape(100.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(OnNavy.copy(alpha = 0.12f)),
        ) {
            BarSegment(safe.billsFraction, Error)
            BarSegment(safe.spentFraction, Blue)
            BarSegment(safe.yoursFraction, Mint)
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Legend(Error, stringResource(R.string.home_bills_due, safe.formattedBillsDue))
            Legend(Blue, stringResource(R.string.home_spent, safe.formattedSpent))
            Legend(Mint, stringResource(R.string.home_yours, safe.formattedAmount))
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.BarSegment(fraction: Float, color: Color) {
    if (fraction > 0f) Box(Modifier.weight(fraction).fillMaxHeight().background(color))
}

@Composable
private fun Legend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(7.dp).background(color, RoundedCornerShape(2.dp)))
        Spacer(Modifier.width(6.dp))
        Text(label, color = OnNavy.copy(alpha = 0.75f), fontSize = 10.5.sp, maxLines = 1)
    }
}

// --- Money in / out -----------------------------------------------------------------

@Composable
private fun CashFlowTiles(flow: CashFlowUi, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatTile(
            label = stringResource(R.string.home_money_in),
            value = flow.formattedMoneyIn,
            valueColor = Success,
            caption = stringResource(R.string.home_so_far, flow.monthLabel),
            captionColor = OnSurfaceVariant,
            onClick = onClick,
            modifier = Modifier.weight(1f),
        )
        val change = flow.changePercent
        StatTile(
            label = stringResource(R.string.home_money_out),
            value = flow.formattedMoneyOut,
            valueColor = Error,
            caption = when {
                change == null -> stringResource(R.string.home_vs_month_none)
                flow.isSpendingUp -> stringResource(R.string.home_vs_month_up, change, flow.previousMonthLabel)
                else -> stringResource(R.string.home_vs_month_down, change, flow.previousMonthLabel)
            },
            captionColor = when {
                change == null -> OnSurfaceVariant
                flow.isSpendingUp -> Error
                else -> Success
            },
            onClick = onClick,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    valueColor: Color,
    caption: String,
    captionColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceVariant)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(14.dp),
    ) {
        Text(label, color = OnSurfaceVariant, fontSize = 10.5.sp)
        Spacer(Modifier.height(5.dp))
        Text(value, color = valueColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(3.dp))
        Text(caption, color = captionColor, fontSize = 10.5.sp)
    }
}

// --- Pending -----------------------------------------------------------------------

@Composable
private fun PendingBanner(pending: PendingUi, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Warning.copy(alpha = 0.1f))
            .border(1.dp, Warning.copy(alpha = 0.28f), RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(34.dp).background(Warning.copy(alpha = 0.18f), RoundedCornerShape(11.dp)),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Outlined.Schedule, contentDescription = null, tint = Warning, modifier = Modifier.size(17.dp)) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                pluralStringResource(R.plurals.home_pending_title, pending.count, pending.count),
                color = OnSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(stringResource(R.string.home_pending_subtitle, pending.formattedTotal), color = OnSurfaceVariant, fontSize = 11.sp)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF9AA3AF))
    }
}

// --- Upcoming payments ----------------------------------------------------------------

@Composable
private fun UpcomingPayments(payments: List<UpcomingPaymentUi>, onClick: () -> Unit) {
    GroupCard(Modifier.padding(horizontal = 20.dp)) {
        if (payments.isEmpty()) EmptyLine(stringResource(R.string.home_upcoming_empty))
        payments.forEachIndexed { index, payment ->
            if (index > 0) HorizontalDivider(color = Outline)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button, onClick = onClick)
                    .padding(horizontal = 16.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.size(36.dp).background(Color(payment.colorArgb), RoundedCornerShape(11.dp)),
                    contentAlignment = Alignment.Center,
                ) { Text(payment.initial, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(payment.name, color = OnSurface, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    val kind = stringResource(
                        if (payment.type == UpcomingPaymentTypeUi.DIRECT_DEBIT) R.string.home_direct_debit else R.string.home_standing_order,
                    )
                    Text("$kind · ${dueLabel(payment.daysUntil)}", color = OnSurfaceVariant, fontSize = 11.sp)
                }
                Text(payment.formattedAmount, color = OnSurface, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun dueLabel(daysUntil: Int): String = when (daysUntil) {
    0 -> stringResource(R.string.home_due_today)
    1 -> stringResource(R.string.home_due_tomorrow)
    else -> pluralStringResource(R.plurals.home_due_in_days, daysUntil, daysUntil)
}

// --- Categories --------------------------------------------------------------------

@Composable
private fun Categories(categories: List<CategorySpendUi>, onClick: () -> Unit) {
    if (categories.isEmpty()) {
        GroupCard(Modifier.padding(horizontal = 20.dp)) { EmptyLine(stringResource(R.string.home_categories_empty)) }
        return
    }
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(categories, key = { it.label }) { category ->
            Column(
                modifier = Modifier
                    .width(118.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceVariant)
                    .clickable(role = Role.Button, onClick = onClick)
                    .padding(14.dp),
            ) {
                val color = Color(category.colorArgb)
                Box(Modifier.size(8.dp).background(color, RoundedCornerShape(3.dp)))
                Spacer(Modifier.height(10.dp))
                Text(category.label, color = OnSurfaceVariant, fontSize = 11.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Text(category.formattedAmount, color = OnSurface, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                ProgressBar(category.fraction, color, height = 5)
            }
        }
    }
}

// --- Card summary ------------------------------------------------------------------

@Composable
private fun CardSummary(card: CardSummaryUi, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceVariant)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(width = 38.dp, height = 26.dp)
                    .background(Brush.linearGradient(listOf(Color(0xFF1B2A47), Color(0xFF0F1D38))), RoundedCornerShape(6.dp)),
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(card.displayName, color = OnSurface, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${card.maskedNumber} · ${card.network}", color = OnSurfaceVariant, fontSize = 11.sp, fontFamily = RobotoMono)
            }
            Column(horizontalAlignment = Alignment.End) {
                card.formattedCurrent?.let { Text(it, color = OnSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                card.formattedCreditLimit?.let { Text(stringResource(R.string.home_card_of_limit, it), color = OnSurfaceVariant, fontSize = 10.5.sp) }
            }
        }
        if (card.utilisationLabel != null) {
            Spacer(Modifier.height(12.dp))
            ProgressBar(card.utilisationFraction, Color(card.utilisationColorArgb), height = 6)
        }
        if (card.hasPaymentDue) {
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.home_card_payment_due, card.formattedPaymentDue!!, card.formattedPaymentDueDate!!),
                color = Error,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

// --- Shared bits ---------------------------------------------------------------------

@Composable
private fun SectionTitle(title: String, action: String, onAction: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, color = OnSurface, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text(
            action,
            color = Blue,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(role = Role.Button, onClick = onAction)
                .padding(8.dp),
        )
    }
}

@Composable
private fun GroupCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceVariant),
    ) { content() }
}

@Composable
private fun EmptyLine(text: String) {
    Text(text, color = OnSurfaceVariant, fontSize = 12.5.sp, modifier = Modifier.padding(16.dp))
}

@Composable
private fun ProgressBar(fraction: Float, color: Color, height: Int) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape((height / 2f).dp))
            .background(Outline),
    ) {
        if (fraction > 0f) {
            Box(Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)).fillMaxHeight().clip(RoundedCornerShape((height / 2f).dp)).background(color))
        }
    }
}

// --- Loading skeleton (DESIGN_SPEC §7: shaped like the content) ----------------------

@Composable
private fun DashboardSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag(DashboardTestTags.SKELETON),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Navy, RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 28.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(38.dp).shimmer(CircleShape, baseColor = Navy2, highlightColor = OnNavy.copy(alpha = 0.15f)))
                Spacer(Modifier.weight(1f))
                Box(Modifier.size(width = 160.dp, height = 16.dp).shimmer(RoundedCornerShape(6.dp), Navy2, OnNavy.copy(alpha = 0.15f)))
                Spacer(Modifier.weight(1f))
                Box(Modifier.size(38.dp).shimmer(CircleShape, Navy2, OnNavy.copy(alpha = 0.15f)))
            }
            Spacer(Modifier.height(30.dp))
            Box(Modifier.fillMaxWidth().height(160.dp).shimmer(RoundedCornerShape(22.dp), Navy2, OnNavy.copy(alpha = 0.15f)))
        }
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SkeletonBlock(Modifier.width(178.dp).height(128.dp), RoundedCornerShape(18.dp))
                SkeletonBlock(Modifier.width(178.dp).height(128.dp), RoundedCornerShape(18.dp))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                repeat(4) { SkeletonBlock(Modifier.size(48.dp), RoundedCornerShape(16.dp)) }
            }
            SkeletonBlock(Modifier.fillMaxWidth().height(130.dp), RoundedCornerShape(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SkeletonBlock(Modifier.weight(1f).height(80.dp), RoundedCornerShape(16.dp))
                SkeletonBlock(Modifier.weight(1f).height(80.dp), RoundedCornerShape(16.dp))
            }
            SkeletonBlock(Modifier.fillMaxWidth().height(190.dp), RoundedCornerShape(18.dp))
        }
    }
}

object DashboardTestTags {
    const val SKELETON = "dashboard_skeleton"
    const val CONTENT = "dashboard_content"
}

// --- Previews ---------------------------------------------------------------------

@Preview(widthDp = 412, heightDp = 1900)
@Composable
private fun DashboardContentPreview() {
    ClearSpendTheme(darkTheme = false) { DashboardContent(DashboardUiState(content = DashboardPreviewData.content), onAction = {}) }
}

@Preview(widthDp = 412, heightDp = 892)
@Composable
private fun DashboardLoadingPreview() {
    ClearSpendTheme(darkTheme = false) { DashboardContent(DashboardUiState(isLoading = true), onAction = {}) }
}

@Preview(widthDp = 412, heightDp = 892)
@Composable
private fun DashboardErrorPreview() {
    ClearSpendTheme(darkTheme = false) { DashboardContent(DashboardUiState(hasError = true), onAction = {}) }
}

@Preview(widthDp = 412, heightDp = 892)
@Composable
private fun DashboardEmptyPreview() {
    ClearSpendTheme(darkTheme = false) {
        DashboardContent(DashboardUiState(content = DashboardPreviewData.content.copy(accounts = emptyList())), onAction = {})
    }
}
