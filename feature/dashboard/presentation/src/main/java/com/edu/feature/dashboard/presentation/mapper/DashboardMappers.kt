package com.edu.feature.dashboard.presentation.mapper

import com.edu.core.presentation.ui.mapper.UiMapper
import com.edu.feature.dashboard.domain.model.CategorySpend
import com.edu.feature.dashboard.domain.model.Dashboard
import com.edu.feature.dashboard.domain.model.MonthlyCashFlow
import com.edu.feature.dashboard.domain.model.PendingSummary
import com.edu.feature.dashboard.domain.model.SafeToSpend
import com.edu.feature.dashboard.domain.model.UpcomingPayment
import com.edu.feature.dashboard.presentation.model.CashFlowUi
import com.edu.feature.dashboard.presentation.model.CategorySpendUi
import com.edu.feature.dashboard.presentation.model.DashboardContentUi
import com.edu.feature.dashboard.presentation.model.DashboardHeaderUi
import com.edu.feature.dashboard.presentation.model.PendingUi
import com.edu.feature.dashboard.presentation.model.SafeToSpendUi
import com.edu.feature.dashboard.presentation.model.UpcomingPaymentUi

interface DashboardHeaderUiMapper : UiMapper<Dashboard, DashboardHeaderUi>
interface SafeToSpendUiMapper : UiMapper<SafeToSpend, SafeToSpendUi>
interface CashFlowUiMapper : UiMapper<MonthlyCashFlow, CashFlowUi>
interface PendingUiMapper : UiMapper<PendingSummary, PendingUi>
interface UpcomingPaymentUiMapper : UiMapper<UpcomingPayment, UpcomingPaymentUi>
interface CategorySpendUiMapper : UiMapper<CategorySpend, CategorySpendUi>

/** Composes the per-model mappers into the whole Home screen. */
interface DashboardContentUiMapper : UiMapper<Dashboard, DashboardContentUi>
