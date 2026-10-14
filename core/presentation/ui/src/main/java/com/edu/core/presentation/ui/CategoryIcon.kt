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
import com.edu.core.presentation.designsystem.MintGreen

private val Orange = Color(0xFFFF9800)
private val Purple = Color(0xFF9C27B0)
private val Blue = Color(0xFF2196F3)
private val Teal = Color(0xFF009688)
private val Grey = Color(0xFF757575)
private val Green = Color(0xFF4CAF50)

private data class CategoryStyle(val color: Color, val icon: ImageVector)

private fun categoryStyleFor(category: String): CategoryStyle = when (category) {
    "PURCHASE" -> CategoryStyle(Orange, Icons.Filled.ShoppingCart)
    "DIRECT_DEBIT" -> CategoryStyle(Purple, Icons.Filled.Repeat)
    "BILL_PAYMENT" -> CategoryStyle(Blue, Icons.Filled.Receipt)
    "TRANSFER" -> CategoryStyle(Teal, Icons.Filled.SwapHoriz)
    "ATM" -> CategoryStyle(Grey, Icons.Filled.LocalAtm)
    "CASH" -> CategoryStyle(Green, Icons.Filled.AttachMoney)
    "CREDIT" -> CategoryStyle(MintGreen, Icons.Filled.ArrowDownward)
    else -> CategoryStyle(Grey, Icons.AutoMirrored.Filled.HelpOutline)
}

@Composable
fun CategoryIcon(
    category: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val style = categoryStyleFor(category)

    Box(
        modifier = modifier
            .size(size)
            .background(color = style.color, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = style.icon,
            contentDescription = category,
            tint = Color.White,
            modifier = Modifier.size(size / 2)
        )
    }
}
