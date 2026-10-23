package com.edu.clearspend.splash

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.StartOffset
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edu.clearspend.R
import com.edu.core.presentation.designsystem.Blue
import com.edu.core.presentation.designsystem.ClearSpendTheme
import com.edu.core.presentation.designsystem.Mint
import com.edu.core.presentation.designsystem.Navy
import com.edu.core.presentation.designsystem.Navy2
import com.edu.core.presentation.designsystem.OnNavy
import com.edu.core.presentation.ui.ObserveAsEvent
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToConnectBank: () -> Unit,
    onNavigateToBiometric: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    viewModel: SplashViewModel = koinViewModel(),
) {
    ObserveAsEvent(viewModel.effect) { effect ->
        when (effect) {
            SplashEffect.NavigateToOnboarding -> onNavigateToOnboarding()
            SplashEffect.NavigateToConnectBank -> onNavigateToConnectBank()
            SplashEffect.NavigateToBiometric -> onNavigateToBiometric()
            SplashEffect.NavigateToDashboard -> onNavigateToDashboard()
        }
    }
    SplashContent()
}

private val shieldPath = PathParser().parsePathString("M12 2l8 3v6c0 5-3.5 8.5-8 10-4.5-1.5-8-5-8-10V5l8-3z").toPath()
private val checkPath = PathParser().parsePathString("M8.5 12l2.3 2.3L16 9").toPath()

/** Screen 01: static brand content (DESIGN_SPEC §4 "01 Splash"). */
@Composable
fun SplashContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .background(Navy2, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.size(42.dp)) {
                scale(size.minDimension / 24f, pivot = Offset.Zero) {
                    drawPath(shieldPath, Blue, style = Stroke(1.8f, join = StrokeJoin.Round))
                    drawPath(checkPath, Mint, style = Stroke(2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
            }
        }
        Spacer(Modifier.height(22.dp))
        Text(stringResource(R.string.app_name), color = OnNavy, fontSize = 26.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.splash_tagline), color = OnNavy.copy(alpha = 0.55f), fontSize = 14.sp)
        Spacer(Modifier.height(70.dp))
        LoadingDots()
        Spacer(Modifier.height(110.dp))
        Text(
            text = stringResource(R.string.splash_powered_by).uppercase(),
            color = OnNavy.copy(alpha = 0.35f),
            fontSize = 10.sp,
            letterSpacing = 1.sp,
        )
    }
}

/** Three 7dp blue dots pulsing with a 200ms stagger (prototype `cs-dot`). */
@Composable
private fun LoadingDots() {
    val transition = rememberInfiniteTransition(label = "dots")
    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        repeat(3) { index ->
            val alpha by transition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(index * 200),
                ),
                label = "dot$index",
            )
            Box(
                Modifier
                    .size(7.dp)
                    .alpha(alpha)
                    .background(Blue, CircleShape),
            )
        }
    }
}

@Preview(widthDp = 412, heightDp = 892)
@Composable
private fun SplashPreview() {
    ClearSpendTheme { SplashContent() }
}
