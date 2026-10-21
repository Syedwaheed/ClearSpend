package com.edu.core.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edu.core.presentation.designsystem.Blue
import com.edu.core.presentation.designsystem.ClearSpendTheme
import com.edu.core.presentation.designsystem.Navy
import com.edu.core.presentation.designsystem.Outline
import com.edu.core.presentation.designsystem.Surface

private data class BottomNavTab(val icon: ImageVector, val labelRes: Int)

private val bottomNavTabs = listOf(
    BottomNavTab(Icons.Outlined.Home, R.string.nav_home),
    BottomNavTab(Icons.Outlined.WorkOutline, R.string.nav_accounts),
    BottomNavTab(Icons.Filled.Add, R.string.nav_send),
    BottomNavTab(Icons.Outlined.SwapHoriz, R.string.nav_transactions),
    BottomNavTab(Icons.Outlined.BarChart, R.string.nav_analytics),
)

private const val CENTRE_TAB_INDEX = 2
private val InactiveGrey = Color(0xFF9AA3AF)

/**
 * Floating pill bottom nav (DESIGN_SPEC §2 / BottomNav.dc.html): 26dp radius,
 * 12dp side margin, raised 52dp Send FAB in the centre, active tab = blue
 * icon + label on a 10 % blue pill. Total footprint ≈ 80dp.
 */
@Composable
fun ClearSpendBottomNav(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 14.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 14.dp, shape = RoundedCornerShape(26.dp), spotColor = Navy.copy(alpha = 0.25f))
                .background(Surface, RoundedCornerShape(26.dp))
                .border(1.dp, Outline, RoundedCornerShape(26.dp))
                .padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            bottomNavTabs.forEachIndexed { index, tab ->
                if (index == CENTRE_TAB_INDEX) {
                    SendFab(tab = tab, onClick = { onTabSelected(index) })
                } else {
                    RegularTab(
                        tab = tab,
                        isSelected = index == selectedTab,
                        onClick = { onTabSelected(index) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun RegularTab(
    tab: BottomNavTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = if (isSelected) Blue else InactiveGrey
    val label = stringResource(tab.labelRes)
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .background(if (isSelected) Blue.copy(alpha = 0.1f) else Color.Transparent, RoundedCornerShape(20.dp))
                .clickable(role = Role.Tab, onClick = onClick)
                .semantics { selected = isSelected }
                .padding(horizontal = if (isSelected) 16.dp else 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(imageVector = tab.icon, contentDescription = null, tint = color, modifier = Modifier.size(21.dp))
            Spacer(Modifier.height(3.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = color,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun SendFab(tab: BottomNavTab, onClick: () -> Unit) {
    val label = stringResource(tab.labelRes)
    Box(
        modifier = Modifier
            .offset(y = (-22).dp)
            .shadow(elevation = 10.dp, shape = CircleShape, spotColor = Blue.copy(alpha = 0.5f))
            .size(52.dp)
            .background(Blue, CircleShape)
            .clickable(role = Role.Button, onClickLabel = label, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(imageVector = tab.icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(24.dp))
    }
}

@Preview(widthDp = 412)
@Composable
private fun BottomNavPreview() {
    ClearSpendTheme { ClearSpendBottomNav(selectedTab = 0, onTabSelected = {}) }
}
