package com.edu.core.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.edu.core.presentation.designsystem.OnSurfaceVariant
import com.edu.core.presentation.designsystem.Spacing
import com.edu.core.presentation.ui.model.TransactionUi

@Composable
fun TransactionRow(
    transaction: TransactionUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.clickable(onClick = onClick)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryIcon(iconId = transaction.categoryIconId, colorArgb = transaction.categoryColorArgb)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Spacing.sm),
            ) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = transaction.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant,
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                AmountText(
                    formattedAmount = transaction.formattedAmount,
                    colorArgb = transaction.amountColorArgb,
                    textStyle = MaterialTheme.typography.bodyMedium,
                )
                transaction.formattedRunningBalance?.let { balance ->
                    Text(
                        text = balance,
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant,
                    )
                }
            }
        }
        HorizontalDivider()
    }
}
