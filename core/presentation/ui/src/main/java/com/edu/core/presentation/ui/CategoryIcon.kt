package com.edu.core.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CategoryIcon(
    iconId: String,
    colorArgb: Int,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(color = Color(colorArgb), shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = iconFor(iconId),
            contentDescription = iconId,
            tint = Color.White,
            modifier = Modifier.size(size / 2),
        )
    }
}

private fun iconFor(iconId: String): ImageVector = when (iconId) {
    "shopping_cart" -> Icons.Filled.ShoppingCart
    "repeat" -> Icons.Filled.Repeat
    "receipt" -> Icons.Filled.Receipt
    "swap_horiz" -> Icons.Filled.SwapHoriz
    "local_atm" -> Icons.Filled.LocalAtm
    "attach_money" -> Icons.Filled.AttachMoney
    "arrow_downward" -> Icons.Filled.ArrowDownward
    else -> Icons.AutoMirrored.Filled.HelpOutline
}
