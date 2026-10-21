package com.edu.core.domain.usecase

import com.edu.core.domain.model.CardBalance
import com.edu.core.domain.model.CardUtilisation
import com.edu.core.domain.model.UtilisationBand
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Utilisation = `current / credit_limit` (DESIGN_SPEC §4 · 21) and its band:
 * green < 40 %, amber < 70 %, red ≥ 70 %. Shared by the accounts and
 * dashboard features, so it lives in core:domain.
 */
class GetCardUtilisationUseCase {

    operator fun invoke(balance: CardBalance): CardUtilisation? {
        if (balance.creditLimit.signum() <= 0) return null
        val owed = balance.current.max(BigDecimal.ZERO)
        val rawRatio = owed.divide(balance.creditLimit, 10, RoundingMode.HALF_UP)
        val percent = rawRatio.multiply(HUNDRED).setScale(0, RoundingMode.HALF_UP).toInt()
        val band = when {
            rawRatio < LOW_LIMIT -> UtilisationBand.LOW
            rawRatio < MEDIUM_LIMIT -> UtilisationBand.MEDIUM
            else -> UtilisationBand.HIGH
        }
        return CardUtilisation(ratio = rawRatio.min(BigDecimal.ONE), percent = percent, band = band)
    }

    private companion object {
        val HUNDRED = BigDecimal(100)
        val LOW_LIMIT = BigDecimal("0.40")
        val MEDIUM_LIMIT = BigDecimal("0.70")
    }
}
