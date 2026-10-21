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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edu.core.presentation.designsystem.ClearSpendTheme
import com.edu.core.presentation.designsystem.Navy
import com.edu.core.presentation.designsystem.Success
import com.edu.core.presentation.designsystem.SurfaceVariant
import com.edu.core.presentation.designsystem.OnSurface
import com.edu.core.presentation.designsystem.OnSurfaceVariant
import com.edu.core.presentation.ui.model.TransactionUi

/**
 * Transaction list row (Home 09, Account Detail 11, Transactions 12):
 * category-tinted initial square, merchant, [subtitle], signed amount in
 * red/green. Rows sit inside a `surfaceVariant` group card; the caller draws
 * the dividers.
 *
 * Transactions (12) also shows the ↑/↓ [showDirectionBadge] on the icon and a
 * [trailingCaption] under the amount (running balance, or "Not settled").
 */
@Composable
fun TransactionRow(
    transaction: TransactionUi,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    subtitle: String = transaction.compactSubtitle,
    iconSize: Dp = 38.dp,
    showDirectionBadge: Boolean = false,
    trailingCaption: String? = null,
    trailingCaptionColor: Color = OnSurfaceVariant,
    trailingCaptionBold: Boolean = false,
    amountColor: Color = Color(transaction.amountColorArgb),
    badgeBorderColor: Color = SurfaceVariant,
) {
    val categoryColor = Color(transaction.categoryColorArgb)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(iconSize)
                    .background(categoryColor.copy(alpha = 0.14f), RoundedCornerShape(iconSize * 0.32f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(transaction.initial, color = categoryColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            if (showDirectionBadge) {
                DirectionBadge(
                    isDebit = transaction.isDebit,
                    borderColor = badgeBorderColor,
                    modifier = Modifier.align(Alignment.BottomEnd).offset(x = 3.dp, y = 3.dp),
                )
            }
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
        Column(horizontalAlignment = Alignment.End) {
            AmountText(
                formattedAmount = transaction.formattedAmount,
                colorArgb = amountColor.toArgb(),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold),
            )
            if (trailingCaption != null) {
                Text(
                    text = trailingCaption,
                    color = trailingCaptionColor,
                    fontSize = 10.5.sp,
                    fontWeight = if (trailingCaptionBold) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                )
            }
        }
    }
}

/** 16dp ↓ (DEBIT, navy) / ↑ (CREDIT, green) disc on the row icon's corner (screen 12). */
@Composable
private fun DirectionBadge(isDebit: Boolean, borderColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(18.dp)
            .background(borderColor, CircleShape)
            .padding(2.dp)
            .background(if (isDebit) Navy else Success, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (isDebit) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(9.dp),
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
