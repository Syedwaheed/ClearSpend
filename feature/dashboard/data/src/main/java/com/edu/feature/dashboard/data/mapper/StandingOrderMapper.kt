package com.edu.feature.dashboard.data.mapper

import com.edu.core.data.time.parseTrueLayerDate
import com.edu.core.database.entity.StandingOrderEntity
import com.edu.core.domain.model.StandingOrder
import com.edu.core.domain.model.StandingOrderFrequency
import com.edu.feature.dashboard.data.remote.dto.StandingOrderDto
import java.math.BigDecimal
import java.time.LocalDate

/** StandingOrderDto / StandingOrderEntity ↔ domain [StandingOrder]. */
class StandingOrderMapper {

    /** Null for inactive orders and rows without a payee or amount. */
    fun toDomain(accountId: String, dto: StandingOrderDto): StandingOrder? {
        if (dto.status.equals("Inactive", ignoreCase = true)) return null
        val payee = dto.payee?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val amount = dto.nextPaymentAmount?.abs() ?: return null
        val reference = dto.reference?.trim()?.takeIf { it.isNotEmpty() }
        return StandingOrder(
            // The API has no id: derive a stable one from what identifies the order.
            standingOrderId = "so-${"$accountId|$payee|${reference.orEmpty()}|${dto.frequency.orEmpty()}".hashCode().toUInt()}",
            accountId = accountId,
            payee = payee,
            reference = reference,
            frequency = frequencyOf(dto.frequency),
            nextPaymentDate = parseTrueLayerDate(dto.nextPaymentDate),
            nextPaymentAmount = amount,
            currency = dto.currency ?: "GBP",
        )
    }

    fun toEntity(domain: StandingOrder): StandingOrderEntity = StandingOrderEntity(
        standingOrderId = domain.standingOrderId,
        accountId = domain.accountId,
        payee = domain.payee,
        reference = domain.reference,
        frequency = domain.frequency.name,
        nextPaymentDateEpochDay = domain.nextPaymentDate?.toEpochDay(),
        nextPaymentAmount = domain.nextPaymentAmount.toPlainString(),
        currency = domain.currency,
    )

    fun fromEntity(entity: StandingOrderEntity): StandingOrder = StandingOrder(
        standingOrderId = entity.standingOrderId,
        accountId = entity.accountId,
        payee = entity.payee,
        reference = entity.reference,
        frequency = StandingOrderFrequency.entries.firstOrNull { it.name == entity.frequency } ?: StandingOrderFrequency.OTHER,
        nextPaymentDate = entity.nextPaymentDateEpochDay?.let(LocalDate::ofEpochDay),
        nextPaymentAmount = BigDecimal(entity.nextPaymentAmount),
        currency = entity.currency,
    )

    /**
     * TrueLayer passes the provider's frequency code through, e.g. ISO 20022
     * `IntrvlWkDay:01:03`, `IntrvlMnthDay:01:15`, `EvryWorkgDay`, or plain words.
     */
    private fun frequencyOf(raw: String?): StandingOrderFrequency {
        val value = raw?.lowercase().orEmpty()
        return when {
            "wk" in value || "week" in value -> StandingOrderFrequency.WEEKLY
            "mnth" in value || "month" in value -> StandingOrderFrequency.MONTHLY
            "yr" in value || "year" in value || "annual" in value -> StandingOrderFrequency.ANNUALLY
            else -> StandingOrderFrequency.OTHER
        }
    }
}
