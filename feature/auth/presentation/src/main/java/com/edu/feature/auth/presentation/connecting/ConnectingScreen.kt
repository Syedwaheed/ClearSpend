package com.edu.feature.auth.presentation.connecting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.edu.core.presentation.designsystem.Blue
import com.edu.core.presentation.designsystem.ClearSpendTheme
import com.edu.core.presentation.designsystem.Error
import com.edu.core.presentation.designsystem.Navy
import com.edu.core.presentation.designsystem.Navy2
import com.edu.core.presentation.designsystem.OnNavy
import com.edu.core.presentation.designsystem.OnNavyMuted
import com.edu.core.presentation.ui.ObserveAsEvent
import com.edu.feature.auth.presentation.R
import com.edu.feature.auth.presentation.components.PrimaryPillButton
import com.edu.feature.auth.presentation.model.ConnectionErrorUi
import org.koin.compose.viewmodel.koinViewModel

/**
 * @param redirect the deep-link parameters, or null once they were handed over.
 * @param onRedirectConsumed called right after [redirect] is forwarded to the ViewModel.
 */
@Composable
fun ConnectingScreen(
    redirect: AuthRedirectParams?,
    onRedirectConsumed: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToConnectBank: () -> Unit,
    viewModel: ConnectingViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(redirect) {
        if (redirect != null) {
            viewModel.onAction(ConnectingAction.RedirectReceived(redirect.code, redirect.state, redirect.error))
            onRedirectConsumed()
        }
    }
    ObserveAsEvent(viewModel.effect) { effect ->
        when (effect) {
            ConnectingEffect.NavigateToDashboard -> onNavigateToDashboard()
            ConnectingEffect.NavigateToConnectBank -> onNavigateToConnectBank()
        }
    }
    ConnectingContent(state = state, onAction = viewModel::onAction)
}

/** Raw redirect parameters handed over by :app (never logged). */
data class AuthRedirectParams(val code: String?, val state: String?, val error: String?) {
    override fun toString(): String = "AuthRedirectParams(code=***, state=***, error=$error)"
}

@Composable
fun ConnectingContent(
    state: ConnectingUiState,
    onAction: (ConnectingAction) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy)
            .safeDrawingPadding()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (state.status == ConnectingStatus.FAILED) {
            FailedContent(state = state, onAction = onAction)
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(
                    color = Blue,
                    trackColor = Blue.copy(alpha = 0.2f),
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(44.dp),
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    text = stringResource(
                        if (state.status == ConnectingStatus.CONNECTED) R.string.connecting_connected else R.string.connecting_title,
                    ),
                    color = OnNavy,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(6.dp))
                Text(stringResource(R.string.connecting_subtitle), color = OnNavy.copy(alpha = 0.5f), fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun FailedContent(state: ConnectingUiState, onAction: (ConnectingAction) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(Navy2, CircleShape),
            contentAlignment = Alignment.Center,
        ) { Text("!", color = Error, fontSize = 26.sp, fontWeight = FontWeight.Bold) }
        Spacer(Modifier.height(20.dp))
        Text(stringResource(R.string.connecting_error_title), color = OnNavy, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(errorMessage(state.error)),
            color = OnNavyMuted,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(28.dp))
        if (state.canRetry) {
            PrimaryPillButton(
                text = stringResource(R.string.error_try_again),
                onClick = { onAction(ConnectingAction.RetryClicked) },
            )
            Spacer(Modifier.height(6.dp))
            TextButton(onClick = { onAction(ConnectingAction.StartAgainClicked) }) {
                Text(stringResource(R.string.connecting_start_again), color = Blue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        } else {
            PrimaryPillButton(
                text = stringResource(R.string.connecting_start_again),
                onClick = { onAction(ConnectingAction.StartAgainClicked) },
            )
        }
    }
}

private fun errorMessage(error: ConnectionErrorUi?): Int = when (error) {
    ConnectionErrorUi.SECURITY_CHECK_FAILED -> R.string.connecting_error_security
    ConnectionErrorUi.CANCELLED -> R.string.connecting_error_cancelled
    ConnectionErrorUi.CODE_REJECTED -> R.string.connecting_error_rejected
    ConnectionErrorUi.NO_INTERNET -> R.string.connecting_error_no_internet
    ConnectionErrorUi.TIMEOUT -> R.string.connecting_error_timeout
    ConnectionErrorUi.SERVER -> R.string.connecting_error_server
    ConnectionErrorUi.UNKNOWN, null -> R.string.connecting_error_unknown
}

@Preview(widthDp = 412, heightDp = 892)
@Composable
private fun ConnectingPreview() {
    ClearSpendTheme { ConnectingContent(ConnectingUiState(status = ConnectingStatus.EXCHANGING), onAction = {}) }
}

@Preview(widthDp = 412, heightDp = 892)
@Composable
private fun ConnectingRetryableErrorPreview() {
    ClearSpendTheme {
        ConnectingContent(
            ConnectingUiState(code = "c", status = ConnectingStatus.FAILED, error = ConnectionErrorUi.NO_INTERNET),
            onAction = {},
        )
    }
}

@Preview(widthDp = 412, heightDp = 892)
@Composable
private fun ConnectingSecurityErrorPreview() {
    ClearSpendTheme {
        ConnectingContent(
            ConnectingUiState(status = ConnectingStatus.FAILED, error = ConnectionErrorUi.SECURITY_CHECK_FAILED),
            onAction = {},
        )
    }
}
