package com.edu.feature.auth.presentation.biometric

import android.os.Build
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.edu.feature.auth.presentation.model.UnlockFailureUi
import com.edu.feature.auth.presentation.model.UnlockMethodUi

internal data class PromptTexts(val title: String, val subtitle: String, val cancel: String)

/** Shows androidx.biometric's BiometricPrompt and reports the outcome. */
internal fun showUnlockPrompt(
    activity: FragmentActivity,
    method: UnlockMethodUi,
    texts: PromptTexts,
    onSuccess: () -> Unit,
    onFailure: (UnlockFailureUi) -> Unit,
) {
    val prompt = BiometricPrompt(
        activity,
        ContextCompat.getMainExecutor(activity),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = onSuccess()

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onFailure(
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                        BiometricPrompt.ERROR_CANCELED -> UnlockFailureUi.CANCELLED
                        BiometricPrompt.ERROR_LOCKOUT,
                        BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> UnlockFailureUi.LOCKED_OUT
                        BiometricPrompt.ERROR_NO_BIOMETRICS,
                        BiometricPrompt.ERROR_HW_NOT_PRESENT,
                        BiometricPrompt.ERROR_HW_UNAVAILABLE,
                        BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL -> UnlockFailureUi.UNAVAILABLE
                        else -> UnlockFailureUi.OTHER
                    },
                )
            }
            // onAuthenticationFailed (a single unrecognised finger) keeps the prompt open.
        },
    )

    val builder = BiometricPrompt.PromptInfo.Builder()
        .setTitle(texts.title)
        .setSubtitle(texts.subtitle)
    when (method) {
        UnlockMethodUi.BIOMETRIC -> builder
            .setAllowedAuthenticators(BIOMETRIC_WEAK)
            .setNegativeButtonText(texts.cancel)
        UnlockMethodUi.DEVICE_CREDENTIAL ->
            // DEVICE_CREDENTIAL on its own is only supported from API 30.
            builder.setAllowedAuthenticators(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) DEVICE_CREDENTIAL else BIOMETRIC_WEAK or DEVICE_CREDENTIAL,
            )
    }
    prompt.authenticate(builder.build())
}
