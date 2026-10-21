package com.edu.clearspend

import androidx.lifecycle.SavedStateHandle
import com.edu.clearspend.deeplink.PendingAuthRedirect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MainViewModelTest {

    private val redirect = PendingAuthRedirect("code", "state", null)

    @Test
    fun `Should hold a received redirect`() {
        val viewModel = MainViewModel(SavedStateHandle())

        viewModel.onRedirectReceived(redirect)

        assertEquals(redirect, viewModel.pendingRedirect.value)
    }

    @Test
    fun `Should drop the redirect once consumed`() {
        val viewModel = MainViewModel(SavedStateHandle())
        viewModel.onRedirectReceived(redirect)

        viewModel.onRedirectConsumed()

        assertNull(viewModel.pendingRedirect.value)
    }

    @Test
    fun `Should restore a pending redirect After process death`() {
        val viewModel = MainViewModel(SavedStateHandle(mapOf("pending_auth_redirect" to redirect)))

        assertEquals(redirect, viewModel.pendingRedirect.value)
    }
}
