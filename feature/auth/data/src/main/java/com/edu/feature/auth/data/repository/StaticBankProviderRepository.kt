package com.edu.feature.auth.data.repository

import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.feature.auth.domain.model.BankProvider
import com.edu.feature.auth.domain.repository.BankProviderRepository

/**
 * Static provider list for Bank Selection (06) — DESIGN_SPEC §1.3 allows a
 * static list. Ids are TrueLayer Open Banking provider ids. A later phase can
 * swap this for `GET {auth}/api/providers?client_id=…` without touching
 * presentation.
 */
class StaticBankProviderRepository : BankProviderRepository {

    override suspend fun getProviders(): Result<List<BankProvider>, DataError> = Result.Success(PROVIDERS)

    override suspend fun getFeaturedProviders(): Result<List<BankProvider>, DataError> =
        Result.Success(PROVIDERS.take(FEATURED_COUNT))

    private companion object {
        const val FEATURED_COUNT = 7

        val PROVIDERS = listOf(
            BankProvider(providerId = "ob-barclays", displayName = "Barclays"),
            BankProvider(providerId = "ob-hsbc", displayName = "HSBC"),
            BankProvider(providerId = "ob-lloyds", displayName = "Lloyds"),
            BankProvider(providerId = "ob-natwest", displayName = "NatWest"),
            BankProvider(providerId = "ob-santander", displayName = "Santander"),
            BankProvider(providerId = "ob-monzo", displayName = "Monzo"),
            BankProvider(providerId = "ob-starling", displayName = "Starling"),
            BankProvider(providerId = "ob-halifax", displayName = "Halifax"),
            BankProvider(providerId = "ob-first-direct", displayName = "First Direct"),
            BankProvider(providerId = "ob-tsb", displayName = "TSB"),
        )
    }
}
