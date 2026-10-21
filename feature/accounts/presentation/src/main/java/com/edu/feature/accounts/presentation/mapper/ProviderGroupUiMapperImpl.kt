package com.edu.feature.accounts.presentation.mapper

import androidx.compose.ui.graphics.toArgb
import com.edu.core.presentation.designsystem.AccountTypeBusinessTransaction
import com.edu.core.presentation.designsystem.Blue
import com.edu.core.presentation.designsystem.CategoryBillPayment
import com.edu.core.presentation.designsystem.Mint
import com.edu.core.presentation.designsystem.Navy
import com.edu.core.presentation.ui.formatter.AmountFormatter
import com.edu.core.presentation.ui.mapper.mapList
import com.edu.feature.accounts.domain.model.ProviderGroup
import com.edu.feature.accounts.presentation.model.ProviderGroupUi
import kotlin.math.absoluteValue

class ProviderGroupUiMapperImpl(
    private val rowMapper: AccountRowUiMapper,
    private val amountFormatter: AmountFormatter,
) : ProviderGroupUiMapper {

    override fun map(domain: ProviderGroup): ProviderGroupUi {
        val name = domain.provider.displayName
        return ProviderGroupUi(
            id = domain.provider.providerId.ifEmpty { name },
            name = name,
            logoUri = domain.provider.logoUri,
            initial = name.firstOrNull { it.isLetterOrDigit() }?.uppercase() ?: "•",
            // Design fallback colour when the logo fails (DESIGN_SPEC §8): stable per provider.
            fallbackColorArgb = PALETTE[name.lowercase().hashCode().absoluteValue % PALETTE.size],
            formattedSubtotal = amountFormatter.format(domain.subtotal),
            accounts = rowMapper.mapList(domain.accounts),
        )
    }

    private companion object {
        val PALETTE = listOf(Blue, Mint, AccountTypeBusinessTransaction, CategoryBillPayment, Navy).map { it.toArgb() }
    }
}
