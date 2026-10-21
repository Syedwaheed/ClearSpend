package com.edu.feature.auth.presentation.mapper

import com.edu.core.presentation.ui.formatter.DateFormatter
import com.edu.feature.auth.domain.model.LoginProfile
import com.edu.feature.auth.presentation.model.WelcomeUi
import java.time.Clock
import java.time.Instant
import java.time.LocalDate

/**
 * First word of `full_name` (DESIGN_SPEC §4 Home greeting rule) and a
 * relative last-login label: "Today, 07:42", "Yesterday, 21:05" or
 * "16 Sep 2026, 14:32".
 */
class WelcomeUiMapperImpl(
    private val dateFormatter: DateFormatter,
    private val clock: Clock,
) : WelcomeUiMapper {

    override fun map(domain: LoginProfile): WelcomeUi {
        val firstName = domain.accountHolderName
            ?.trim()
            ?.split(Regex("\\s+"))
            ?.firstOrNull()
            ?.takeIf { it.isNotEmpty() }
        return WelcomeUi(
            firstName = firstName,
            avatarInitial = firstName?.first()?.uppercaseChar()?.toString(),
            lastLoginLabel = domain.lastLogin?.let(::lastLoginLabel),
        )
    }

    private fun lastLoginLabel(instant: Instant): String {
        val day = instant.atZone(clock.zone).toLocalDate()
        val today = LocalDate.now(clock)
        val dayLabel = when (day) {
            today -> "Today"
            today.minusDays(1) -> "Yesterday"
            else -> dateFormatter.formatDate(instant)
        }
        return "$dayLabel, ${dateFormatter.formatTime(instant)}"
    }
}
