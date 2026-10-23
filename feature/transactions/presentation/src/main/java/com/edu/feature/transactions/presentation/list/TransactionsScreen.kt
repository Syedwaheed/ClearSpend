package com.edu.feature.transactions.presentation.list

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.edu.core.presentation.designsystem.Blue
import com.edu.core.presentation.designsystem.ClearSpendTheme
import com.edu.core.presentation.designsystem.Mint
import com.edu.core.presentation.designsystem.Navy
import com.edu.core.presentation.designsystem.OnNavy
import com.edu.core.presentation.designsystem.OnSurface
import com.edu.core.presentation.designsystem.OnSurfaceVariant
import com.edu.core.presentation.designsystem.Outline
import com.edu.core.presentation.designsystem.SuccessOnWhite
import com.edu.core.presentation.designsystem.Surface
import com.edu.core.presentation.designsystem.SurfaceVariant
import com.edu.core.presentation.designsystem.Warning
import com.edu.core.presentation.designsystem.WarningOnWhite
import com.edu.core.presentation.ui.EmptyStateScreen
import com.edu.core.presentation.ui.ErrorScreen
import com.edu.core.presentation.ui.ObserveAsEvent
import com.edu.core.presentation.ui.OfflineBanner
import com.edu.core.presentation.ui.SkeletonBlock
import com.edu.core.presentation.ui.TransactionRow
import com.edu.core.presentation.ui.dashedBorder
import com.edu.core.presentation.ui.model.TransactionUi
import com.edu.feature.transactions.presentation.R
import com.edu.feature.transactions.presentation.model.AccountOptionUi
import com.edu.feature.transactions.presentation.model.CategoryChipUi
import com.edu.feature.transactions.presentation.model.PeriodChipUi
import com.edu.feature.transactions.presentation.model.TransactionDayGroupUi
import com.edu.feature.transactions.presentation.model.TransactionsContentUi
import com.edu.feature.transactions.presentation.model.TransactionsSummaryUi
import org.koin.compose.viewmodel.koinViewModel

/** Room for the floating bottom nav (≈80dp) plus the raised Send FAB. */
private val BottomNavClearance = 112.dp
private val PendingBorder = Color(0x66D98200)
private val PendingDivider = Color(0x26D98200)
private val PendingBackground = Color(0x0DFFB020)
private val SpendHighlight = Color(0xFFFF8080)
private val ScreenGutter = 20.dp

/** Lets a horizontally scrolling chip row run under the screen gutters, edge to edge (screen 12). */
private fun Modifier.bleedHorizontally(bleed: Dp): Modifier = layout { measurable, constraints ->
    val extra = bleed.roundToPx() * 2
    val placeable = measurable.measure(
        constraints.copy(minWidth = constraints.minWidth + extra, maxWidth = constraints.maxWidth + extra),
    )
    layout(placeable.width - extra, placeable.height) { placeable.place(-extra / 2, 0) }
}

