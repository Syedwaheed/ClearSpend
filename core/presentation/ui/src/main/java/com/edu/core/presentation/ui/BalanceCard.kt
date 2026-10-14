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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.edu.core.presentation.designsystem.Blue
import com.edu.core.presentation.designsystem.BlueDark
import com.edu.core.presentation.designsystem.Mint
import com.edu.core.presentation.designsystem.Spacing
import com.edu.core.presentation.designsystem.heroCardShape
import com.edu.core.presentation.ui.model.BalanceUi

@Composable
fun BalanceCard(
    balance: BalanceUi,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(heroCardShape)
            .background(brush = Brush.linearGradient(colors = listOf(Blue, BlueDark)))
            .padding(Spacing.md),
    ) {
        Text(
            text = "Total Available Balance",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
        )
        Spacer(modifier = Modifier.height(Spacing.xs))
        Text(
            text = balance.formattedAvailable,
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
        Spacer(modifier = Modifier.height(Spacing.xs))
        Text(
            text = "Current Balance: ${balance.formattedCurrent}",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
        )
        if (balance.hasOverdraft && balance.formattedOverdraft != null) {
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = "Overdraft: ${balance.formattedOverdraft} available",
                style = MaterialTheme.typography.bodyMedium,
                color = Mint,
            )
        }
        Spacer(modifier = Modifier.height(Spacing.md))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            Text(
                text = "Updated ${balance.formattedUpdateDate}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
            )
        }
    }
}
