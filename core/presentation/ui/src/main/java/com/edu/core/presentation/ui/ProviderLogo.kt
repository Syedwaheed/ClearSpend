package com.edu.core.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage

/**
 * Bank logo from `provider.logo_uri` via Coil; while loading, on error, or
 * without a URI it falls back to the design's initial-on-colour badge
 * (DESIGN_SPEC §8: colour/initial are fallbacks when the logo fails).
 */
@Composable
fun ProviderLogo(
    logoUri: String?,
    initial: String,
    fallbackColorArgb: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    cornerRadius: Dp = 7.dp,
) {
    val shape = RoundedCornerShape(cornerRadius)
    val fallback: @Composable () -> Unit = {
        Box(
            modifier = Modifier.size(size).background(Color(fallbackColorArgb), shape),
            contentAlignment = Alignment.Center,
        ) {
            Text(initial, color = Color.White, fontSize = (size.value * 0.45f).sp, fontWeight = FontWeight.Bold)
        }
    }
    if (logoUri == null) {
        Box(modifier) { fallback() }
        return
    }
    SubcomposeAsyncImage(
        model = logoUri,
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        modifier = modifier.size(size).clip(shape),
        loading = { fallback() },
        error = { fallback() },
    )
}
