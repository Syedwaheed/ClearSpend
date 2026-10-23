package com.edu.feature.dashboard.presentation.home

import androidx.lifecycle.SavedStateHandle
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.onError
import com.edu.core.domain.util.onSuccess
import com.edu.core.presentation.ui.state.MviViewModel
import com.edu.feature.dashboard.domain.usecase.GetDashboardUseCase
import com.edu.feature.dashboard.presentation.mapper.DashboardContentUiMapper
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Home (09). [GetDashboardUseCase] does the parallel loading and every
 * derivation; this ViewModel only sequences intents, maps through the
 * injected [DashboardContentUiMapper] and emits partial states.
 */
class DashboardViewModel(
    savedStateHandle: SavedStateHandle,
    initialState: DashboardUiState,
    private val getDashboard: GetDashboardUseCase,
    private val contentMapper: DashboardContentUiMapper,
) : MviViewModel<DashboardUiState, DashboardPartialState, DashboardAction>(
    savedStateHandle = savedStateHandle,
    initialState = initialState,
) {

    private val _effect = Channel<DashboardEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        if (!isStateRestored) {
            onAction(DashboardAction.LoadData)
        }
    }

    override fun handleAction(action: DashboardAction): Flow<DashboardPartialState> = when (action) {
        DashboardAction.LoadData, DashboardAction.RetryClicked -> load(asRefresh = false)
        DashboardAction.RefreshData -> load(asRefresh = true)
        DashboardAction.AvatarClicked -> sendEffect(DashboardEffect.NavigateToSettings)
        DashboardAction.NotificationsClicked -> sendEffect(DashboardEffect.NavigateToNotifications)
        is DashboardAction.AccountSelected -> sendEffect(DashboardEffect.NavigateToAccount(action.accountId))
        DashboardAction.ConnectAnotherBankClicked -> sendEffect(DashboardEffect.NavigateToBankSelection)
        DashboardAction.SendClicked -> sendEffect(DashboardEffect.OpenSendMoney)
        DashboardAction.RequestClicked, DashboardAction.StatementsClicked -> sendEffect(DashboardEffect.ShowComingSoon)
        DashboardAction.AnalyticsClicked -> sendEffect(DashboardEffect.NavigateToAnalytics)
        DashboardAction.PendingClicked, DashboardAction.SeeAllTransactionsClicked ->
            sendEffect(DashboardEffect.NavigateToTransactions)
        DashboardAction.UpcomingPaymentsClicked -> sendEffect(DashboardEffect.NavigateToBills)
        DashboardAction.CardClicked -> sendEffect(DashboardEffect.NavigateToCard)
        is DashboardAction.TransactionSelected -> sendEffect(DashboardEffect.NavigateToTransaction(action.transactionId))
    }

    override fun reduceUiState(
        previousState: DashboardUiState,
        partialState: DashboardPartialState,
    ): DashboardUiState = when (partialState) {
        is DashboardPartialState.LoadingChanged -> previousState.copy(isLoading = partialState.isLoading, hasError = false)
        is DashboardPartialState.RefreshingChanged -> previousState.copy(isRefreshing = partialState.isRefreshing)
        is DashboardPartialState.ContentLoaded -> previousState.copy(
            content = partialState.content,
            isLoading = false,
            isRefreshing = false,
            hasError = false,
        )
        DashboardPartialState.LoadFailed -> previousState.copy(
            isLoading = false,
            isRefreshing = false,
            // Keep showing what we have if a refresh fails; only error out when there is nothing.
            hasError = previousState.content == null,
        )
    }

    private fun sendEffect(effect: DashboardEffect): Flow<DashboardPartialState> = flow {
        _effect.send(effect)
    }

    private fun load(asRefresh: Boolean): Flow<DashboardPartialState> = flow {
        val refreshing = asRefresh && uiState.value.content != null
        emit(if (refreshing) DashboardPartialState.RefreshingChanged(true) else DashboardPartialState.LoadingChanged(true))
        getDashboard()
            .onSuccess { dashboard -> emit(DashboardPartialState.ContentLoaded(contentMapper.map(dashboard))) }
            .onError { error ->
                if (error == DataError.NetworkError.UNAUTHORIZED) _effect.send(DashboardEffect.NavigateToConnectBank)
                emit(DashboardPartialState.LoadFailed)
            }
    }
}
