package com.edu.core.presentation.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.edu.core.presentation.designsystem.ClearSpendTheme

/** DESIGN_SPEC §7 Error: same layout as Empty, "Couldn't load your data" + "Try again". */
@Composable
fun ErrorScreen(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    message: String = stringResource(R.string.state_error_body),
) {
    EmptyStateScreen(
        title = stringResource(R.string.state_error_title),
        subtitle = message,
        icon = Icons.Outlined.CloudOff,
        ctaText = stringResource(R.string.state_error_retry),
        onCtaClick = onRetry,
        modifier = modifier,
    )
}

@Preview(widthDp = 412, heightDp = 500)
@Composable
private fun ErrorScreenPreview() {
    ClearSpendTheme { ErrorScreen(onRetry = {}) }
}
