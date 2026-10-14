package com.edu.core.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.edu.core.presentation.designsystem.Blue
import com.edu.core.presentation.designsystem.Navy
import com.edu.core.presentation.designsystem.Spacing
import com.edu.core.presentation.designsystem.OnNavy

@Composable
fun ClearSpendTopBar(
    userName: String,
    onNotificationClick: () -> Unit,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Navy)
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color = Blue, shape = CircleShape)
                .clickable(onClick = onAvatarClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initialsFor(userName),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = OnNavy
            )
        }

        Text(
            text = "Good morning, $userName",
            style = MaterialTheme.typography.titleMedium,
            color = OnNavy
        )

        Icon(
            imageVector = Icons.Filled.Notifications,
            contentDescription = "Notifications",
            tint = OnNavy,
            modifier = Modifier
                .size(28.dp)
                .clickable(onClick = onNotificationClick)
        )
    }
}

private fun initialsFor(name: String): String {
    return name.trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
}
