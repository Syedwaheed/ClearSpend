package com.edu.feature.auth.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edu.core.presentation.designsystem.Blue
import com.edu.core.presentation.designsystem.Navy2
import com.edu.core.presentation.designsystem.buttonShape

/** Navy2 rounded tile with the ClearSpend shield (splash 84dp, connect-bank 56dp). */
@Composable
fun ShieldLogoTile(
    tileSize: Dp,
    cornerRadius: Dp,
    iconSize: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(tileSize)
            .background(Navy2, RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center,
    ) {
        StrokeIcon(paths = AuthIcons.shieldCheck, size = iconSize)
    }
}

/** Full-width 24dp pill CTA (17dp vertical padding, 16sp bold). */
@Composable
fun PrimaryPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = buttonShape,
        colors = ButtonDefaults.buttonColors(containerColor = Blue, contentColor = Color.White),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 54.dp),
    ) {
        Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

/** Square brand badge with the bank's initial(s). */
@Composable
fun BankBadge(
    initial: String,
    colorArgb: Long,
    size: Dp,
    cornerRadius: Dp,
    fontSizeSp: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(Color(colorArgb), RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initial,
            color = Color.White,
            fontSize = fontSizeSp.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
