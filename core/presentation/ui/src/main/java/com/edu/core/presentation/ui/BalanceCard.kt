package com.edu.core.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.edu.core.domain.model.Balance
import com.edu.core.presentation.designsystem.ElectricBlue
import com.edu.core.presentation.designsystem.MintGreen
import com.edu.core.presentation.designsystem.Spacing
import com.edu.core.presentation.designsystem.cardShape
import java.text.NumberFormat
import java.util.Locale

private val ElectricBlueDark = Color(0xFF1B4FC4)

@Composable
fun BalanceCard(
    balance: Balance,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = remember(Locale.UK) {
        NumberFormat.getCurrencyInstance(Locale.UK)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(ElectricBlue, ElectricBlueDark)
                )
            )
            .padding(Spacing.md)
    ) {
        Text(
            text = "Total Available Balance",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(Spacing.xs))
        Text(
            text = currencyFormatter.format(balance.available),
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(Spacing.xs))
        Text(
            text = "Current Balance: ${currencyFormatter.format(balance.current)}",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
        if (balance.overdraft > 0) {
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = "Overdraft: ${currencyFormatter.format(balance.overdraft)} available",
                style = MaterialTheme.typography.bodyMedium,
                color = MintGreen
            )
        }
        Spacer(modifier = Modifier.height(Spacing.md))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Updated just now",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        }
    }
}
