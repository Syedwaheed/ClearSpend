package com.edu.feature.transactions.presentation.detail

import android.content.ClipData
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.edu.core.presentation.designsystem.Blue
import com.edu.core.presentation.designsystem.ClearSpendTheme
import com.edu.core.presentation.designsystem.ErrorOnWhite
import com.edu.core.presentation.designsystem.Mint
import com.edu.core.presentation.designsystem.Navy
import com.edu.core.presentation.designsystem.Navy2
import com.edu.core.presentation.designsystem.OnNavy
import com.edu.core.presentation.designsystem.OnSurface
import com.edu.core.presentation.designsystem.OnSurfaceVariant
import com.edu.core.presentation.designsystem.Outline
import com.edu.core.presentation.designsystem.RobotoMono
import com.edu.core.presentation.designsystem.Surface
import com.edu.core.presentation.designsystem.SurfaceVariant
import com.edu.core.presentation.designsystem.Warning
import com.edu.core.presentation.ui.ErrorScreen
import com.edu.core.presentation.ui.ObserveAsEvent
import com.edu.core.presentation.ui.OfflineBanner
import com.edu.core.presentation.ui.SkeletonBlock
import com.edu.core.presentation.ui.shimmer
import com.edu.feature.transactions.presentation.R
import com.edu.feature.transactions.presentation.model.MerchantInsightUi
import com.edu.feature.transactions.presentation.model.TransactionDetailUi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** How long "Copied!" stays next to the transaction ID (same as Account Detail). */
private const val COPIED_FEEDBACK_MILLIS = 1_200L

@Composable
fun TransactionDetailScreen(
    transactionId: String,
    onNavigateBack: () -> Unit,
    onNavigateToBills: () -> Unit,
    onNavigateToConnectBank: () -> Unit,
    viewModel: TransactionDetailViewModel = koinViewModel(key = "transaction-detail-$transactionId") { parametersOf(transactionId) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val reportUnavailable = stringResource(R.string.detail_report_issue_unavailable)
    ObserveAsEvent(viewModel.effect) { effect ->
        when (effect) {
            TransactionDetailEffect.NavigateBack -> onNavigateBack()
            is TransactionDetailEffect.CopyToClipboard -> scope.launch {
                clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(effect.label, effect.value)))
            }
            TransactionDetailEffect.NavigateToBills -> onNavigateToBills()
            TransactionDetailEffect.NavigateToConnectBank -> onNavigateToConnectBank()
            TransactionDetailEffect.ShowReportIssueUnavailable ->
                Toast.makeText(context, reportUnavailable, Toast.LENGTH_LONG).show()
        }
    }
    LaunchedEffect(state.isIdCopied) {
        if (!state.isIdCopied) return@LaunchedEffect
        delay(COPIED_FEEDBACK_MILLIS)
        viewModel.onAction(TransactionDetailAction.CopiedFeedbackElapsed)
    }
    TransactionDetailContent(state = state, onAction = viewModel::onAction)
}

@Composable
fun TransactionDetailContent(
    state: TransactionDetailUiState,
    onAction: (TransactionDetailAction) -> Unit,
) {
    val detail = state.detail
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .verticalScroll(rememberScrollState()),
    ) {
        Hero(detail = detail, onBack = { onAction(TransactionDetailAction.BackClicked) })
        when {
            state.hasError -> ErrorScreen(
                onRetry = { onAction(TransactionDetailAction.RetryClicked) },
                modifier = Modifier.padding(top = 32.dp),
            )
            detail == null -> BodySkeleton()
            else -> Body(detail, state.isIdCopied, onAction)
        }
    }
}

