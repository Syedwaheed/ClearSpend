package com.edu.feature.auth.domain.usecase

import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.core.domain.util.map
import com.edu.feature.auth.domain.model.BankProvider
import com.edu.feature.auth.domain.repository.BankProviderRepository

/** Case-insensitive name search for Bank Selection (06). A blank query returns every bank. */
class SearchBanksUseCase(
    private val bankProviderRepository: BankProviderRepository,
) {
    suspend operator fun invoke(query: String): Result<List<BankProvider>, DataError> {
        val needle = query.trim()
        return bankProviderRepository.getProviders().map { providers ->
            if (needle.isEmpty()) {
                providers
            } else {
                providers.filter { it.displayName.contains(needle, ignoreCase = true) }
            }
        }
    }
}
