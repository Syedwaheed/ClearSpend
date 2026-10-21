package com.edu.feature.accounts.data.remote.dto

import com.edu.core.network.serialization.BigDecimalSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal

/** One entry of `GET /data/v1/cards` → `results[]`. The card's id is `account_id`. */
@Serializable
data class CardDto(
    @SerialName("account_id") val accountId: String,
    @SerialName("card_network") val cardNetwork: String? = null,
    @SerialName("card_type") val cardType: String? = null,
    @SerialName("currency") val currency: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("partial_card_number") val partialCardNumber: String? = null,
    @SerialName("name_on_card") val nameOnCard: String? = null,
    @SerialName("provider") val provider: ProviderDto? = null,
)

/** `GET /data/v1/cards/{id}/balance` → `results[0]`. */
@Serializable
data class CardBalanceDto(
    @SerialName("currency") val currency: String? = null,
    @Serializable(with = BigDecimalSerializer::class) @SerialName("available") val available: BigDecimal? = null,
    @Serializable(with = BigDecimalSerializer::class) @SerialName("current") val current: BigDecimal? = null,
    @Serializable(with = BigDecimalSerializer::class) @SerialName("credit_limit") val creditLimit: BigDecimal? = null,
    @Serializable(with = BigDecimalSerializer::class) @SerialName("last_statement_balance") val lastStatementBalance: BigDecimal? = null,
    @Serializable(with = BigDecimalSerializer::class) @SerialName("payment_due") val paymentDue: BigDecimal? = null,
    @SerialName("payment_due_date") val paymentDueDate: String? = null,
    @SerialName("update_timestamp") val updateTimestamp: String? = null,
)
