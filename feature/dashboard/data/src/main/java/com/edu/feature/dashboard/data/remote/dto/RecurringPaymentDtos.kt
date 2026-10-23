package com.edu.feature.dashboard.data.remote.dto

import com.edu.core.network.serialization.BigDecimalSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal

/** One entry of `GET /data/v1/accounts/{id}/direct_debits` → `results[]`. No next date in the API. */
@Serializable
data class DirectDebitDto(
    @SerialName("direct_debit_id") val directDebitId: String? = null,
    @SerialName("timestamp") val timestamp: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("previous_payment_timestamp") val previousPaymentTimestamp: String? = null,
    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("previous_payment_amount") val previousPaymentAmount: BigDecimal? = null,
    @SerialName("currency") val currency: String? = null,
)

/** One entry of `GET /data/v1/accounts/{id}/standing_orders` → `results[]`. No id in the API. */
@Serializable
data class StandingOrderDto(
    @SerialName("frequency") val frequency: String? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("timestamp") val timestamp: String? = null,
    @SerialName("currency") val currency: String? = null,
    @SerialName("next_payment_date") val nextPaymentDate: String? = null,
    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("next_payment_amount") val nextPaymentAmount: BigDecimal? = null,
    @SerialName("reference") val reference: String? = null,
    @SerialName("payee") val payee: String? = null,
)
