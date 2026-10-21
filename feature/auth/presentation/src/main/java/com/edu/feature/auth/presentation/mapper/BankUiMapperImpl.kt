package com.edu.feature.auth.presentation.mapper

import com.edu.feature.auth.domain.model.BankProvider
import com.edu.feature.auth.domain.model.BankSegment
import com.edu.feature.auth.presentation.model.BankUi

/**
 * Brand badge colours/initials are design fallbacks (DESIGN_SPEC §8: provider
 * colour/initial are used when no logo is available).
 */
class BankUiMapperImpl : BankUiMapper {

    override fun map(domain: BankProvider): BankUi = BankUi(
        providerId = domain.providerId,
        name = domain.displayName,
        initial = INITIALS[domain.providerId] ?: initialFor(domain.displayName),
        brandColorArgb = BRAND_COLORS[domain.providerId] ?: DEFAULT_COLOR,
        segmentLabel = when (domain.segment) {
            BankSegment.PERSONAL -> "Personal"
            BankSegment.BUSINESS -> "Business"
        },
    )

    private fun initialFor(name: String): String =
        name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    private companion object {
        const val DEFAULT_COLOR = 0xFF6B7684

        val INITIALS = mapOf(
            "ob-starling" to "St",
            "ob-first-direct" to "FD",
        )

        val BRAND_COLORS = mapOf(
            "ob-barclays" to 0xFF00AEEF,
            "ob-hsbc" to 0xFFDB0011,
            "ob-lloyds" to 0xFF024731,
            "ob-natwest" to 0xFF5A287D,
            "ob-santander" to 0xFFEC0000,
            "ob-monzo" to 0xFF2D6EF5,
            "ob-starling" to 0xFF00D4AA,
            "ob-halifax" to 0xFF0A5EB0,
            "ob-first-direct" to 0xFF000000,
            "ob-tsb" to 0xFF0072CE,
        )
    }
}
