package com.edu.core.presentation.ui

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

@Composable
fun AmountText(
    formattedAmount: String,
    colorArgb: Int,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
) {
    Text(
        text = formattedAmount,
        style = textStyle,
        color = Color(colorArgb),
        modifier = modifier,
    )
}
