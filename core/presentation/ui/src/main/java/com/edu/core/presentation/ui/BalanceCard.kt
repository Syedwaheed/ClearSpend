package com.edu.core.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edu.core.presentation.designsystem.Blue
import com.edu.core.presentation.designsystem.BlueDark
import com.edu.core.presentation.designsystem.ClearSpendTheme
import com.edu.core.presentation.designsystem.Mint
import com.edu.core.presentation.designsystem.heroCardShape
import com.edu.core.presentation.ui.model.BalanceUi

/** Blue hero card on Home (09): available balance, current, overdraft, "Updated …". */
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
            .padding(22.dp),
    ) {
        Text(
            text = stringResource(R.string.balance_card_total_available),
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.75f),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = balance.formattedAvailable,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-1).sp,
            color = Color.White,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.balance_card_current, balance.formattedCurrent),
            fontSize = 12.5.sp,
            color = Color.White.copy(alpha = 0.85f),
        )
        if (balance.hasOverdraft && balance.formattedOverdraft != null) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.balance_card_overdraft, balance.formattedOverdraft),
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Mint,
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.balance_card_updated, balance.formattedUpdatedAgo),
            fontSize = 10.5.sp,
            color = Color.White.copy(alpha = 0.55f),
        )
    }
}

@Preview
@Composable
private fun BalanceCardPreview() {
    ClearSpendTheme {
        BalanceCard(
            BalanceUi("a", "£44.01", "£2.00", "£100.00", true, "16 Sep 2026", "£100.00", "just now"),
        )
    }
}
