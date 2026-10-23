package com.edu.feature.accounts.data.mapper

import com.edu.core.data.time.parseTrueLayerInstant
import com.edu.core.database.entity.BalanceEntity
import com.edu.core.domain.model.Balance
import com.edu.feature.accounts.data.remote.dto.BalanceDto
import java.math.BigDecimal
import java.time.Instant

/** BalanceDto / BalanceEntity ↔ domain [Balance]. Money is stored as a BigDecimal string. */
class BalanceMapper {

    /** [fetchedAt] stands in for a missing `update_timestamp`. */
    fun toDomain(accountId: String, dto: BalanceDto, fetchedAt: Instant): Balance {
        val current = dto.current ?: dto.available ?: BigDecimal.ZERO
        return Balance(
            accountId = accountId,
            available = dto.available ?: current,
            current = current,
            overdraft = dto.overdraft ?: BigDecimal.ZERO,
            currency = dto.currency ?: "GBP",
            updateTimestamp = parseTrueLayerInstant(dto.updateTimestamp) ?: fetchedAt,
        )
    }

    fun toEntity(domain: Balance): BalanceEntity = BalanceEntity(
        accountId = domain.accountId,
        available = domain.available.toPlainString(),
        current = domain.current.toPlainString(),
        overdraft = domain.overdraft.toPlainString(),
        currency = domain.currency,
        updateTimestampEpochMillis = domain.updateTimestamp.toEpochMilli(),
    )

    fun fromEntity(entity: BalanceEntity): Balance = Balance(
        accountId = entity.accountId,
        available = BigDecimal(entity.available),
        current = BigDecimal(entity.current),
        overdraft = BigDecimal(entity.overdraft),
        currency = entity.currency,
        updateTimestamp = Instant.ofEpochMilli(entity.updateTimestampEpochMillis),
    )
}
