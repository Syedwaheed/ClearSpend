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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.edu.core.presentation.designsystem.Navy2
import com.edu.core.presentation.designsystem.OnNavy
import com.edu.core.presentation.designsystem.OnNavyMuted
import com.edu.core.presentation.designsystem.Spacing
import com.edu.core.presentation.designsystem.cardShape
import com.edu.core.presentation.designsystem.chipShape
import com.edu.core.presentation.ui.model.AccountUi
import com.edu.core.presentation.ui.model.BalanceUi

@Composable
fun AccountCard(
    account: AccountUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    balance: BalanceUi? = null,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = Navy2),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ProviderLogo(
                    logoUrl = account.providerLogoUri,
                    providerName = account.providerDisplayName,
                )

                Box(
                    modifier = Modifier
                        .background(
                            color = Color(account.accountTypeColorArgb).copy(alpha = 0.18f),
                            shape = chipShape,
                        )
                        .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
                ) {
                    Text(
                        text = account.accountTypeBadge,
                        style = MaterialTheme.typography.labelSmall,
                        color = OnNavy,
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            Text(
                text = account.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = OnNavy,
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            (account.maskedAccountNumber ?: account.maskedIban)?.let { masked ->
                Text(
                    text = masked,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnNavyMuted,
                )
            }

            account.formattedSortCode?.let { sortCode ->
                Text(
                    text = "Sort code: $sortCode",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnNavyMuted,
                )
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            if (balance != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Text(
                        text = balance.formattedAvailable,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnNavy,
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
                .background(color = Color.White, shape = CircleShape),
        )
    } else {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(color = Color.White.copy(alpha = 0.15f), shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = providerName.take(1).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = OnNavy,
            )
        }
    }
}
