package com.edu.feature.auth.presentation.bankselection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.edu.core.presentation.designsystem.Blue
import com.edu.core.presentation.designsystem.ClearSpendTheme
import com.edu.core.presentation.designsystem.ErrorOnWhite
import com.edu.core.presentation.designsystem.OnSurface
import com.edu.core.presentation.designsystem.OnSurfaceVariant
import com.edu.core.presentation.designsystem.Outline
import com.edu.core.presentation.designsystem.Surface
import com.edu.core.presentation.designsystem.SurfaceVariant
import com.edu.core.presentation.designsystem.chipShape
import com.edu.core.presentation.ui.ObserveAsEvent
import com.edu.core.presentation.ui.SkeletonBlock
import com.edu.feature.auth.presentation.R
import com.edu.feature.auth.presentation.components.AuthIcons
import com.edu.feature.auth.presentation.components.BankBadge
import com.edu.feature.auth.presentation.components.PrimaryPillButton
import com.edu.feature.auth.presentation.components.StrokeIcon
import com.edu.feature.auth.presentation.model.BankUi
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BankSelectionScreen(
    onNavigateBack: () -> Unit,
    viewModel: BankSelectionViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    ObserveAsEvent(viewModel.effect) { effect ->
        when (effect) {
            is BankSelectionEffect.OpenAuthLink ->
                if (!launchAuthCustomTab(context, effect.url)) {
                    viewModel.onAction(BankSelectionAction.BrowserUnavailable)
                }
            BankSelectionEffect.NavigateBack -> onNavigateBack()
        }
    }
    BankSelectionContent(state = state, onAction = viewModel::onAction)
}

private val HintGrey = Color(0xFF9AA3AF)

@Composable
fun BankSelectionContent(
    state: BankSelectionUiState,
    onAction: (BankSelectionAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .statusBarsPadding(),
    ) {
        Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(SurfaceVariant, CircleShape)
                    .clickable(role = Role.Button, onClickLabel = stringResource(R.string.bank_select_back)) {
                        onAction(BankSelectionAction.BackClicked)
                    },
                contentAlignment = Alignment.Center,
            ) { StrokeIcon(paths = AuthIcons.chevronBack, size = 18.dp) }
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.bank_select_title), color = OnSurface, fontSize = 21.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(14.dp))
            SearchField(query = state.query, onQueryChange = { onAction(BankSelectionAction.QueryChanged(it)) })
            Spacer(Modifier.height(14.dp))
            if (state.hasLaunchError) {
                Text(
                    stringResource(R.string.bank_select_launch_error),
                    color = ErrorOnWhite,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 10.dp),
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when {
                state.isLoading -> BankGridSkeleton()
                state.hasError -> StateMessage(
                    title = stringResource(R.string.error_title),
                    body = stringResource(R.string.error_body),
                    cta = stringResource(R.string.error_try_again),
                    onCta = { onAction(BankSelectionAction.RetryClicked) },
                )
                state.isEmpty -> StateMessage(
                    title = stringResource(R.string.bank_select_empty_title),
                    body = stringResource(R.string.bank_select_empty_body, state.query.trim()),
                    cta = stringResource(R.string.bank_select_empty_cta),
                    onCta = { onAction(BankSelectionAction.ClearQueryClicked) },
                )
                else -> BankGrid(
                    banks = state.banks,
                    enabled = !state.isOpeningBank,
                    onBankClick = { onAction(BankSelectionAction.BankSelected(it.providerId)) },
                )
            }
            if (state.isOpeningBank) {
                CircularProgressIndicator(color = Blue, modifier = Modifier.align(Alignment.Center))
            }
        }

        HorizontalDivider(color = Outline)
        TextButton(
            onClick = { onAction(BankSelectionAction.OtherBankClicked) },
            enabled = !state.isOpeningBank,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Text(stringResource(R.string.bank_select_dont_see), color = Blue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceVariant, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StrokeIcon(paths = AuthIcons.search, size = 16.dp)
        Spacer(Modifier.width(8.dp))
        Box(Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(stringResource(R.string.bank_select_search_hint), color = HintGrey, fontSize = 14.sp)
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = TextStyle(color = OnSurface, fontSize = 14.sp),
                cursorBrush = SolidColor(Blue),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun BankGrid(banks: List<BankUi>, enabled: Boolean, onBankClick: (BankUi) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(banks, key = { it.providerId }) { bank ->
            BankTile(bank = bank, enabled = enabled, onClick = { onBankClick(bank) })
        }
    }
}

@Composable
private fun BankTile(bank: BankUi, enabled: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Outline, shape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(14.dp),
    ) {
        BankBadge(initial = bank.initial, colorArgb = bank.brandColorArgb, size = 34.dp, cornerRadius = 10.dp, fontSizeSp = 13)
        Spacer(Modifier.height(10.dp))
        Text(bank.name, color = OnSurface, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(
            text = bank.segmentLabel,
            color = Blue,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .background(Blue.copy(alpha = 0.1f), chipShape)
                .padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun BankGridSkeleton() {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        repeat(4) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SkeletonBlock(Modifier.weight(1f).height(112.dp))
                SkeletonBlock(Modifier.weight(1f).height(112.dp))
            }
        }
    }
}

/** DESIGN_SPEC §7 Empty / Error layout: 56dp rounded icon square, title, one-line hint, button. */
@Composable
private fun StateMessage(title: String, body: String, cta: String, onCta: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(SurfaceVariant, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
        ) { StrokeIcon(paths = AuthIcons.search, size = 24.dp) }
        Spacer(Modifier.height(16.dp))
        Text(title, color = OnSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(body, color = OnSurfaceVariant, fontSize = 13.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        PrimaryPillButton(text = cta, onClick = onCta)
    }
}

private val previewBanks = listOf(
    BankUi("ob-barclays", "Barclays", "B", 0xFF00AEEF, "Personal"),
    BankUi("ob-hsbc", "HSBC", "H", 0xFFDB0011, "Personal"),
    BankUi("ob-lloyds", "Lloyds", "L", 0xFF024731, "Personal"),
    BankUi("ob-natwest", "NatWest", "N", 0xFF5A287D, "Personal"),
    BankUi("ob-starling", "Starling", "St", 0xFF00D4AA, "Personal"),
    BankUi("ob-first-direct", "First Direct", "FD", 0xFF000000, "Personal"),
)

@Preview(widthDp = 412, heightDp = 892)
@Composable
private fun BankSelectionPreview() {
    ClearSpendTheme { BankSelectionContent(BankSelectionUiState(banks = previewBanks), onAction = {}) }
}

@Preview(widthDp = 412, heightDp = 892)
@Composable
private fun BankSelectionLoadingPreview() {
    ClearSpendTheme { BankSelectionContent(BankSelectionUiState(isLoading = true), onAction = {}) }
}

@Preview(widthDp = 412, heightDp = 892)
@Composable
private fun BankSelectionEmptyPreview() {
    ClearSpendTheme { BankSelectionContent(BankSelectionUiState(query = "zz"), onAction = {}) }
}

@Preview(widthDp = 412, heightDp = 892)
@Composable
private fun BankSelectionErrorPreview() {
    ClearSpendTheme { BankSelectionContent(BankSelectionUiState(hasError = true), onAction = {}) }
}
