package com.edu.feature.accounts.data.remote.dto

import com.edu.core.network.serialization.BigDecimalSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal

/** `GET /data/v1/accounts/{id}/balance` → `results[0]`. */
@Serializable
data class BalanceDto(
    @SerialName("currency") val currency: String? = null,
    @Serializable(with = BigDecimalSerializer::class) @SerialName("available") val available: BigDecimal? = null,
    @Serializable(with = BigDecimalSerializer::class) @SerialName("current") val current: BigDecimal? = null,
    @Serializable(with = BigDecimalSerializer::class) @SerialName("overdraft") val overdraft: BigDecimal? = null,
    @SerialName("update_timestamp") val updateTimestamp: String? = null,
)
