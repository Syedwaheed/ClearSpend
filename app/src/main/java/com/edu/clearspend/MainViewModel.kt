package com.edu.clearspend

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.edu.clearspend.deeplink.PendingAuthRedirect
import kotlinx.coroutines.flow.StateFlow

/**
 * Holds an OAuth redirect between MainActivity receiving the deep link and the
 * Connecting screen taking it. Backed by SavedStateHandle so it survives
 * process death (CLAUDE.md §4 Rule 2). Not a screen, so not MVI.
 */
class MainViewModel(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val pendingRedirect: StateFlow<PendingAuthRedirect?> =
        savedStateHandle.getStateFlow(KEY_PENDING_REDIRECT, null)

    fun onRedirectReceived(redirect: PendingAuthRedirect) {
        savedStateHandle[KEY_PENDING_REDIRECT] = redirect
    }

    fun onRedirectConsumed() {
        savedStateHandle[KEY_PENDING_REDIRECT] = null
    }

    private companion object {
        const val KEY_PENDING_REDIRECT = "pending_auth_redirect"
    }
}
