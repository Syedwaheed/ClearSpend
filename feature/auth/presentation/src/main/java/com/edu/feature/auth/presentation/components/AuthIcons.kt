package com.edu.feature.auth.presentation.components

import androidx.compose.ui.graphics.Color
import com.edu.core.presentation.designsystem.Blue
import com.edu.core.presentation.designsystem.Mint

/** Icon geometry from designs/ClearSpend.dc.html (24×24 viewBox). */
internal object AuthIcons {
    private val SearchGrey = Color(0xFF9AA3AF)

    val shieldCheck = listOf(
        StrokePath("M12 2l8 3v6c0 5-3.5 8.5-8 10-4.5-1.5-8-5-8-10V5l8-3z", Blue, 1.8f),
        StrokePath("M8.5 12l2.3 2.3L16 9", Mint, 2f),
    )
    val phoneSignal = listOf(
        StrokePath("M9 2h6a2 2 0 012 2v14a2 2 0 01-2 2h-6a2 2 0 01-2-2v-14a2 2 0 012-2z", Blue, 1.6f),
        StrokePath("M3 12a9 9 0 0118 0", Mint, 1.6f),
    )
    val spendingClock = listOf(
        StrokePath("M4 12a8 8 0 1016 0a8 8 0 10-16 0", Mint, 1.6f),
        StrokePath("M12 4v8l6 3", Blue, 1.6f),
    )
    val sendArrow = listOf(
        StrokePath("M4 12h13M11 6l6 6-6 6", Blue, 1.8f),
    )
    val lock = listOf(
        StrokePath("M7 10h10a2 2 0 012 2v6a2 2 0 01-2 2h-10a2 2 0 01-2-2v-6a2 2 0 012-2z", Blue, 1.6f),
        StrokePath("M8 10V7a4 4 0 018 0v3", Blue, 1.6f),
    )
    val house = listOf(
        StrokePath("M3 10.5L12 4l9 6.5", Blue, 1.6f),
        StrokePath("M5 9.5V20h14V9.5", Blue, 1.6f),
    )
    val eye = listOf(
        StrokePath("M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6-10-6-10-6z", Blue, 1.6f),
        StrokePath("M9.5 12a2.5 2.5 0 105 0a2.5 2.5 0 10-5 0", Blue, 1.6f),
    )
    val fingerprint = listOf(
        StrokePath("M12 11a3 3 0 013 3v2a5 5 0 01-1 3", Blue, 1.6f),
        StrokePath("M8 12v2a7 7 0 001 3.6M6 9.5a6 6 0 0112 0v3", Blue, 1.6f),
        StrokePath("M12 11v4M9 11.5V14a3 3 0 003 3", Blue, 1.6f),
    )
    val search = listOf(
        StrokePath("M4 11a7 7 0 1014 0a7 7 0 10-14 0", SearchGrey, 2f),
        StrokePath("M21 21l-4.3-4.3", SearchGrey, 2f),
    )
    val chevronBack = listOf(
        StrokePath("M15 18l-6-6 6-6", Color(0xFF0A1628), 2f),
    )
}