@Composable
fun TransactionsScreen(
    onNavigateToTransaction: (transactionId: String) -> Unit,
    onNavigateToConnectBank: () -> Unit,
    viewModel: TransactionsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ObserveAsEvent(viewModel.effect) { effect ->
        when (effect) {
            is TransactionsEffect.NavigateToTransaction -> onNavigateToTransaction(effect.transactionId)
            TransactionsEffect.NavigateToConnectBank -> onNavigateToConnectBank()
        }
    }
    TransactionsContent(state = state, onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsContent(
    state: TransactionsUiState,
    onAction: (TransactionsAction) -> Unit,
) {
    // Local text so typing never waits on the intent queue; the ViewModel gets every change.
    var searchText by rememberSaveable { mutableStateOf(state.query) }
    val content = state.content

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .statusBarsPadding()
            .testTag(TransactionsTestTags.LIST),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = BottomNavClearance),
    ) {
        item(key = "header") {
            Header(state = state, onAction = onAction)
        }
        content?.offlineSyncTime?.let { syncTime ->
            item(key = "offline") { OfflineBanner(syncTime, Modifier.padding(bottom = 12.dp)) }
        }
        item(key = "summary") {
            if (content != null) {
                SummaryCard(content.summary)
            } else {
                SkeletonBlock(Modifier.fillMaxWidth().height(168.dp), RoundedCornerShape(22.dp))
            }
            Spacer(Modifier.height(14.dp))
        }
        item(key = "search") {
            SearchField(
                text = searchText,
                onTextChange = {
                    searchText = it
                    onAction(TransactionsAction.QueryChanged(it))
                },
                onClear = {
                    searchText = ""
                    onAction(TransactionsAction.QueryChanged(""))
                },
            )
            Spacer(Modifier.height(12.dp))
        }
        item(key = "periods") {
            PeriodChips(selected = state.period.chip, onSelect = { onAction(TransactionsAction.PeriodSelected(it)) })
            Spacer(Modifier.height(8.dp))
        }
        item(key = "categories") {
            CategoryChips(selected = state.category, onSelect = { onAction(TransactionsAction.CategorySelected(it)) })
            Spacer(Modifier.height(20.dp))
        }

        when {
            state.hasError -> item(key = "error") {
                ErrorScreen(onRetry = { onAction(TransactionsAction.RetryClicked) }, modifier = Modifier.padding(top = 24.dp))
            }
            content == null -> item(key = "skeleton") { ListSkeleton() }
            content.isEmpty && !state.hasMore && !state.isLoadingMore -> item(key = "empty") {
                EmptyState(
                    filtered = state.hasActiveFilters,
                    onClearFilters = {
                        searchText = ""
                        onAction(TransactionsAction.ClearFiltersClicked)
                    },
                    onRefresh = { onAction(TransactionsAction.RetryClicked) },
                )
            }
            else -> {
                if (content.pending.isNotEmpty()) {
                    item(key = "pending") {
                        PendingSection(content, onClick = { onAction(TransactionsAction.TransactionSelected(it)) })
                    }
                }
                content.groups.forEach { group ->
                    item(key = "group-${group.key}") {
                        DayGroup(group, onClick = { onAction(TransactionsAction.TransactionSelected(it)) })
                    }
                }
                if (state.hasMore || state.isLoadingMore || state.loadMoreFailed) {
                    item(key = "more") {
                        LoadMoreFooter(state = state, onAction = onAction)
                    }
                }
            }
        }
    }

    if (state.showDateRangePicker) {
        val pickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = state.period.customStartUtcMillis,
            initialSelectedEndDateMillis = state.period.customEndUtcMillis,
        )
        DatePickerDialog(
            onDismissRequest = { onAction(TransactionsAction.DateRangePickerDismissed) },
            confirmButton = {
                TextButton(
                    enabled = pickerState.selectedStartDateMillis != null,
                    onClick = {
                        val start = pickerState.selectedStartDateMillis ?: return@TextButton
                        onAction(TransactionsAction.CustomRangeSelected(start, pickerState.selectedEndDateMillis))
                    },
                ) { Text(stringResource(R.string.transactions_date_range_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { onAction(TransactionsAction.DateRangePickerDismissed) }) {
                    Text(stringResource(R.string.transactions_date_range_cancel))
                }
            },
        ) {
            DateRangePicker(state = pickerState, modifier = Modifier.weight(1f))
        }
    }
}

// --- Header + account switcher ---------------------------------------------

@Composable
private fun Header(state: TransactionsUiState, onAction: (TransactionsAction) -> Unit) {
    var menuOpen by rememberSaveable { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(R.string.transactions_title),
            color = OnSurface,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        Box {
            val selected = state.selectedAccount
            Row(
                modifier = Modifier
                    .widthIn(max = 190.dp)
                    .clip(CircleShape)
                    .background(SurfaceVariant)
                    .clickable(role = Role.DropdownList, onClickLabel = stringResource(R.string.transactions_switch_account)) {
                        menuOpen = true
                    }
                    .padding(start = 7.dp, end = 12.dp, top = 7.dp, bottom = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AccountBadge(
                    initial = selected?.initial ?: stringResource(R.string.transactions_all_accounts_symbol),
                    color = selected?.let { Color(it.colorArgb) } ?: Navy,
                )
                Spacer(Modifier.width(7.dp))
                Text(
                    selected?.displayName ?: stringResource(R.string.transactions_all_accounts),
                    color = OnSurface,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(14.dp))
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.transactions_all_accounts), fontWeight = FontWeight.SemiBold) },
                    leadingIcon = { AccountBadge(stringResource(R.string.transactions_all_accounts_symbol), Navy) },
                    onClick = {
                        menuOpen = false
                        onAction(TransactionsAction.AccountSelected(null))
                    },
                )
                state.accounts.forEach { account ->
                    AccountMenuItem(account) {
                        menuOpen = false
                        onAction(TransactionsAction.AccountSelected(account.accountId))
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountMenuItem(account: AccountOptionUi, onClick: () -> Unit) {
    DropdownMenuItem(
        text = {
            Column {
                Text(account.displayName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = OnSurface)
                Text(account.providerName, fontSize = 11.5.sp, color = OnSurfaceVariant)
            }
        },
        leadingIcon = { AccountBadge(account.initial, Color(account.colorArgb)) },
        onClick = onClick,
    )
}

@Composable
private fun AccountBadge(initial: String, color: Color) {
    Box(
        modifier = Modifier.size(22.dp).background(color, RoundedCornerShape(7.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(initial, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

// --- Summary card -------------------------------------------------------------

@Composable
private fun SummaryCard(summary: TransactionsSummaryUi) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Navy)
            .padding(18.dp)
            .testTag(TransactionsTestTags.SUMMARY),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (summary.isAllTime) stringResource(R.string.transactions_all_time) else summary.periodLabel,
                color = OnNavy.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            Text(
                pluralStringResource(R.plurals.transactions_count, summary.transactionCount, summary.transactionCount),
                color = OnNavy.copy(alpha = 0.6f),
                fontSize = 11.sp,
            )
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SummaryStat(stringResource(R.string.transactions_money_in), summary.formattedMoneyIn, Mint, Modifier.weight(1f))
            SummaryStat(stringResource(R.string.transactions_money_out), summary.formattedMoneyOut, SpendHighlight, Modifier.weight(1f))
            SummaryStat(stringResource(R.string.transactions_net), summary.formattedNet, OnNavy, Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        DailyBars(summary)
        Spacer(Modifier.height(6.dp))
        Text(stringResource(R.string.transactions_daily_spending), color = OnNavy.copy(alpha = 0.5f), fontSize = 10.sp)
    }
}

@Composable
private fun SummaryStat(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, color = OnNavy.copy(alpha = 0.6f), fontSize = 10.sp, maxLines = 1)
        Spacer(Modifier.height(3.dp))
        Text(value, color = color, fontSize = 19.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** Σ DEBIT per day, oldest first; bars of 3dp minimum, newest bar highlighted (Canvas). */
@Composable
private fun DailyBars(summary: TransactionsSummaryUi) {
    Canvas(Modifier.fillMaxWidth().height(34.dp)) {
        val bars = summary.dailyBars
        if (bars.isEmpty()) return@Canvas
        val gap = 4.dp.toPx()
        val width = (size.width - gap * (bars.size - 1)) / bars.size
        val minHeight = 3.dp.toPx()
        val radius = CornerRadius(3.dp.toPx())
        bars.forEachIndexed { index, bar ->
            val height = maxOf(minHeight, size.height * bar.heightFraction)
            drawRoundRect(
                color = if (bar.isLatest) SpendHighlight else OnNavy.copy(alpha = 0.25f),
                topLeft = Offset(index * (width + gap), size.height - height),
                size = Size(width, height),
                cornerRadius = radius,
            )
        }
    }
}

// --- Search + chips ----------------------------------------------------------

@Composable
private fun SearchField(text: String, onTextChange: (String) -> Unit, onClear: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceVariant)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.Search, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f)) {
            if (text.isEmpty()) {
                Text(stringResource(R.string.transactions_search_hint), color = OnSurfaceVariant, fontSize = 14.sp, maxLines = 1)
            }
            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                singleLine = true,
                textStyle = TextStyle(color = OnSurface, fontSize = 14.sp),
                cursorBrush = SolidColor(Blue),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier.fillMaxWidth().testTag(TransactionsTestTags.SEARCH),
            )
        }
        if (text.isNotEmpty()) {
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFC9CED6))
                    .clickable(role = Role.Button, onClickLabel = stringResource(R.string.transactions_clear_search), onClick = onClear),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.transactions_clear_search), tint = Color.White, modifier = Modifier.size(12.dp))
            }
        }
    }
}

