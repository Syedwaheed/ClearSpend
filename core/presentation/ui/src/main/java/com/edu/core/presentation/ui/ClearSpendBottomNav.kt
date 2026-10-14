package com.edu.core.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.edu.core.presentation.designsystem.Blue
import com.edu.core.presentation.designsystem.Navy
import com.edu.core.presentation.designsystem.OnNavyMuted

private data class BottomNavTab(val icon: ImageVector, val label: String)

private val bottomNavTabs = listOf(
    BottomNavTab(Icons.Filled.Home, "Home"),
    BottomNavTab(Icons.Filled.CreditCard, "Accounts"),
    BottomNavTab(Icons.AutoMirrored.Filled.Send, "Send"),
    BottomNavTab(Icons.Filled.Receipt, "Transactions"),
    BottomNavTab(Icons.Filled.BarChart, "Analytics")
)

private const val CENTRE_TAB_INDEX = 2

@Composable
fun ClearSpendBottomNav(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Navy),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        bottomNavTabs.forEachIndexed { index, tab ->
            if (index == CENTRE_TAB_INDEX) {
                CentreTab(
                    tab = tab,
                    onClick = { onTabSelected(index) }
                )
            } else {
                RegularTab(
                    tab = tab,
                    isSelected = index == selectedTab,
                    onClick = { onTabSelected(index) }
                )
            }
        }
    }
}

@Composable
private fun RegularTab(
    tab: BottomNavTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = if (isSelected) Blue else OnNavyMuted
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = tab.label,
            tint = color
        )
        Text(
            text = tab.label,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
private fun CentreTab(
    tab: BottomNavTab,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(color = Blue, shape = CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.label,
                tint = Color.White
            )
        }
        Text(
            text = tab.label,
            style = MaterialTheme.typography.labelSmall,
            color = Blue
        )
    }
}
