package com.edu.feature.accounts.data.mapper

import com.edu.core.data.time.parseTrueLayerDate
import com.edu.core.database.entity.CardBalanceEntity
import com.edu.core.database.entity.CardEntity
import com.edu.core.domain.model.Card
import com.edu.core.domain.model.CardBalance
import com.edu.core.domain.model.Provider
import com.edu.feature.accounts.data.remote.dto.CardBalanceDto
import com.edu.feature.accounts.data.remote.dto.CardDto
import java.math.BigDecimal
import java.time.LocalDate

/** CardDto / CardBalanceDto / entities ↔ domain [Card] and [CardBalance]. */
class CardMapper {

    fun toDomain(dto: CardDto): Card = Card(
        cardId = dto.accountId,
        cardNetwork = dto.cardNetwork.orEmpty(),
        cardType = dto.cardType.orEmpty(),
        displayName = dto.displayName?.takeIf { it.isNotBlank() } ?: "Card",
        partialCardNumber = dto.partialCardNumber.orEmpty(),
        nameOnCard = dto.nameOnCard.orEmpty(),
        provider = Provider(
            providerId = dto.provider?.providerId.orEmpty(),
            displayName = dto.provider?.displayName ?: "Bank",
            logoUri = dto.provider?.logoUri,
        ),
    )

    fun toDomain(cardId: String, dto: CardBalanceDto): CardBalance {
        val current = dto.current ?: BigDecimal.ZERO
        val limit = dto.creditLimit ?: BigDecimal.ZERO
        return CardBalance(
            cardId = cardId,
            available = dto.available ?: (limit - current),
            current = current,
            creditLimit = limit,
            lastStatementBalance = dto.lastStatementBalance,
            paymentDue = dto.paymentDue,
            paymentDueDate = parseTrueLayerDate(dto.paymentDueDate),
            currency = dto.currency ?: "GBP",
        )
    }

    fun toEntity(domain: Card): CardEntity = CardEntity(
        cardId = domain.cardId,
        cardNetwork = domain.cardNetwork,
        cardType = domain.cardType,
        displayName = domain.displayName,
        partialCardNumber = domain.partialCardNumber,
        nameOnCard = domain.nameOnCard,
        providerId = domain.provider.providerId,
        providerDisplayName = domain.provider.displayName,
        providerLogoUri = domain.provider.logoUri,
    )

    fun fromEntity(entity: CardEntity): Card = Card(
        cardId = entity.cardId,
        cardNetwork = entity.cardNetwork,
        cardType = entity.cardType,
        displayName = entity.displayName,
        partialCardNumber = entity.partialCardNumber,
        nameOnCard = entity.nameOnCard,
        provider = Provider(entity.providerId, entity.providerDisplayName, entity.providerLogoUri),
    )

    fun toEntity(domain: CardBalance): CardBalanceEntity = CardBalanceEntity(
        cardId = domain.cardId,
        available = domain.available.toPlainString(),
        current = domain.current.toPlainString(),
        creditLimit = domain.creditLimit.toPlainString(),
        lastStatementBalance = domain.lastStatementBalance?.toPlainString(),
        paymentDue = domain.paymentDue?.toPlainString(),
        paymentDueDateEpochDay = domain.paymentDueDate?.toEpochDay(),
        currency = domain.currency,
    )

    fun fromEntity(entity: CardBalanceEntity): CardBalance = CardBalance(
        cardId = entity.cardId,
        available = BigDecimal(entity.available),
        current = BigDecimal(entity.current),
        creditLimit = BigDecimal(entity.creditLimit),
        lastStatementBalance = entity.lastStatementBalance?.let(::BigDecimal),
        paymentDue = entity.paymentDue?.let(::BigDecimal),
        paymentDueDate = entity.paymentDueDateEpochDay?.let(LocalDate::ofEpochDay),
        currency = entity.currency,
    )
}