@Composable
private fun PeriodChips(selected: PeriodChipUi, onSelect: (PeriodChipUi) -> Unit) {
    Row(Modifier.bleedHorizontally(ScreenGutter).horizontalScroll(rememberScrollState()).padding(horizontal = ScreenGutter), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PeriodChipUi.entries.forEach { chip ->
            val isSelected = chip == selected
            Text(
                text = stringResource(chip.labelRes()),
                color = if (isSelected) Color.White else OnSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) Navy else Color.Transparent)
                    .border(1.dp, if (isSelected) Navy else Outline, CircleShape)
                    .clickable(role = Role.Tab) { onSelect(chip) }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
            )
        }
    }
}

@Composable
private fun CategoryChips(selected: CategoryChipUi, onSelect: (CategoryChipUi) -> Unit) {
    Row(Modifier.bleedHorizontally(ScreenGutter).horizontalScroll(rememberScrollState()).padding(horizontal = ScreenGutter), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CategoryChipUi.entries.forEach { chip ->
            val isSelected = chip == selected
            Text(
                text = stringResource(chip.labelRes()),
                color = if (isSelected) Blue else OnSurface,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) Blue.copy(alpha = 0.1f) else Surface)
                    .border(1.dp, if (isSelected) Blue else Outline, CircleShape)
                    .clickable(role = Role.Tab) { onSelect(chip) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}

private fun PeriodChipUi.labelRes(): Int = when (this) {
    PeriodChipUi.ALL -> R.string.transactions_period_all
    PeriodChipUi.THIS_MONTH -> R.string.transactions_period_this_month
    PeriodChipUi.LAST_MONTH -> R.string.transactions_period_last_month
    PeriodChipUi.CUSTOM_RANGE -> R.string.transactions_period_custom
}

private fun CategoryChipUi.labelRes(): Int = when (this) {
    CategoryChipUi.ALL -> R.string.transactions_category_all
    CategoryChipUi.PURCHASE -> R.string.transactions_category_purchase
    CategoryChipUi.DIRECT_DEBIT -> R.string.transactions_category_direct_debit
    CategoryChipUi.BILL_PAYMENT -> R.string.transactions_category_bill_payment
    CategoryChipUi.TRANSFER -> R.string.transactions_category_transfer
    CategoryChipUi.CASH -> R.string.transactions_category_cash
    CategoryChipUi.CREDIT -> R.string.transactions_category_credit
}

// --- Pending + day groups ----------------------------------------------------

@Composable
private fun PendingSection(content: TransactionsContentUi, onClick: (String) -> Unit) {
    Column(Modifier.padding(bottom = 18.dp).testTag(TransactionsTestTags.PENDING)) {
        Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(7.dp).background(Warning, CircleShape))
            Spacer(Modifier.width(7.dp))
            Text(
                stringResource(R.string.transactions_pending),
                color = OnSurface,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                modifier = Modifier.weight(1f),
            )
            Text(content.formattedPendingTotal, color = OnSurfaceVariant, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
        }
        Column(
            Modifier
                .fillMaxWidth()
                .dashedBorder(color = PendingBorder, cornerRadius = 18.dp, strokeWidth = 1.5.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(PendingBackground),
        ) {
            content.pending.forEachIndexed { index, transaction ->
                if (index > 0) HorizontalDivider(color = PendingDivider)
                TransactionRow(
                    transaction = transaction,
                    subtitle = transaction.subtitle,
                    iconSize = 40.dp,
                    onClick = { onClick(transaction.transactionId) },
                    amountColor = OnSurface,
                    trailingCaption = stringResource(R.string.transactions_not_settled),
                    trailingCaptionColor = WarningOnWhite,
                    trailingCaptionBold = true,
                )
            }
        }
    }
}

@Composable
private fun DayGroup(group: TransactionDayGroupUi, onClick: (String) -> Unit) {
    Column(Modifier.padding(bottom = 18.dp)) {
        Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                group.label,
                color = OnSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                modifier = Modifier.weight(1f),
            )
            Text(
                group.formattedNet,
                color = if (group.isNetPositive) SuccessOnWhite else OnSurfaceVariant,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(SurfaceVariant)) {
            group.transactions.forEachIndexed { index, transaction ->
                if (index > 0) HorizontalDivider(color = Outline)
                TransactionRow(
                    transaction = transaction,
                    subtitle = transaction.subtitle,
                    iconSize = 40.dp,
                    showDirectionBadge = true,
                    trailingCaption = transaction.formattedRunningBalance,
                    onClick = { onClick(transaction.transactionId) },
                )
            }
        }
    }
}

@Composable
private fun LoadMoreFooter(state: TransactionsUiState, onAction: (TransactionsAction) -> Unit) {
    // Composed only when it scrolls into view: that is the "extend on scroll" trigger.
    LaunchedEffect(state.oldestLoadedEpochDay, state.loadMoreFailed, state.isLoadingMore) {
        if (state.hasMore && !state.isLoadingMore && !state.loadMoreFailed) onAction(TransactionsAction.LoadMore)
    }
    Box(Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
        if (state.loadMoreFailed) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.transactions_load_more_failed), color = OnSurfaceVariant, fontSize = 12.5.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.transactions_try_again),
                    color = Blue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(CircleShape)
                        .border(1.dp, Outline, CircleShape)
                        .clickable(role = Role.Button) { onAction(TransactionsAction.LoadMore) }
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                )
            }
        } else {
            CircularProgressIndicator(color = Blue, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
        }
    }
}

