package com.edu.feature.transactions.presentation.detail

import androidx.lifecycle.SavedStateHandle
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.onError
import com.edu.core.domain.util.onSuccess
import com.edu.core.presentation.ui.state.MviViewModel
import com.edu.feature.transactions.domain.usecase.GetTransactionDetailUseCase
import com.edu.feature.transactions.presentation.mapper.TransactionDetailUiMapper
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Transaction Detail (14-16). Opened with the `transaction_id` only; the
 * transaction itself is loaded from the repository cache.
 */
class TransactionDetailViewModel(
    savedStateHandle: SavedStateHandle,
    initialState: TransactionDetailUiState,
    private val transactionId: String,
    private val getTransactionDetail: GetTransactionDetailUseCase,
    private val detailMapper: TransactionDetailUiMapper,
) : MviViewModel<TransactionDetailUiState, TransactionDetailPartialState, TransactionDetailAction>(
    savedStateHandle = savedStateHandle,
    initialState = initialState,
) {

    private val _effect = Channel<TransactionDetailEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        // Skip the initial load after process death unless there was nothing to restore.
        if (!isStateRestored || uiState.value.detail == null) onAction(TransactionDetailAction.LoadData)
    }

    override fun handleAction(action: TransactionDetailAction): Flow<TransactionDetailPartialState> = when (action) {
        TransactionDetailAction.LoadData, TransactionDetailAction.RetryClicked -> load()
        TransactionDetailAction.BackClicked -> sendEffect(TransactionDetailEffect.NavigateBack)
        TransactionDetailAction.CopyIdClicked -> flow {
            val id = uiState.value.detail?.transactionId ?: return@flow
            _effect.send(TransactionDetailEffect.CopyToClipboard(LABEL_TRANSACTION_ID, id))
            emit(TransactionDetailPartialState.IdCopiedChanged(true))
        }
        TransactionDetailAction.CopiedFeedbackElapsed -> flowOf(TransactionDetailPartialState.IdCopiedChanged(false))
        TransactionDetailAction.ManageInBillsClicked -> flow {
            if (uiState.value.detail?.showManageInBills == true) _effect.send(TransactionDetailEffect.NavigateToBills)
        }
        TransactionDetailAction.ReportIssueClicked -> sendEffect(TransactionDetailEffect.ShowReportIssueUnavailable)
    }

    override fun reduceUiState(
        previousState: TransactionDetailUiState,
        partialState: TransactionDetailPartialState,
    ): TransactionDetailUiState = when (partialState) {
        TransactionDetailPartialState.Loading -> previousState.copy(isLoading = true, hasError = false)
        is TransactionDetailPartialState.DetailLoaded ->
            previousState.copy(isLoading = false, hasError = false, detail = partialState.detail)
        TransactionDetailPartialState.LoadFailed ->
            previousState.copy(isLoading = false, hasError = previousState.detail == null)
        is TransactionDetailPartialState.IdCopiedChanged -> previousState.copy(isIdCopied = partialState.copied)
    }

    private fun sendEffect(effect: TransactionDetailEffect): Flow<TransactionDetailPartialState> = flow { _effect.send(effect) }

    private fun load(): Flow<TransactionDetailPartialState> = flow {
        emit(TransactionDetailPartialState.Loading)
        getTransactionDetail(transactionId)
            .onSuccess { detail -> emit(TransactionDetailPartialState.DetailLoaded(detailMapper.map(detail))) }
            .onError { error ->
                if (error == DataError.NetworkError.UNAUTHORIZED) _effect.send(TransactionDetailEffect.NavigateToConnectBank)
                emit(TransactionDetailPartialState.LoadFailed)
            }
    }

    private companion object {
        const val LABEL_TRANSACTION_ID = "Transaction ID"
    }
}
