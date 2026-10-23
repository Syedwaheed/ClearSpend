package com.edu.feature.accounts.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProviderDto(
    @SerialName("provider_id") val providerId: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("logo_uri") val logoUri: String? = null,
)

@Serializable
data class AccountNumberDto(
    @SerialName("iban") val iban: String? = null,
    @SerialName("number") val number: String? = null,
    @SerialName("sort_code") val sortCode: String? = null,
    @SerialName("swift_bic") val swiftBic: String? = null,
)

/** One entry of `GET /data/v1/accounts` → `results[]`. */
@Serializable
data class AccountDto(
    @SerialName("account_id") val accountId: String,
    @SerialName("account_type") val accountType: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("currency") val currency: String? = null,
    @SerialName("account_number") val accountNumber: AccountNumberDto? = null,
    @SerialName("provider") val provider: ProviderDto? = null,
    @SerialName("update_timestamp") val updateTimestamp: String? = null,
)
