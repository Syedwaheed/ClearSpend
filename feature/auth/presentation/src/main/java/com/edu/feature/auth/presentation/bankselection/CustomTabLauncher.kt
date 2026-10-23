package com.edu.feature.auth.presentation.bankselection

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import com.edu.core.presentation.designsystem.Navy
import androidx.compose.ui.graphics.toArgb

/**
 * Opens the TrueLayer auth link in a Chrome Custom Tab (CLAUDE.md §8 Step 2),
 * falling back to any browser. Returns false if nothing can open it.
 */
internal fun launchAuthCustomTab(context: Context, url: String): Boolean {
    val uri = url.toUri()
    return try {
        CustomTabsIntent.Builder()
            .setShowTitle(true)
            .setDefaultColorSchemeParams(
                CustomTabColorSchemeParams.Builder().setToolbarColor(Navy.toArgb()).build(),
            )
            .build()
            .launchUrl(context, uri)
        true
    } catch (e: ActivityNotFoundException) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }
}
