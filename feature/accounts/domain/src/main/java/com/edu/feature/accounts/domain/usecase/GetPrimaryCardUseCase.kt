package com.edu.feature.accounts.domain.usecase

import com.edu.core.domain.storage.SyncStatusStorage
import com.edu.core.domain.usecase.GetCardSummariesUseCase
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.feature.accounts.domain.model.CardDetail

/**
 * Card screen (21) shows one card: the first credit card, else the first card.
 * `CardDetail.summary` is null when the user has no cards (Empty state).
 */
class GetPrimaryCardUseCase(
    private val getCardSummaries: GetCardSummariesUseCase,
    private val syncStatus: SyncStatusStorage,
) {

    suspend operator fun invoke(): Result<CardDetail, DataError> =
        when (val result = getCardSummaries()) {
            is Result.Error -> result
            is Result.Success -> {
                val primary = result.data.firstOrNull { it.card.cardType.equals(CREDIT, ignoreCase = true) }
                    ?: result.data.firstOrNull()
                Result.Success(CardDetail(primary, syncStatus.getOfflineSince()))
            }
        }

    private companion object {
        const val CREDIT = "CREDIT"
    }
}
