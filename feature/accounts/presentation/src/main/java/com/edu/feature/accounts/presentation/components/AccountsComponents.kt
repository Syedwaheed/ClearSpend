package com.edu.feature.accounts.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edu.core.presentation.designsystem.OnSurface
import com.edu.core.presentation.designsystem.Outline
import com.edu.core.presentation.designsystem.SurfaceVariant
import com.edu.feature.accounts.presentation.R

/** Back circle + 17sp title used by Account Detail (11) and Card (21). */
@Composable
internal fun BackHeader(title: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(SurfaceVariant)
                .clickable(role = Role.Button, onClickLabel = stringResource(R.string.detail_back), onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = stringResource(R.string.detail_back), tint = OnSurface)
        }
        Spacer(Modifier.width(12.dp))
        Text(title, color = OnSurface, fontSize = 17.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** Small uppercase label + value pair used in stat rows. */
@Composable
internal fun StatCell(label: String, value: String, labelColor: Color, valueColor: Color, modifier: Modifier = Modifier, valueSize: Int = 12) {
    Column(modifier) {
        Text(label, color = labelColor, fontSize = 9.5.sp, maxLines = 1)
        Spacer(Modifier.height(2.dp))
        Text(value, color = valueColor, fontSize = valueSize.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
internal fun UtilisationBar(fraction: Float, color: Color, modifier: Modifier = Modifier, height: Int = 6) {
    val shape = RoundedCornerShape((height / 2f).dp)
    Box(modifier.fillMaxWidth().height(height.dp).clip(shape).background(Outline)) {
        if (fraction > 0f) {
            Box(Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)).fillMaxHeight().clip(shape).background(color))
        }
    }
}

/** Mini card face used in the credit-card rows. */
@Composable
internal fun MiniCardFace(width: Int, height: Int, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(width.dp, height.dp)
            .background(
                androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF1B2A47), Color(0xFF0F1D38))),
                RoundedCornerShape(8.dp),
            ),
    )
}

@Composable
internal fun Modifier.screenPadding(): Modifier = padding(horizontal = 20.dp)
