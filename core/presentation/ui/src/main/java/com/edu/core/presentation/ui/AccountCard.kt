package com.edu.core.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.edu.core.domain.model.Account
import com.edu.core.domain.model.Balance
import com.edu.core.presentation.designsystem.CardNavy
import com.edu.core.presentation.designsystem.Spacing
import com.edu.core.presentation.designsystem.TextPrimary
import com.edu.core.presentation.designsystem.TextSecondary
import com.edu.core.presentation.designsystem.cardShape
import com.edu.core.presentation.designsystem.chipShape
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AccountCard(
    account: Account,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    balance: Balance? = null
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = CardNavy),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProviderLogo(
                    logoUrl = account.provider.logoUrl,
                    providerName = account.provider.displayName
                )

                Box(
                    modifier = Modifier
                        .background(color = Color.White.copy(alpha = 0.1f), shape = chipShape)
                        .padding(horizontal = Spacing.sm, vertical = Spacing.xs)
                ) {
                    Text(
                        text = account.accountType,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            Text(
                text = account.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            Text(
                text = maskedAccountNumber(account.accountNumber.number),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            account.accountNumber.sortCode?.let { sortCode ->
                Text(
                    text = "Sort code: ${formatSortCode(sortCode)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            if (balance != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    val formatter = remember { NumberFormat.getCurrencyInstance(Locale.UK) }
                    Text(
                        text = formatter.format(balance.available),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun ProviderLogo(logoUrl: String?, providerName: String) {
    if (logoUrl != null) {
        AsyncImage(
            model = logoUrl,
            contentDescription = providerName,
            modifier = Modifier
                .size(32.dp)
                .background(color = Color.White, shape = CircleShape)
        )
    } else {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(color = Color.White.copy(alpha = 0.15f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = providerName.take(1).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = TextPrimary
            )
        }
    }
}

private fun maskedAccountNumber(number: String?): String {
    val lastFour = number?.takeLast(4).orEmpty()
    return "•••• $lastFour"
}

private fun formatSortCode(sortCode: String): String {
    val digits = sortCode.filter { it.isDigit() }
    return digits.chunked(2).joinToString("-")
}
