package com.edu.feature.dashboard.presentation.mapper

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.edu.core.presentation.designsystem.Blue
import com.edu.core.presentation.designsystem.CategoryBillPayment
import com.edu.core.presentation.designsystem.CategoryTransfer
import com.edu.core.presentation.designsystem.Error
import com.edu.core.presentation.designsystem.Mint
import com.edu.core.presentation.designsystem.OnSurfaceVariant
import com.edu.core.presentation.designsystem.Warning
import com.edu.core.presentation.ui.formatter.AmountFormatter
import com.edu.feature.dashboard.domain.model.CategorySpend
import com.edu.feature.dashboard.presentation.model.CategorySpendUi
import kotlin.math.absoluteValue

class CategorySpendUiMapperImpl(
    private val amountFormatter: AmountFormatter,
) : CategorySpendUiMapper {

    override fun map(domain: CategorySpend): CategorySpendUi = CategorySpendUi(
        label = domain.category,
        formattedAmount = amountFormatter.format(domain.amount),
        fraction = domain.ratioToLargest.toFloat().coerceIn(0f, 1f),
        colorArgb = colorFor(domain.category).toArgb(),
    )

    /** Known TrueLayer top-level classifications get the design's colours; others a stable palette colour. */
    private fun colorFor(category: String): Color = when (category.lowercase()) {
        "shopping" -> Blue
        "transfers", "transfer" -> Mint
        "bills", "bills and utilities", "utilities" -> Error
        "food & dining", "food and drink", "eating out", "groceries" -> Warning
        "entertainment", "leisure" -> CategoryTransfer
        "transport", "travel" -> CategoryBillPayment
        "other" -> OnSurfaceVariant
        else -> PALETTE[category.lowercase().hashCode().absoluteValue % PALETTE.size]
    }

    private companion object {
        val PALETTE = listOf(Blue, Mint, Error, Warning, CategoryTransfer, CategoryBillPayment)
    }
}
