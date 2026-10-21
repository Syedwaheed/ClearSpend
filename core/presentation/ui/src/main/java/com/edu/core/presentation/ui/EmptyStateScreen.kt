package com.edu.core.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edu.core.presentation.designsystem.Blue
import com.edu.core.presentation.designsystem.OnSurface
import com.edu.core.presentation.designsystem.OnSurfaceVariant
import com.edu.core.presentation.designsystem.Outline
import com.edu.core.presentation.designsystem.SurfaceVariant
import com.edu.core.presentation.designsystem.buttonShape

/**
 * DESIGN_SPEC §7 Empty (see 13-transactions-empty-state.png): icon in a 56dp
 * rounded square, title, one-line hint, secondary (outlined) button.
 */
@Composable
fun EmptyStateScreen(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    ctaText: String? = null,
    onCtaClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(SurfaceVariant, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(title, color = OnSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(subtitle, color = OnSurfaceVariant, fontSize = 13.sp, textAlign = TextAlign.Center)
        if (ctaText != null && onCtaClick != null) {
            Spacer(Modifier.height(20.dp))
            Text(
                text = ctaText,
                color = Blue,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .border(1.dp, Outline, buttonShape)
                    .clickable(role = Role.Button, onClick = onCtaClick)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
            )
        }
    }
}
