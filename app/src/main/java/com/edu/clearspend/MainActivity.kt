package com.edu.clearspend

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.edu.clearspend.deeplink.AuthRedirectParser
import com.edu.clearspend.navigation.ClearSpendNavHost
import com.edu.core.presentation.designsystem.ClearSpendTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Single activity. FragmentActivity because BiometricPrompt (screen 08) needs
 * one. Catches `clearspend://callback` and hands it to Connecting (07), whose
 * ViewModel validates `state` before exchanging the code.
 */
class MainActivity : FragmentActivity() {

    private val mainViewModel: MainViewModel by viewModel()

    /** Hash of the last redirect handled, so a recreated activity never replays it. */
    private var handledRedirectHash: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        handledRedirectHash = savedInstanceState?.takeIf { it.containsKey(KEY_HANDLED) }?.getInt(KEY_HANDLED)
        handleRedirect(intent)

        setContent {
            ClearSpendTheme(darkTheme = false) {
                val pendingRedirect by mainViewModel.pendingRedirect.collectAsStateWithLifecycle()
                ClearSpendNavHost(
                    pendingRedirect = pendingRedirect,
                    onRedirectConsumed = mainViewModel::onRedirectConsumed,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleRedirect(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        handledRedirectHash?.let { outState.putInt(KEY_HANDLED, it) }
    }

    private fun handleRedirect(intent: Intent?) {
        val data = intent?.dataString ?: return
        val hash = data.hashCode()
        if (hash == handledRedirectHash) return
        val redirect = AuthRedirectParser.parse(data) ?: return
        handledRedirectHash = hash
        mainViewModel.onRedirectReceived(redirect)
    }

    private companion object {
        const val KEY_HANDLED = "handled_redirect_hash"
    }
}
