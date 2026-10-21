package com.edu.core.domain.model

import java.math.BigDecimal

/** Utilisation colour band (DESIGN_SPEC §4 · 21): < 40 % low, < 70 % medium, ≥ 70 % high. */
enum class UtilisationBand { LOW, MEDIUM, HIGH }

data class CardUtilisation(
    /** current / credit_limit, clamped to 0..1 for drawing. */
    val ratio: BigDecimal,
    /** Rounded whole percent (may exceed 100 when over limit). */
    val percent: Int,
    val band: UtilisationBand,
)

/** A card with its balance and derived utilisation. Shared by Home (09), Accounts (10) and Card (21). */
data class CardSummary(
    val card: Card,
    val balance: CardBalance?,
    val utilisation: CardUtilisation?,
)
