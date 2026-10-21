package com.edu.core.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edu.core.presentation.designsystem.Blue
import com.edu.core.presentation.designsystem.BlueDark
import com.edu.core.presentation.designsystem.ClearSpendTheme
import com.edu.core.presentation.designsystem.Navy
import com.edu.core.presentation.designsystem.OnNavy
import com.edu.core.presentation.designsystem.RobotoMono
import com.edu.core.presentation.ui.model.AccountUi
import com.edu.core.presentation.ui.model.BalanceUi

/**
 * 178dp account card from the Home carousel (09). [highlighted] swaps the
 * navy surface for the blue gradient (the design alternates them).
 */
@Composable
fun AccountCard(
    account: AccountUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    balance: BalanceUi? = null,
    highlighted: Boolean = false,
) {
    val shape = RoundedCornerShape(18.dp)
    val background = if (highlighted) {
        Modifier.background(Brush.linearGradient(listOf(Blue, BlueDark)), shape)
    } else {
        Modifier.background(Navy, shape)
    }
    val muted = if (highlighted) Color.White.copy(alpha = 0.75f) else OnNavy.copy(alpha = 0.5f)
    val mono = if (highlighted) Color.White.copy(alpha = 0.85f) else OnNavy.copy(alpha = 0.7f)
    val faint = if (highlighted) Color.White.copy(alpha = 0.65f) else OnNavy.copy(alpha = 0.45f)
    Column(
        modifier = modifier
            .width(178.dp)
            .clip(shape)
            .then(background)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(16.dp),
    ) {
        Text(
            text = account.displayName,
            fontSize = 11.sp,
            color = muted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(16.dp))
        (account.maskedAccountNumber ?: account.maskedIban)?.let { masked ->
            Text(text = masked, fontSize = 12.sp, fontFamily = RobotoMono, color = mono)
            Spacer(Modifier.height(6.dp))
        }
        Text(
            text = balance?.formattedAvailable ?: "—",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
        Spacer(Modifier.height(8.dp))
        Row {
            account.formattedSortCode?.let { sortCode ->
                Text(
                    text = stringResource(R.string.account_card_sort_code, sortCode),
                    fontSize = 10.5.sp,
                    color = faint,
                )
            }
        }
    }
}

@Preview
@Composable
private fun AccountCardPreview() {
    ClearSpendTheme {
        Row {
            val account = AccountUi("a", "Transaction Account", "GBP", null, "•••• 0000", "01-21-31", "Mock", null, "CURRENT", 0)
            AccountCard(account, onClick = {}, balance = BalanceUi("a", "£44.01", "£2.00", null, false, ""))
            Spacer(Modifier.width(12.dp))
            AccountCard(account, onClick = {}, highlighted = true)
        }
    }
}
