package com.edu.feature.dashboard.presentation.mapper

import com.edu.core.presentation.ui.formatter.DateFormatter
import com.edu.feature.dashboard.domain.model.Dashboard
import com.edu.feature.dashboard.domain.model.GreetingPeriod
import com.edu.feature.dashboard.presentation.model.DashboardHeaderUi
import com.edu.feature.dashboard.presentation.model.GreetingPeriodUi

class DashboardHeaderUiMapperImpl(
    private val dateFormatter: DateFormatter,
) : DashboardHeaderUiMapper {
    override fun map(domain: Dashboard): DashboardHeaderUi = DashboardHeaderUi(
        period = when (domain.greeting.period) {
            GreetingPeriod.MORNING -> GreetingPeriodUi.MORNING
            GreetingPeriod.AFTERNOON -> GreetingPeriodUi.AFTERNOON
            GreetingPeriod.EVENING -> GreetingPeriodUi.EVENING
        },
        firstName = domain.greeting.firstName,
        avatarInitial = domain.greeting.firstName?.firstOrNull()?.uppercase() ?: "•",
        formattedDate = dateFormatter.formatLongDate(domain.today),
    )
}
