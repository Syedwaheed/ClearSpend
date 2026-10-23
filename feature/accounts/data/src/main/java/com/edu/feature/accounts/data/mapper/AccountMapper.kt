package com.edu.feature.accounts.data.mapper

import com.edu.core.database.entity.AccountEntity
import com.edu.core.domain.model.Account
import com.edu.core.domain.model.AccountNumber
import com.edu.core.domain.model.AccountType
import com.edu.core.domain.model.Provider
import com.edu.feature.accounts.data.remote.dto.AccountDto

/** AccountDto / AccountEntity ↔ domain [Account]. */
class AccountMapper {

    fun toDomain(dto: AccountDto): Account = Account(
        accountId = dto.accountId,
        accountType = accountTypeOf(dto.accountType),
        displayName = dto.displayName?.takeIf { it.isNotBlank() } ?: DEFAULT_NAME,
        currency = dto.currency ?: DEFAULT_CURRENCY,
        provider = Provider(
            providerId = dto.provider?.providerId.orEmpty(),
            displayName = dto.provider?.displayName?.takeIf { it.isNotBlank() } ?: UNKNOWN_PROVIDER,
            logoUri = dto.provider?.logoUri?.takeIf { it.isNotBlank() },
        ),
        accountNumber = AccountNumber(
            iban = dto.accountNumber?.iban,
            sortCode = dto.accountNumber?.sortCode,
            number = dto.accountNumber?.number,
        ),
    )

    fun toEntity(domain: Account): AccountEntity = AccountEntity(
        accountId = domain.accountId,
        accountType = domain.accountType.name,
        displayName = domain.displayName,
        currency = domain.currency,
        providerId = domain.provider.providerId,
        providerDisplayName = domain.provider.displayName,
        providerLogoUri = domain.provider.logoUri,
        iban = domain.accountNumber.iban,
        sortCode = domain.accountNumber.sortCode,
        number = domain.accountNumber.number,
    )

    fun fromEntity(entity: AccountEntity): Account = Account(
        accountId = entity.accountId,
        accountType = accountTypeOf(entity.accountType),
        displayName = entity.displayName,
        currency = entity.currency,
        provider = Provider(entity.providerId, entity.providerDisplayName, entity.providerLogoUri),
        accountNumber = AccountNumber(entity.iban, entity.sortCode, entity.number),
    )

    /** Unknown types are treated as current accounts so they still show up. */
    private fun accountTypeOf(raw: String?): AccountType =
        AccountType.entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: AccountType.TRANSACTION

    private companion object {
        const val DEFAULT_NAME = "Account"
        const val DEFAULT_CURRENCY = "GBP"
        const val UNKNOWN_PROVIDER = "Bank"
    }
}
