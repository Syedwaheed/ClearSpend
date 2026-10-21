package com.edu.feature.auth.presentation.connectbank

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.edu.core.presentation.designsystem.Blue
import com.edu.core.presentation.designsystem.ClearSpendTheme
import com.edu.core.presentation.designsystem.Navy
import com.edu.core.presentation.designsystem.OnNavy
import com.edu.core.presentation.designsystem.OnSurface
import com.edu.core.presentation.designsystem.OnSurfaceVariant
import com.edu.core.presentation.designsystem.Surface
import com.edu.core.presentation.designsystem.SurfaceVariant
import com.edu.core.presentation.designsystem.chipShape
import com.edu.core.presentation.ui.ObserveAsEvent
import com.edu.core.presentation.ui.SkeletonBlock
import com.edu.feature.auth.presentation.R
import com.edu.feature.auth.presentation.components.AuthIcons
import com.edu.feature.auth.presentation.components.BankBadge
import com.edu.feature.auth.presentation.components.PrimaryPillButton
import com.edu.feature.auth.presentation.components.ShieldLogoTile
import com.edu.feature.auth.presentation.components.StrokeIcon
import com.edu.feature.auth.presentation.components.StrokePath
import com.edu.feature.auth.presentation.model.BankUi
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ConnectBankScreen(
    onNavigateToBankSelection: () -> Unit,
    viewModel: ConnectBankViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ObserveAsEvent(viewModel.effect) { effect ->
        when (effect) {
            ConnectBankEffect.NavigateToBankSelection -> onNavigateToBankSelection()
        }
    }
    ConnectBankContent(state = state, onAction = viewModel::onAction)
}

private val TermsGrey = Color(0xFF9AA3AF)

@Composable
fun ConnectBankContent(
    state: ConnectBankUiState,
    onAction: (ConnectBankAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy),
    ) {
        // Navy header: 210dp tall below the status bar.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .height(210.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            ShieldLogoTile(tileSize = 56.dp, cornerRadius = 16.dp, iconSize = 28.dp)
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.app_title), color = OnNavy, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        // White sheet with 28dp top corners.
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Surface, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 28.dp),
        ) {
            Text(
                text = stringResource(R.string.connect_title),
                color = OnSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.connect_subtitle),
                color = OnSurfaceVariant,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(22.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FeatureTile(AuthIcons.lock, stringResource(R.string.connect_feature_encryption), Modifier.weight(1f))
                FeatureTile(AuthIcons.house, stringResource(R.string.connect_feature_fca), Modifier.weight(1f))
                FeatureTile(AuthIcons.eye, stringResource(R.string.connect_feature_read_only), Modifier.weight(1f))
            }
            Spacer(Modifier.height(24.dp))
            PrimaryPillButton(
                text = stringResource(R.string.connect_cta),
                onClick = { onAction(ConnectBankAction.ConnectClicked) },
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.connect_redirect_hint),
                color = OnSurfaceVariant,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(22.dp))
            Text(
                text = stringResource(R.string.connect_supported_banks).uppercase(),
                color = OnSurfaceVariant,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
            )
            Spacer(Modifier.height(10.dp))
            SupportedBanks(state = state, onRetry = { onAction(ConnectBankAction.RetryClicked) })
            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.connect_terms),
                color = TermsGrey,
                fontSize = 10.5.sp,
                lineHeight = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun FeatureTile(icon: List<StrokePath>, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(SurfaceVariant, RoundedCornerShape(14.dp))
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StrokeIcon(paths = icon, size = 18.dp)
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            color = OnSurface,
            fontSize = 10.5.sp,
            lineHeight = 13.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SupportedBanks(state: ConnectBankUiState, onRetry: () -> Unit) {
    when {
        state.isLoadingBanks -> FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(7) { SkeletonBlock(Modifier.size(width = 88.dp, height = 32.dp), shape = chipShape) }
        }

        state.hasBanksError -> Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.connect_banks_error), color = OnSurfaceVariant, fontSize = 12.sp)
            TextButton(onClick = onRetry) {
                Text(stringResource(R.string.error_try_again), color = Blue, fontWeight = FontWeight.SemiBold)
            }
        }

        else -> FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            state.featuredBanks.forEach { BankChip(it) }
        }
    }
}

@Composable
private fun BankChip(bank: BankUi) {
    Row(
        modifier = Modifier
            .background(SurfaceVariant, chipShape)
            .padding(start = 6.dp, top = 6.dp, bottom = 6.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BankBadge(initial = bank.initial, colorArgb = bank.brandColorArgb, size = 20.dp, cornerRadius = 6.dp, fontSizeSp = 9)
        Spacer(Modifier.width(6.dp))
        Text(bank.name, color = OnSurface, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

private val previewBanks = listOf(
    BankUi("ob-barclays", "Barclays", "B", 0xFF00AEEF, "Personal"),
    BankUi("ob-hsbc", "HSBC", "H", 0xFFDB0011, "Personal"),
    BankUi("ob-lloyds", "Lloyds", "L", 0xFF024731, "Personal"),
    BankUi("ob-starling", "Starling", "St", 0xFF00D4AA, "Personal"),
)

@Preview(widthDp = 412, heightDp = 892)
@Composable
private fun ConnectBankPreview() {
    ClearSpendTheme { ConnectBankContent(ConnectBankUiState(featuredBanks = previewBanks), onAction = {}) }
}

@Preview(widthDp = 412, heightDp = 892)
@Composable
private fun ConnectBankLoadingPreview() {
    ClearSpendTheme { ConnectBankContent(ConnectBankUiState(isLoadingBanks = true), onAction = {}) }
}

@Preview(widthDp = 412, heightDp = 892)
@Composable
private fun ConnectBankErrorPreview() {
    ClearSpendTheme { ConnectBankContent(ConnectBankUiState(hasBanksError = true), onAction = {}) }
}