@Composable
private fun Hero(detail: TransactionDetailUi?, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(Navy)
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 26.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(bottom = 22.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Navy2)
                    .clickable(role = Role.Button, onClickLabel = stringResource(R.string.detail_back), onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = stringResource(R.string.detail_back), tint = OnNavy)
            }
            Spacer(Modifier.weight(1f))
            if (detail != null) StatusBadge(detail.isPending)
        }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            if (detail == null) {
                val navyShimmer = Modifier.shimmer(RoundedCornerShape(12.dp), baseColor = Navy2, highlightColor = Color(0xFF1C2E52))
                Box(Modifier.size(60.dp).then(navyShimmer))
                Spacer(Modifier.height(14.dp))
                Box(Modifier.width(120.dp).height(18.dp).then(navyShimmer))
                Spacer(Modifier.height(10.dp))
                Box(Modifier.width(170.dp).height(36.dp).then(navyShimmer))
                Spacer(Modifier.height(10.dp))
                Box(Modifier.width(110.dp).height(12.dp).then(navyShimmer))
            } else {
                Box(
                    modifier = Modifier.size(60.dp).background(Color(detail.categoryColorArgb), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(detail.initial, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    detail.title,
                    color = OnNavy,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    detail.formattedAmount,
                    color = if (detail.isCredit) Mint else OnNavy,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-1).sp,
                    modifier = Modifier.testTag(TransactionDetailTestTags.AMOUNT),
                )
                Spacer(Modifier.height(6.dp))
                Text(detail.dateTime, color = OnNavy.copy(alpha = 0.65f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun StatusBadge(isPending: Boolean) {
    val color = if (isPending) Warning else Mint
    Text(
        stringResource(if (isPending) R.string.detail_pending else R.string.detail_settled),
        color = color,
        fontSize = 10.5.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 11.dp, vertical = 5.dp)
            .testTag(TransactionDetailTestTags.STATUS),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Body(detail: TransactionDetailUi, isIdCopied: Boolean, onAction: (TransactionDetailAction) -> Unit) {
    val categoryColor = Color(detail.categoryColorArgb)
    Column(
        Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 24.dp)
            .navigationBarsPadding()
            .testTag(TransactionDetailTestTags.CONTENT),
    ) {
        detail.offlineSyncTime?.let { OfflineBanner(it, Modifier.padding(bottom = 12.dp)) }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(bottom = 18.dp),
        ) {
            detail.classifications.forEach { label ->
                Text(
                    label,
                    color = categoryColor,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(categoryColor.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        }

        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(SurfaceVariant)) {
            InfoRow(stringResource(R.string.detail_account), detail.accountLabel ?: stringResource(R.string.detail_value_missing))
            HorizontalDivider(color = Outline)
            InfoRow(stringResource(R.string.detail_payment_method), detail.paymentMethod)
            HorizontalDivider(color = Outline)
            InfoRow(stringResource(R.string.detail_reference), detail.reference, monospace = true)
            HorizontalDivider(color = Outline)
            InfoRow(stringResource(R.string.detail_balance_after), detail.formattedBalanceAfter ?: stringResource(R.string.detail_value_missing))
        }
        Spacer(Modifier.height(14.dp))

        InsightCard(detail.insight)
        Spacer(Modifier.height(14.dp))

        if (detail.showManageInBills) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Blue.copy(alpha = 0.07f))
                    .border(1.dp, Blue.copy(alpha = 0.22f), RoundedCornerShape(16.dp))
                    .clickable(role = Role.Button) { onAction(TransactionDetailAction.ManageInBillsClicked) }
                    .padding(horizontal = 15.dp, vertical = 13.dp)
                    .testTag(TransactionDetailTestTags.MANAGE_IN_BILLS),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null, tint = Blue, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(12.dp))
                Text(
                    stringResource(R.string.detail_manage_in_bills),
                    color = OnSurface,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.height(14.dp))
        }

        Text(
            stringResource(R.string.detail_transaction_id),
            color = OnSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(top = 6.dp, bottom = 8.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceVariant)
                .clickable(role = Role.Button, onClickLabel = stringResource(R.string.detail_copy_id)) {
                    onAction(TransactionDetailAction.CopyIdClicked)
                }
                .padding(horizontal = 14.dp, vertical = 11.dp)
                .testTag(TransactionDetailTestTags.TRANSACTION_ID),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(detail.transactionId, color = OnSurfaceVariant, fontSize = 11.sp, fontFamily = RobotoMono, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            if (isIdCopied) {
                Text(stringResource(R.string.detail_copied), color = Blue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            } else {
                Icon(Icons.Outlined.ContentCopy, contentDescription = stringResource(R.string.detail_copy_id), tint = OnSurfaceVariant, modifier = Modifier.size(14.dp))
            }
        }
        Spacer(Modifier.height(20.dp))

        Text(
            stringResource(R.string.detail_report_issue),
            color = ErrorOnWhite,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .border(1.dp, Color(0x4DFF4B4B), RoundedCornerShape(18.dp))
                .clickable(role = Role.Button) { onAction(TransactionDetailAction.ReportIssueClicked) }
                .padding(15.dp),
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String, monospace: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = OnSurfaceVariant, fontSize = 13.sp)
        Spacer(Modifier.width(12.dp))
        Text(
            value,
            color = OnSurface,
            fontSize = if (monospace) 12.5.sp else 13.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = if (monospace) RobotoMono else null,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun InsightCard(insight: MerchantInsightUi) {
    val (title, body) = when (insight) {
        is MerchantInsightUi.FirstTime ->
            stringResource(R.string.detail_insight_first_time_title, insight.merchant) to
                pluralStringResource(R.plurals.detail_insight_first_time_body, insight.lookbackDays, insight.lookbackDays)
        is MerchantInsightUi.MonthlyTotal -> {
            val count = pluralStringResource(R.plurals.detail_insight_monthly_count, insight.count, insight.count)
            stringResource(R.string.detail_insight_monthly_title, insight.formattedTotal, insight.merchant) to
                if (insight.isTopMerchant) stringResource(R.string.detail_insight_top_merchant, count) else count
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceVariant)
            .padding(horizontal = 16.dp, vertical = 15.dp)
            .testTag(TransactionDetailTestTags.INSIGHT),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(38.dp).background(Surface, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.BarChart, contentDescription = null, tint = Blue, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = OnSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(body, color = OnSurfaceVariant, fontSize = 11.5.sp)
        }
    }
}

@Composable
private fun BodySkeleton() {
    Column(
        Modifier.padding(20.dp).testTag(TransactionDetailTestTags.SKELETON),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SkeletonBlock(Modifier.width(180.dp).height(30.dp), CircleShape)
        SkeletonBlock(Modifier.fillMaxWidth().height(200.dp), RoundedCornerShape(18.dp))
        SkeletonBlock(Modifier.fillMaxWidth().height(70.dp), RoundedCornerShape(18.dp))
        SkeletonBlock(Modifier.fillMaxWidth().height(44.dp), RoundedCornerShape(12.dp))
    }
}

object TransactionDetailTestTags {
    const val CONTENT = "transaction_detail_content"
    const val SKELETON = "transaction_detail_skeleton"
    const val STATUS = "transaction_detail_status"
    const val AMOUNT = "transaction_detail_amount"
    const val INSIGHT = "transaction_detail_insight"
    const val MANAGE_IN_BILLS = "transaction_detail_manage_in_bills"
    const val TRANSACTION_ID = "transaction_detail_id"
}

// Preview-only sample data.
private val previewDetail = TransactionDetailUi(
    transactionId = "preview-id",
    title = "Energy supplier",
    initial = "E",
    categoryColorArgb = 0xFFFF4B4B.toInt(),
    formattedAmount = "-£10.00",
    isCredit = false,
    isPending = false,
    dateTime = "1 Jan 2026 · 09:00",
    classifications = listOf("Bills and Utilities", "Energy"),
    accountLabel = "Current account · Bank",
    paymentMethod = "Direct Debit",
    reference = "ENERGY",
    formattedBalanceAfter = "£90.00",
    insight = MerchantInsightUi.MonthlyTotal("Energy supplier", "£10.00", 1, isTopMerchant = true),
    showManageInBills = true,
    offlineSyncTime = null,
)

@Preview(widthDp = 412, heightDp = 1000)
@Composable
private fun TransactionDetailPreview() {
    ClearSpendTheme(darkTheme = false) { TransactionDetailContent(TransactionDetailUiState(detail = previewDetail), onAction = {}) }
}

@Preview(widthDp = 412, heightDp = 892)
@Composable
private fun TransactionDetailPendingPreview() {
    val pending = previewDetail.copy(
        isPending = true,
        formattedBalanceAfter = null,
        showManageInBills = false,
        insight = MerchantInsightUi.FirstTime("Energy supplier", 90),
    )
    ClearSpendTheme(darkTheme = false) { TransactionDetailContent(TransactionDetailUiState(detail = pending), onAction = {}) }
}

@Preview(widthDp = 412, heightDp = 892)
@Composable
private fun TransactionDetailLoadingPreview() {
    ClearSpendTheme(darkTheme = false) { TransactionDetailContent(TransactionDetailUiState(isLoading = true), onAction = {}) }
}
