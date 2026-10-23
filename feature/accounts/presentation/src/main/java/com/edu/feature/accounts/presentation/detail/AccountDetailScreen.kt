package com.edu.feature.accounts.presentation.detail

import android.content.ClipData
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.edu.core.presentation.designsystem.Blue
import com.edu.core.presentation.designsystem.BlueDark
import com.edu.core.presentation.designsystem.ClearSpendTheme
import com.edu.core.presentation.designsystem.OnSurface
import com.edu.core.presentation.designsystem.OnSurfaceVariant
import com.edu.core.presentation.designsystem.Outline
import com.edu.core.presentation.designsystem.RobotoMono
import com.edu.core.presentation.designsystem.Surface
import com.edu.core.presentation.designsystem.SurfaceVariant
import com.edu.core.presentation.ui.ErrorScreen
import com.edu.core.presentation.ui.ObserveAsEvent
import com.edu.core.presentation.ui.OfflineBanner
import com.edu.core.presentation.ui.SkeletonBlock
import com.edu.core.presentation.ui.TransactionRow
import com.edu.core.presentation.ui.model.TransactionUi
import com.edu.feature.accounts.presentation.R
import com.edu.feature.accounts.presentation.components.BackHeader
import com.edu.feature.accounts.presentation.model.AccountDetailUi
import com.edu.feature.accounts.presentation.model.CopiedFieldUi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** How long "Copied!" stays on a row (DESIGN_SPEC §4 · 11). */
private const val COPIED_FEEDBACK_MILLIS = 1_200L

@Composable
fun AccountDetailScreen(
    accountId: String,
    onNavigateBack: () -> Unit,
    onNavigateToTransaction: (transactionId: String) -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToConnectBank: () -> Unit,
    viewModel: AccountDetailViewModel = koinViewModel(key = "account-detail-$accountId") { parametersOf(accountId) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    ObserveAsEvent(viewModel.effect) { effect ->
        when (effect) {
            AccountDetailEffect.NavigateBack -> onNavigateBack()
            is AccountDetailEffect.CopyToClipboard -> scope.launch {
                clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(effect.label, effect.value)))
            }
            is AccountDetailEffect.NavigateToTransaction -> onNavigateToTransaction(effect.transactionId)
            AccountDetailEffect.NavigateToTransactions -> onNavigateToTransactions()
            AccountDetailEffect.NavigateToConnectBank -> onNavigateToConnectBank()
        }
    }
    // The ViewModel owns which row shows "Copied!"; the Screen only owns the 1.2 s timer.
    LaunchedEffect(state.copiedField) {
        val field = state.copiedField ?: return@LaunchedEffect
        delay(COPIED_FEEDBACK_MILLIS)
        viewModel.onAction(AccountDetailAction.CopiedFeedbackElapsed(field))
    }
    AccountDetailContent(state = state, onAction = viewModel::onAction)
}

@Composable
fun AccountDetailContent(
    state: AccountDetailUiState,
    onAction: (AccountDetailAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .statusBarsPadding(),
    ) {
        BackHeader(
            title = state.detail?.title ?: stringResource(R.string.detail_title_fallback),
            onBack = { onAction(AccountDetailAction.BackClicked) },
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 20.dp),
        )
        val detail = state.detail
        when {
            state.isLoading && detail == null -> DetailSkeleton()
            state.hasError -> ErrorScreen(onRetry = { onAction(AccountDetailAction.RetryClicked) })
            detail != null -> DetailBody(detail, state.copiedField, onAction)
            else -> DetailSkeleton()
        }
    }
}