// --- Empty / loading ---------------------------------------------------------

@Composable
private fun EmptyState(filtered: Boolean, onClearFilters: () -> Unit, onRefresh: () -> Unit) {
    EmptyStateScreen(
        title = stringResource(R.string.transactions_empty_title),
        subtitle = stringResource(if (filtered) R.string.transactions_empty_filtered_hint else R.string.transactions_empty_hint),
        icon = Icons.Outlined.Search,
        ctaText = stringResource(if (filtered) R.string.transactions_clear_filters else R.string.transactions_refresh),
        onCtaClick = if (filtered) onClearFilters else onRefresh,
        modifier = Modifier.padding(top = 40.dp).testTag(TransactionsTestTags.EMPTY),
    )
}

@Composable
private fun ListSkeleton() {
    Column(Modifier.testTag(TransactionsTestTags.SKELETON), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(3) {
            SkeletonBlock(Modifier.width(160.dp).height(14.dp), RoundedCornerShape(6.dp))
            SkeletonBlock(Modifier.fillMaxWidth().height(132.dp), RoundedCornerShape(18.dp))
            Spacer(Modifier.height(8.dp))
        }
    }
}

object TransactionsTestTags {
    const val LIST = "transactions_list"
    const val SUMMARY = "transactions_summary"
    const val SEARCH = "transactions_search"
    const val PENDING = "transactions_pending"
    const val EMPTY = "transactions_empty"
    const val SKELETON = "transactions_skeleton"
}

