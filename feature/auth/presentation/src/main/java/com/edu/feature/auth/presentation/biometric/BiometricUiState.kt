package com.edu.feature.auth.presentation.biometric

import android.os.Parcelable
import com.edu.feature.auth.presentation.model.BiometricAvailabilityUi
import com.edu.feature.auth.presentation.model.UnlockFailureUi
import com.edu.feature.auth.presentation.model.WelcomeUi
import kotlinx.parcelize.Parcelize

@Parcelize
data class BiometricUiState(
    val isLoading: Boolean = false,
    val welcome: WelcomeUi = WelcomeUi(),
    val availability: BiometricAvailabilityUi = BiometricAvailabilityUi.UNKNOWN,
    val failure: UnlockFailureUi? = null,
) : Parcelable {
    val canUseBiometric: Boolean get() = availability == BiometricAvailabilityUi.BIOMETRIC_READY
    val canUseDeviceCredential: Boolean
        get() = availability == BiometricAvailabilityUi.BIOMETRIC_READY ||
            availability == BiometricAvailabilityUi.DEVICE_CREDENTIAL_ONLY
}
