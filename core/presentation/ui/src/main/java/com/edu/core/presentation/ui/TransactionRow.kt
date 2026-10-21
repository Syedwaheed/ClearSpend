package com.edu.core.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edu.core.presentation.designsystem.ClearSpendTheme
import com.edu.core.presentation.designsystem.OnSurface
import com.edu.core.presentation.designsystem.OnSurfaceVariant
import com.edu.core.presentation.ui.model.TransactionUi

/**
 * Transaction list row (Home 09, Account Detail 11): category-tinted initial
 * square, merchant, [subtitle], signed amount in red/green. Rows sit inside a
 * `surfaceVariant` group card; the caller draws the dividers.
 */
@Composable
fun TransactionRow(
    transaction: TransactionUi,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    subtitle: String = transaction.compactSubtitle,
    iconSize: Dp = 38.dp,
) {
    val categoryColor = Color(transaction.categoryColorArgb)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(iconSize)
                .background(categoryColor.copy(alpha = 0.14f), RoundedCornerShape(iconSize * 0.32f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(transaction.initial, color = categoryColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle.isNotEmpty()) {
                Text(text = subtitle, fontSize = 11.5.sp, color = OnSurfaceVariant, maxLines = 1)
            }
        }
        Spacer(Modifier.width(8.dp))
        AmountText(
            formattedAmount = transaction.formattedAmount,
            colorArgb = transaction.amountColorArgb,
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold),
        )
    }
}

@Preview
@Composable
private fun TransactionRowPreview() {
    ClearSpendTheme {
        TransactionRow(
            TransactionUi(
                "t", "Tesco", "", "-£39.99", 0xFFE03B3B.toInt(), true, "", "", 0xFF2D6EF5.toInt(), null, false,
                initial = "T", compactSubtitle = "Shopping · Today",
            ),
            onClick = {},
        )
    }
}