@Composable
private fun DetailBody(detail: AccountDetailUi, copiedField: CopiedFieldUi?, onAction: (AccountDetailAction) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, bottom = 24.dp)
            .navigationBarsPadding()
            .testTag(AccountDetailTestTags.CONTENT),
    ) {
        detail.offlineSyncTime?.let { OfflineBanner(it, Modifier.padding(bottom = 12.dp)) }
        BalanceHero(detail)
        Spacer(Modifier.height(18.dp))

        SectionLabel(stringResource(R.string.detail_account_details))
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(SurfaceVariant)) {
            var first = true
            fun divider() = !first.also { first = false }
            detail.sortCode?.let { sortCode ->
                if (divider()) HorizontalDivider(color = Outline)
                DetailRow(
                    label = stringResource(R.string.detail_sort_code),
                    value = sortCode,
                    copied = copiedField == CopiedFieldUi.SORT_CODE,
                    onCopy = { onAction(AccountDetailAction.CopySortCodeClicked) },
                )
            }
            detail.accountNumber?.let { number ->
                if (divider()) HorizontalDivider(color = Outline)
                DetailRow(
                    label = stringResource(R.string.detail_account_number),
                    value = number,
                    copied = copiedField == CopiedFieldUi.ACCOUNT_NUMBER,
                    onCopy = { onAction(AccountDetailAction.CopyAccountNumberClicked) },
                )
            }
            detail.iban?.let { iban ->
                if (divider()) HorizontalDivider(color = Outline)
                DetailRow(label = stringResource(R.string.detail_iban), value = iban, valueSize = 12)
            }
            if (divider()) HorizontalDivider(color = Outline)
            DetailRow(label = stringResource(R.string.detail_currency), value = detail.currency, monospace = false)
        }
        Spacer(Modifier.height(20.dp))

        SectionLabel(stringResource(R.string.detail_recent_activity))
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(SurfaceVariant)) {
            if (detail.recentTransactions.isEmpty()) {
                Text(stringResource(R.string.detail_no_activity), color = OnSurfaceVariant, fontSize = 12.5.sp, modifier = Modifier.padding(16.dp))
            }
            detail.recentTransactions.forEachIndexed { index, transaction ->
                if (index > 0) HorizontalDivider(color = Outline)
                TransactionRow(
                    transaction = transaction,
                    subtitle = transaction.relativeDay,
                    iconSize = 34.dp,
                    onClick = { onAction(AccountDetailAction.TransactionSelected(transaction.transactionId)) },
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(R.string.detail_view_all),
            color = Blue,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Outline, RoundedCornerShape(16.dp))
                .clickable(role = Role.Button) { onAction(AccountDetailAction.ViewAllTransactionsClicked) }
                .padding(15.dp),
        )
    }
}

@Composable
private fun BalanceHero(detail: AccountDetailUi) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.linearGradient(listOf(Blue, BlueDark)))
            .padding(22.dp),
    ) {
        Text(stringResource(R.string.detail_available_balance), color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
        Spacer(Modifier.height(6.dp))
        Text(detail.formattedAvailable ?: "—", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.25f))
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            HeroStat(stringResource(R.string.detail_current), detail.formattedCurrent ?: "—")
            Spacer(Modifier.width(16.dp))
            Box(Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.25f)))
            Spacer(Modifier.width(16.dp))
            HeroStat(stringResource(R.string.detail_overdraft), detail.formattedOverdraft ?: "—")
        }
    }
}

@Composable
private fun HeroStat(label: String, value: String) {
    Column {
        Text(label, color = Color.White.copy(alpha = 0.65f), fontSize = 10.5.sp)
        Spacer(Modifier.height(2.dp))
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        color = OnSurfaceVariant,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(bottom = 10.dp),
    )
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    copied: Boolean = false,
    onCopy: (() -> Unit)? = null,
    monospace: Boolean = true,
    valueSize: Int = 13,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = OnSurfaceVariant, fontSize = 13.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                value,
                color = OnSurface,
                fontSize = valueSize.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = if (monospace) RobotoMono else null,
            )
            if (onCopy != null) {
                Spacer(Modifier.width(10.dp))
                Text(
                    stringResource(if (copied) R.string.detail_copied else R.string.detail_copy),
                    color = Blue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(role = Role.Button, onClick = onCopy)
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun DetailSkeleton() {
    Column(
        Modifier.padding(horizontal = 20.dp).testTag(AccountDetailTestTags.SKELETON),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SkeletonBlock(Modifier.fillMaxWidth().height(156.dp), RoundedCornerShape(22.dp))
        SkeletonBlock(Modifier.fillMaxWidth().height(200.dp), RoundedCornerShape(18.dp))
        SkeletonBlock(Modifier.fillMaxWidth().height(180.dp), RoundedCornerShape(18.dp))
    }
}

object AccountDetailTestTags {
    const val SKELETON = "account_detail_skeleton"
    const val CONTENT = "account_detail_content"
}

private val previewDetail = AccountDetailUi(
    accountId = "a",
    title = "Current account",
    formattedAvailable = "£100.00",
    formattedCurrent = "£90.00",
    formattedOverdraft = "£50.00",
    sortCode = "11-22-33",
    accountNumber = "12345678",
    iban = "GB00TEST11223312345678",
    currency = "GBP",
    recentTransactions = listOf(
        TransactionUi(
            "t", "Coffee shop", "", "-£3.20", 0xFFE03B3B.toInt(), true, "", "", 0xFF2D6EF5.toInt(), null, false,
            initial = "C", relativeDay = "Today",
        ),
    ),
)

@Preview(widthDp = 412, heightDp = 892)
@Composable
private fun AccountDetailPreview() {
    ClearSpendTheme(darkTheme = false) {
        AccountDetailContent(AccountDetailUiState(detail = previewDetail, copiedField = CopiedFieldUi.SORT_CODE), onAction = {})
    }
}

@Preview(widthDp = 412, heightDp = 892)
@Composable
private fun AccountDetailLoadingPreview() {
    ClearSpendTheme(darkTheme = false) { AccountDetailContent(AccountDetailUiState(isLoading = true), onAction = {}) }
}
