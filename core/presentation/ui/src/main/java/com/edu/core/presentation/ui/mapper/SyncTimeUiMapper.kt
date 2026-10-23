package com.edu.core.presentation.ui.mapper

import java.time.Instant

/** Last-sync [Instant] → the time shown in the offline banner ("14:32"). */
interface SyncTimeUiMapper : UiMapper<Instant, String>
