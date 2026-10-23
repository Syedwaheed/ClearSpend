package com.edu.core.domain.usecase

import com.edu.core.domain.model.CardSummary
import com.edu.core.domain.repository.CardRepository
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * `GET /cards`, then each `/cards/{id}/balance` in parallel, plus the derived
 * utilisation band. A failed card balance leaves it null; a 401 fails the load.
 * Shared by Home (09), Accounts (10) and Card (21).
 */
class GetCardSummariesUseCase(
    private val cardRepository: CardRepository,
    private val getCardUtilisation: GetCardUtilisationUseCase,
) {

    suspend operator fun invoke(): Result<List<CardSummary>, DataError> {
        val cards = when (val result = cardRepository.getCards()) {
            is Result.Success -> result.data
            is Result.Error -> return result
        }
        val balances = coroutineScope {
            cards.map { card -> async { cardRepository.getCardBalance(card.cardId) } }.awaitAll()
        }
        if (balances.any { it is Result.Error && it.error == DataError.NetworkError.UNAUTHORIZED }) {
            return Result.Error(DataError.NetworkError.UNAUTHORIZED)
        }
        return Result.Success(
            cards.zip(balances) { card, result ->
                val balance = (result as? Result.Success)?.data
                CardSummary(card = card, balance = balance, utilisation = balance?.let(getCardUtilisation::invoke))
            },
        )
    }
}
