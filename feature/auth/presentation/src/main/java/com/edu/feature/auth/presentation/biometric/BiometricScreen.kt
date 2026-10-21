package com.edu.feature.auth.presentation.biometric

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.edu.core.presentation.designsystem.Blue
import com.edu.core.presentation.designsystem.ClearSpendTheme
import com.edu.core.presentation.designsystem.Navy
import com.edu.core.presentation.designsystem.Navy2
import com.edu.core.presentation.designsystem.OnNavy
import com.edu.core.presentation.designsystem.OnNavyMuted
import com.edu.core.presentation.designsystem.Warning
import com.edu.core.presentation.ui.ObserveAsEvent
import com.edu.feature.auth.presentation.R
import com.edu.feature.auth.presentation.components.AuthIcons
import com.edu.feature.auth.presentation.components.StrokeIcon
import com.edu.feature.auth.presentation.model.BiometricAvailabilityUi
import com.edu.feature.auth.presentation.model.UnlockFailureUi
import com.edu.feature.auth.presentation.model.WelcomeUi
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BiometricScreen(
    onNavigateToDashboard: () -> Unit,
    onNavigateToConnectBank: () -> Unit,
    viewModel: BiometricViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current as? FragmentActivity
    val texts = PromptTexts(
        title = stringResource(R.string.biometric_prompt_title),
        subtitle = stringResource(R.string.biometric_prompt_subtitle),
        cancel = stringResource(R.string.biometric_prompt_cancel),
    )
    ObserveAsEvent(viewModel.effect) { effect ->
        when (effect) {
            is BiometricEffect.ShowPrompt -> if (activity != null) {
                showUnlockPrompt(
                    activity = activity,
                    method = effect.method,
                    texts = texts,
                    onSuccess = { viewModel.onAction(BiometricAction.AuthenticationSucceeded) },
                    onFailure = { viewModel.onAction(BiometricAction.AuthenticationFailed(it)) },
                )
            } else {
                viewModel.onAction(BiometricAction.AuthenticationFailed(UnlockFailureUi.UNAVAILABLE))
            }
            BiometricEffect.NavigateToDashboard -> onNavigateToDashboard()
            BiometricEffect.NavigateToConnectBank -> onNavigateToConnectBank()
        }
    }
    BiometricContent(state = state, onAction = viewModel::onAction)
}

@Composable
fun BiometricContent(
    state: BiometricUiState,
    onAction: (BiometricAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy)
            .safeDrawingPadding()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        Avatar(state.welcome)
        Spacer(Modifier.height(18.dp))
        Text(
            text = state.welcome.firstName?.let { stringResource(R.string.biometric_welcome_named, it) }
                ?: stringResource(R.string.biometric_welcome),
            color = OnNavy,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        state.welcome.lastLoginLabel?.let {
            Spacer(Modifier.height(6.dp))
            Text(stringResource(R.string.biometric_last_login, it), color = OnNavy.copy(alpha = 0.5f), fontSize = 12.sp)
        }
        Spacer(Modifier.height(64.dp))

        when (state.availability) {
            BiometricAvailabilityUi.BIOMETRIC_READY,
            BiometricAvailabilityUi.UNKNOWN -> UnlockButton(
                label = stringResource(R.string.biometric_touch_to_unlock),
                onClick = { onAction(BiometricAction.UnlockClicked) },
            )
            BiometricAvailabilityUi.DEVICE_CREDENTIAL_ONLY -> UnlockButton(
                label = stringResource(R.string.biometric_tap_to_unlock_pin),
                onClick = { onAction(BiometricAction.UsePinClicked) },
            )
            BiometricAvailabilityUi.NONE -> Text(
                text = stringResource(R.string.biometric_no_lock_screen),
                color = OnNavyMuted,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                textAlign = TextAlign.Center,
            )
        }

        failureMessage(state.failure)?.let {
            Spacer(Modifier.height(12.dp))
            Text(stringResource(it), color = Warning, fontSize = 12.sp, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(28.dp))
        if (state.canUseBiometric) {
            TextButton(onClick = { onAction(BiometricAction.UsePinClicked) }) {
                Text(stringResource(R.string.biometric_use_pin), color = OnNavyMuted, fontSize = 13.sp)
            }
        }
        TextButton(onClick = { onAction(BiometricAction.SignInDifferentlyClicked) }) {
            Text(stringResource(R.string.biometric_sign_in_differently), color = Blue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun Avatar(welcome: WelcomeUi) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .background(Navy2, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        val initial = welcome.avatarInitial
        if (initial != null) {
            Text(initial, color = Blue, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        } else {
            StrokeIcon(paths = AuthIcons.shieldCheck, size = 28.dp)
        }
    }
}

/** 76dp ring with a soft pulsing halo (prototype `cs-pulse`). */
@Composable
private fun UnlockButton(label: String, onClick: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val halo by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2_000), RepeatMode.Restart),
        label = "halo",
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(104.dp)
                .drawBehind {
                    drawCircle(
                        color = Blue.copy(alpha = 0.15f * (1f - halo)),
                        radius = size.minDimension / 2f * (0.75f + 0.25f * halo),
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .background(Blue.copy(alpha = 0.15f), CircleShape)
                    .border(2.dp, Blue, CircleShape)
                    .clickable(role = Role.Button, onClickLabel = label, onClick = onClick),
                contentAlignment = Alignment.Center,
            ) { StrokeIcon(paths = AuthIcons.fingerprint, size = 32.dp) }
        }
        Spacer(Modifier.height(4.dp))
        Text(label, color = OnNavy, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

private fun failureMessage(failure: UnlockFailureUi?): Int? = when (failure) {
    UnlockFailureUi.LOCKED_OUT -> R.string.biometric_locked_out
    UnlockFailureUi.OTHER, UnlockFailureUi.UNAVAILABLE -> R.string.biometric_failed
    UnlockFailureUi.CANCELLED, null -> null
}

@Preview(widthDp = 412, heightDp = 892)
@Composable
private fun BiometricReadyPreview() {
    ClearSpendTheme {
        BiometricContent(
            BiometricUiState(
                welcome = WelcomeUi(firstName = "Alex", avatarInitial = "A", lastLoginLabel = "Today, 07:42"),
                availability = BiometricAvailabilityUi.BIOMETRIC_READY,
            ),
            onAction = {},
        )
    }
}

@Preview(widthDp = 412, heightDp = 892)
@Composable
private fun BiometricNoLockPreview() {
    ClearSpendTheme {
        BiometricContent(BiometricUiState(availability = BiometricAvailabilityUi.NONE), onAction = {})
    }
}
