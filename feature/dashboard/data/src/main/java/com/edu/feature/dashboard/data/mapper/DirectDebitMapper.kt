package com.edu.feature.dashboard.data.mapper

import com.edu.core.data.time.parseTrueLayerInstant
import com.edu.core.database.entity.DirectDebitEntity
import com.edu.core.domain.model.DirectDebit
import com.edu.core.domain.model.DirectDebitStatus
import com.edu.feature.dashboard.data.remote.dto.DirectDebitDto
import java.math.BigDecimal
import java.time.Instant

/** DirectDebitDto / DirectDebitEntity ↔ domain [DirectDebit]. */
class DirectDebitMapper {

    fun toDomain(accountId: String, dto: DirectDebitDto): DirectDebit? {
        val name = dto.name?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        return DirectDebit(
            directDebitId = dto.directDebitId?.takeIf { it.isNotBlank() } ?: "dd-${"$accountId|$name".hashCode().toUInt()}",
            accountId = accountId,
            name = name,
            status = statusOf(dto.status),
            previousPaymentAmount = dto.previousPaymentAmount?.abs(),
            previousPaymentTimestamp = parseTrueLayerInstant(dto.previousPaymentTimestamp),
            currency = dto.currency ?: "GBP",
        )
    }

    fun toEntity(domain: DirectDebit): DirectDebitEntity = DirectDebitEntity(
        directDebitId = domain.directDebitId,
        accountId = domain.accountId,
        name = domain.name,
        status = domain.status.name,
        previousPaymentAmount = domain.previousPaymentAmount?.toPlainString(),
        previousPaymentTimestampEpochMillis = domain.previousPaymentTimestamp?.toEpochMilli(),
        currency = domain.currency,
    )

    fun fromEntity(entity: DirectDebitEntity): DirectDebit = DirectDebit(
        directDebitId = entity.directDebitId,
        accountId = entity.accountId,
        name = entity.name,
        status = statusOf(entity.status),
        previousPaymentAmount = entity.previousPaymentAmount?.let(::BigDecimal),
        previousPaymentTimestamp = entity.previousPaymentTimestampEpochMillis?.let(Instant::ofEpochMilli),
        currency = entity.currency,
    )

    /** TrueLayer sends "Active" / "Inactive"; anything else is treated as not active. */
    private fun statusOf(raw: String?): DirectDebitStatus = when (raw?.trim()?.uppercase()?.replace(' ', '_')) {
        "ACTIVE" -> DirectDebitStatus.ACTIVE
        "FAILURE_PENDING" -> DirectDebitStatus.FAILURE_PENDING
        else -> DirectDebitStatus.INACTIVE
    }
}
