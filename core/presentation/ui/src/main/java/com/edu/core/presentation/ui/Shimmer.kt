package com.edu.core.presentation.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import com.edu.core.presentation.designsystem.SurfaceVariant
import com.edu.core.presentation.designsystem.cardShape

/**
 * Animated shimmer for skeleton placeholders (DESIGN_SPEC §7 Loading):
 * `surfaceVariant` blocks with a moving highlight.
 */
fun Modifier.shimmer(
    shape: Shape = cardShape,
    baseColor: Color = SurfaceVariant,
    highlightColor: Color = Color.White,
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerProgress",
    )
    val start = -600f + progress * 1_800f
    background(
        brush = Brush.linearGradient(
            colors = listOf(baseColor, highlightColor.copy(alpha = 0.7f), baseColor),
            start = Offset(start, 0f),
            end = Offset(start + 600f, 0f),
        ),
        shape = shape,
    )
}

/** A single skeleton block; size it with [modifier]. */
@Composable
fun SkeletonBlock(
    modifier: Modifier = Modifier,
    shape: Shape = cardShape,
) {
    Box(modifier = modifier.shimmer(shape = shape))
}
