package com.edu.feature.transactions.data.remote.dto

import com.edu.core.network.serialization.BigDecimalSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class RunningBalanceDto(
    @Serializable(with = BigDecimalSerializer::class) @SerialName("amount") val amount: BigDecimal? = null,
    @SerialName("currency") val currency: String? = null,
)

/**
 * One entry of `GET /data/v1/accounts/{id}/transactions[/pending]` → `results[]`.
 * `amount` is NEGATIVE for debits in the TrueLayer API.
 */
@Serializable
data class TransactionDto(
    @SerialName("transaction_id") val transactionId: String? = null,
    @SerialName("timestamp") val timestamp: String? = null,
    @SerialName("description") val description: String? = null,
    @Serializable(with = BigDecimalSerializer::class) @SerialName("amount") val amount: BigDecimal? = null,
    @SerialName("currency") val currency: String? = null,
    @SerialName("transaction_type") val transactionType: String? = null,
    @SerialName("transaction_category") val transactionCategory: String? = null,
    @SerialName("transaction_classification") val transactionClassification: List<String> = emptyList(),
    @SerialName("merchant_name") val merchantName: String? = null,
    @SerialName("running_balance") val runningBalance: RunningBalanceDto? = null,
)
