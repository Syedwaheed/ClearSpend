package com.edu.feature.auth.presentation.onboarding

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.edu.core.presentation.designsystem.Blue
import com.edu.core.presentation.designsystem.ClearSpendTheme
import com.edu.core.presentation.designsystem.Navy
import com.edu.core.presentation.designsystem.OnNavy
import com.edu.core.presentation.designsystem.OnNavyMuted
import com.edu.core.presentation.ui.ObserveAsEvent
import com.edu.feature.auth.presentation.R
import com.edu.feature.auth.presentation.components.AuthIcons
import com.edu.feature.auth.presentation.components.PrimaryPillButton
import com.edu.feature.auth.presentation.components.StrokeIcon
import com.edu.feature.auth.presentation.components.StrokePath
import org.koin.compose.viewmodel.koinViewModel

private data class OnboardingPage(
    @param:StringRes val title: Int,
    @param:StringRes val body: Int,
    val icon: List<StrokePath>,
    val iconBackground: Color,
)

private val pages = listOf(
    OnboardingPage(R.string.onboarding_1_title, R.string.onboarding_1_body, AuthIcons.phoneSignal, Blue.copy(alpha = 0.12f)),
    OnboardingPage(R.string.onboarding_2_title, R.string.onboarding_2_body, AuthIcons.spendingClock, Color(0x1F00D4AA)),
    OnboardingPage(R.string.onboarding_3_title, R.string.onboarding_3_body, AuthIcons.sendArrow, Blue.copy(alpha = 0.12f)),
)

@Composable
fun OnboardingScreen(
    onNavigateToConnectBank: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ObserveAsEvent(viewModel.effect) { effect ->
        when (effect) {
            OnboardingEffect.NavigateToConnectBank -> onNavigateToConnectBank()
            OnboardingEffect.NavigateToSignIn -> onNavigateToSignIn()
        }
    }
    OnboardingContent(state = state, onAction = viewModel::onAction)
}

@Composable
fun OnboardingContent(
    state: OnboardingUiState,
    onAction: (OnboardingAction) -> Unit,
) {
    val pagerState = rememberPagerState(initialPage = state.currentPage, pageCount = { state.pageCount })

    // VM → pager (Next button)
    LaunchedEffect(state.currentPage) {
        if (pagerState.currentPage != state.currentPage) pagerState.animateScrollToPage(state.currentPage)
    }
    // pager → VM (swipe)
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { onAction(OnboardingAction.PageSettled(it)) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy)
            .safeDrawingPadding()
            .padding(horizontal = 28.dp, vertical = 32.dp),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) { index -> OnboardingPageContent(pages[index]) }

        PageIndicator(
            pageCount = state.pageCount,
            currentPage = state.currentPage,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 24.dp),
        )
        PrimaryPillButton(
            text = stringResource(if (state.isLastPage) R.string.onboarding_get_started else R.string.onboarding_next),
            onClick = {
                onAction(if (state.isLastPage) OnboardingAction.GetStartedClicked else OnboardingAction.NextClicked)
            },
        )
        if (state.isLastPage) {
            Spacer(Modifier.height(14.dp))
            Text(
                text = buildAnnotatedString {
                    append(stringResource(R.string.onboarding_have_account))
                    append(" ")
                    withStyle(SpanStyle(color = Blue, fontWeight = FontWeight.SemiBold)) {
                        append(stringResource(R.string.onboarding_sign_in))
                    }
                },
                color = OnNavyMuted,
                fontSize = 13.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable { onAction(OnboardingAction.SignInClicked) }
                    .padding(8.dp),
            )
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(page.iconBackground, RoundedCornerShape(32.dp)),
            contentAlignment = Alignment.Center,
        ) {
            StrokeIcon(paths = page.icon, size = 56.dp)
        }
        Spacer(Modifier.height(36.dp))
        Text(
            text = stringResource(page.title),
            color = OnNavy,
            fontSize = 23.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 300.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(page.body),
            color = OnNavyMuted,
            fontSize = 14.sp,
            lineHeight = 21.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 300.dp),
        )
    }
}

/** Active page = 20×7dp blue pill, others 7dp dots at 25% (prototype onboardDots). */
@Composable
private fun PageIndicator(pageCount: Int, currentPage: Int, modifier: Modifier = Modifier) {
    val description = stringResource(R.string.onboarding_page_indicator, currentPage + 1, pageCount)
    Row(
        modifier = modifier.semantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(pageCount) { index ->
            val active = index == currentPage
            val width by animateDpAsState(if (active) 20.dp else 7.dp, label = "dotWidth")
            Box(
                modifier = Modifier
                    .size(width = width, height = 7.dp)
                    .background(
                        color = if (active) Blue else OnNavy.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(4.dp),
                    ),
            )
        }
    }
}

@Preview(widthDp = 412, heightDp = 892)
@Composable
private fun OnboardingFirstPagePreview() {
    ClearSpendTheme { OnboardingContent(state = OnboardingUiState(currentPage = 0), onAction = {}) }
}

@Preview(widthDp = 412, heightDp = 892)
@Composable
private fun OnboardingLastPagePreview() {
    ClearSpendTheme { OnboardingContent(state = OnboardingUiState(currentPage = 2), onAction = {}) }
}
