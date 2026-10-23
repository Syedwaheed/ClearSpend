package com.edu.feature.dashboard.presentation.di

import com.edu.feature.dashboard.presentation.home.DashboardUiState
import com.edu.feature.dashboard.presentation.home.DashboardViewModel
import com.edu.feature.dashboard.presentation.mapper.CashFlowUiMapper
import com.edu.feature.dashboard.presentation.mapper.CashFlowUiMapperImpl
import com.edu.feature.dashboard.presentation.mapper.CategorySpendUiMapper
import com.edu.feature.dashboard.presentation.mapper.CategorySpendUiMapperImpl
import com.edu.feature.dashboard.presentation.mapper.DashboardContentUiMapper
import com.edu.feature.dashboard.presentation.mapper.DashboardContentUiMapperImpl
import com.edu.feature.dashboard.presentation.mapper.DashboardHeaderUiMapper
import com.edu.feature.dashboard.presentation.mapper.DashboardHeaderUiMapperImpl
import com.edu.feature.dashboard.presentation.mapper.PendingUiMapper
import com.edu.feature.dashboard.presentation.mapper.PendingUiMapperImpl
import com.edu.feature.dashboard.presentation.mapper.SafeToSpendUiMapper
import com.edu.feature.dashboard.presentation.mapper.SafeToSpendUiMapperImpl
import com.edu.feature.dashboard.presentation.mapper.UpcomingPaymentUiMapper
import com.edu.feature.dashboard.presentation.mapper.UpcomingPaymentUiMapperImpl
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Home screen. Use cases come from :app's appModule; shared formatters and
 * mappers from coreUiModule.
 */
val dashboardPresentationModule = module {
    factory { DashboardUiState() }

    factory<DashboardHeaderUiMapper> { DashboardHeaderUiMapperImpl(dateFormatter = get()) }
    factory<SafeToSpendUiMapper> { SafeToSpendUiMapperImpl(amountFormatter = get()) }
    factory<CashFlowUiMapper> { CashFlowUiMapperImpl(amountFormatter = get(), dateFormatter = get()) }
    factory<PendingUiMapper> { PendingUiMapperImpl(amountFormatter = get()) }
    factory<UpcomingPaymentUiMapper> { UpcomingPaymentUiMapperImpl(amountFormatter = get()) }
    factory<CategorySpendUiMapper> { CategorySpendUiMapperImpl(amountFormatter = get()) }
    factory<DashboardContentUiMapper> {
        DashboardContentUiMapperImpl(
            headerMapper = get(),
            accountMapper = get(),
            balanceMapper = get(),
            safeToSpendMapper = get(),
            cashFlowMapper = get(),
            pendingMapper = get(),
            upcomingPaymentMapper = get(),
            categorySpendMapper = get(),
            cardSummaryMapper = get(),
            transactionMapper = get(),
            syncTimeMapper = get(),
        )
    }

    viewModel {
        DashboardViewModel(
            savedStateHandle = get(),
            initialState = get(),
            getDashboard = get(),
            contentMapper = get(),
        )
    }
}
