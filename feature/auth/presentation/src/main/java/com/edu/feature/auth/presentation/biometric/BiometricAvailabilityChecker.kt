package com.edu.feature.auth.presentation.biometric

import android.app.KeyguardManager
import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import com.edu.feature.auth.presentation.model.BiometricAvailabilityUi

/** Abstracts the platform check so the ViewModel stays unit-testable. */
fun interface BiometricAvailabilityChecker {
    fun check(): BiometricAvailabilityUi
}

class AndroidBiometricAvailabilityChecker(
    private val context: Context,
) : BiometricAvailabilityChecker {
    override fun check(): BiometricAvailabilityUi {
        val biometrics = BiometricManager.from(context).canAuthenticate(BIOMETRIC_WEAK)
        if (biometrics == BiometricManager.BIOMETRIC_SUCCESS) return BiometricAvailabilityUi.BIOMETRIC_READY
        val keyguard = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        return if (keyguard?.isDeviceSecure == true) {
            BiometricAvailabilityUi.DEVICE_CREDENTIAL_ONLY
        } else {
            BiometricAvailabilityUi.NONE
        }
    }
}
