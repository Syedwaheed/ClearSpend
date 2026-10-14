package com.edu.core.presentation.ui

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.edu.core.presentation.designsystem.ErrorRed
import com.edu.core.presentation.designsystem.SuccessGreen
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AmountText(
    amount: Double,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current
) {
    val formatter = remember(Locale.UK) {
        NumberFormat.getCurrencyInstance(Locale.UK)
    }
    val color = if (amount < 0) ErrorRed else SuccessGreen

    Text(
        text = formatter.format(amount),
        style = textStyle,
        color = color,
        modifier = modifier
    )
}
