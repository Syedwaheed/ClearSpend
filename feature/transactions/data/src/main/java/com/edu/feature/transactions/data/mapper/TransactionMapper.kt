package com.edu.feature.transactions.data.mapper

import com.edu.core.data.time.parseTrueLayerInstant
import com.edu.core.database.entity.TransactionEntity
import com.edu.core.domain.model.Transaction
import com.edu.core.domain.model.TransactionCategory
import com.edu.core.domain.model.TransactionType
import com.edu.feature.transactions.data.remote.dto.TransactionDto
import java.math.BigDecimal
import java.time.Instant

/** TransactionDto / TransactionEntity ↔ domain [Transaction]. */
class TransactionMapper {

    /** Null when the row is unusable (no amount or no timestamp). */
    fun toDomain(accountId: String, dto: TransactionDto, isPending: Boolean): Transaction? {
        val amount = dto.amount ?: return null
        val timestamp = parseTrueLayerInstant(dto.timestamp) ?: return null
        val description = dto.description?.trim().orEmpty()
        return Transaction(
            transactionId = dto.transactionId?.takeIf { it.isNotBlank() }
                ?: syntheticId(accountId, timestamp, description, amount),
            accountId = accountId,
            timestamp = timestamp,
            description = description,
            merchantName = dto.merchantName?.trim()?.takeIf { it.isNotEmpty() },
            amount = amount,
            currency = dto.currency ?: "GBP",
            transactionType = typeOf(dto.transactionType, amount),
            transactionCategory = categoryOf(dto.transactionCategory),
            classification = dto.transactionClassification.filter { it.isNotBlank() },
            runningBalance = dto.runningBalance?.amount,
            isPending = isPending,
            providerReference = dto.meta?.providerReference?.trim()?.takeIf { it.isNotEmpty() },
        )
    }

    fun toEntity(domain: Transaction): TransactionEntity = TransactionEntity(
        transactionId = domain.transactionId,
        accountId = domain.accountId,
        timestampEpochMillis = domain.timestamp.toEpochMilli(),
        description = domain.description,
        merchantName = domain.merchantName,
        amount = domain.amount.toPlainString(),
        currency = domain.currency,
        transactionType = domain.transactionType.name,
        transactionCategory = domain.transactionCategory.name,
        classification = domain.classification,
        runningBalance = domain.runningBalance?.toPlainString(),
        isPending = domain.isPending,
        providerReference = domain.providerReference,
    )

    fun fromEntity(entity: TransactionEntity): Transaction {
        val amount = BigDecimal(entity.amount)
        return Transaction(
            transactionId = entity.transactionId,
            accountId = entity.accountId,
            timestamp = Instant.ofEpochMilli(entity.timestampEpochMillis),
            description = entity.description,
            merchantName = entity.merchantName,
            amount = amount,
            currency = entity.currency,
            transactionType = typeOf(entity.transactionType, amount),
            transactionCategory = categoryOf(entity.transactionCategory),
            classification = entity.classification,
            runningBalance = entity.runningBalance?.let(::BigDecimal),
            isPending = entity.isPending,
            providerReference = entity.providerReference,
        )
    }

    /** Falls back to the sign of [amount] (debits are negative in the API). */
    private fun typeOf(raw: String?, amount: BigDecimal): TransactionType =
        TransactionType.entries.firstOrNull { it.name.equals(raw, ignoreCase = true) }
            ?: if (amount.signum() < 0) TransactionType.DEBIT else TransactionType.CREDIT

    /** Categories outside DESIGN_SPEC §6 (INTEREST, FEE_CHARGE, …) become OTHER. */
    private fun categoryOf(raw: String?): TransactionCategory =
        TransactionCategory.entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: TransactionCategory.OTHER

    /** Some providers omit ids on pending rows; derive a stable one so Room can upsert. */
    private fun syntheticId(accountId: String, timestamp: Instant, description: String, amount: BigDecimal): String =
        "synthetic-" + "$accountId|${timestamp.toEpochMilli()}|$description|${amount.toPlainString()}".hashCode().toUInt()
}