// --- Previews (preview-only sample data, never shipped to screens) ------------

private fun previewRow(id: String, title: String, amount: String, debit: Boolean, balance: String?, pending: Boolean = false) = TransactionUi(
    transactionId = id,
    title = title,
    subtitle = if (pending) "Coffee · Card" else "Groceries · 06:00 · Card",
    formattedAmount = amount,
    amountColorArgb = if (debit) 0xFFE03B3B.toInt() else 0xFF00A043.toInt(),
    isDebit = debit,
    formattedDate = "",
    categoryIconId = "",
    categoryColorArgb = 0xFF2D6EF5.toInt(),
    formattedRunningBalance = balance,
    isPending = pending,
    initial = title.take(1),
)

private val previewContent = TransactionsContentUi(
    summary = TransactionsSummaryUi(
        isAllTime = true,
        periodLabel = "",
        transactionCount = 3,
        formattedMoneyIn = "£20.00",
        formattedMoneyOut = "£15.00",
        formattedNet = "+£5.00",
        dailyBars = listOf(0.1f, 0.4f, 1f).mapIndexed { i, f -> com.edu.feature.transactions.presentation.model.DailyBarUi(f, i == 2) },
    ),
    pending = listOf(previewRow("p", "Coffee shop", "-£3.00", true, null, pending = true)),
    formattedPendingTotal = "-£3.00",
    groups = listOf(
        TransactionDayGroupUi(
            key = "d1",
            label = "TODAY — 1 JANUARY 2026",
            formattedNet = "-£15.00",
            isNetPositive = false,
            transactions = listOf(previewRow("a", "Grocer", "-£15.00", true, "£100.00")),
        ),
        TransactionDayGroupUi(
            key = "d2",
            label = "WEDNESDAY — 31 DECEMBER 2025",
            formattedNet = "+£20.00",
            isNetPositive = true,
            transactions = listOf(previewRow("b", "Refund", "+£20.00", false, "£115.00")),
        ),
    ),
    offlineSyncTime = null,
)

@Preview(widthDp = 412, heightDp = 1400)
@Composable
private fun TransactionsPreview() {
    ClearSpendTheme(darkTheme = false) { TransactionsContent(TransactionsUiState(content = previewContent), onAction = {}) }
}

@Preview(widthDp = 412, heightDp = 892)
@Composable
private fun TransactionsEmptyPreview() {
    val empty = previewContent.copy(pending = emptyList(), groups = emptyList())
    ClearSpendTheme(darkTheme = false) {
        TransactionsContent(TransactionsUiState(content = empty, query = "zzzz"), onAction = {})
    }
}

@Preview(widthDp = 412, heightDp = 892)
@Composable
private fun TransactionsLoadingPreview() {
    ClearSpendTheme(darkTheme = false) { TransactionsContent(TransactionsUiState(isLoading = true), onAction = {}) }
}
