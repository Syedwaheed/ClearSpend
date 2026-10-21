package com.edu.feature.dashboard.domain.usecase

import com.edu.feature.dashboard.domain.model.Greeting
import com.edu.feature.dashboard.domain.model.GreetingPeriod
import java.time.Clock
import java.time.LocalTime

/** Greeting by device time (05–11 morning, 12–17 afternoon, else evening) + first name. */
class GetGreetingUseCase(
    private val clock: Clock,
) {
    operator fun invoke(fullName: String?): Greeting {
        val hour = LocalTime.now(clock).hour
        val period = when (hour) {
            in 5..11 -> GreetingPeriod.MORNING
            in 12..17 -> GreetingPeriod.AFTERNOON
            else -> GreetingPeriod.EVENING
        }
        val firstName = fullName?.trim()?.split(Regex("\\s+"))?.firstOrNull()?.takeIf { it.isNotEmpty() }
        return Greeting(period, firstName)
    }
}
