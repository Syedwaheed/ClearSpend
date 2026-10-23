package com.edu.clearspend.placeholder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edu.core.presentation.designsystem.ClearSpendTheme
import com.edu.core.presentation.designsystem.OnSurface
import com.edu.core.presentation.designsystem.OnSurfaceVariant
import com.edu.core.presentation.designsystem.Surface
import com.edu.core.presentation.ui.SkeletonBlock

/**
 * Stand-in for screens later phases build. Static, stateless, no data —
 * so no ViewModel. Replaced route by route in Phases 3–7.
 */
@Composable
fun PlaceholderScreen(
    title: String,
    phaseNote: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Surface)
            .statusBarsPadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        Text(title, color = OnSurface, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        SkeletonBlock(Modifier.fillMaxWidth().height(160.dp))
        Spacer(Modifier.height(12.dp))
        SkeletonBlock(Modifier.fillMaxWidth().height(72.dp))
        Spacer(Modifier.height(12.dp))
        SkeletonBlock(Modifier.fillMaxWidth().height(72.dp))
        Spacer(Modifier.height(24.dp))
        Text(
            text = phaseNote,
            color = OnSurfaceVariant,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally),
        )
    }
}

@Preview(widthDp = 412, heightDp = 892)
@Composable
private fun PlaceholderPreview() {
    ClearSpendTheme { PlaceholderScreen(title = "Home", phaseNote = "Built in Phase 3") }
}
