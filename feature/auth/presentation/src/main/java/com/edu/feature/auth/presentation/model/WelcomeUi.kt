package com.edu.feature.auth.presentation.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** Display-ready greeting for Biometric Login (08). Null fields hide their line. */
@Parcelize
data class WelcomeUi(
    val firstName: String? = null,
    val avatarInitial: String? = null,
    /** e.g. "Today, 07:42" — the screen adds the "Last login:" prefix. */
    val lastLoginLabel: String? = null,
) : Parcelable
