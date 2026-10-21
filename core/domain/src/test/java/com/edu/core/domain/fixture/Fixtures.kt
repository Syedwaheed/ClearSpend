package com.edu.core.domain.fixture

import com.edu.core.domain.model.Account
import com.edu.core.domain.model.AccountNumber
import com.edu.core.domain.model.AccountType
import com.edu.core.domain.model.Balance
import com.edu.core.domain.model.Card
import com.edu.core.domain.model.CardBalance
import com.edu.core.domain.model.Provider
import java.math.BigDecimal
import java.time.Instant

fun account(id: String, type: AccountType = AccountType.TRANSACTION) = Account(
    accountId = id,
    accountType = type,
    displayName = id,
    provider = Provider("p", "Bank"),
    accountNumber = AccountNumber(),
)

fun balance(id: String, available: String = "10") = Balance(
    accountId = id,
    available = BigDecimal(available),
    current = BigDecimal(available),
    updateTimestamp = Instant.EPOCH,
)

fun card(id: String) = Card(id, "VISA", "CREDIT", id, "1234", "J", Provider("p", "Bank"))

fun cardBalance(id: String, current: String = "100", limit: String = "1000") =
    CardBalance(id, BigDecimal(limit) - BigDecimal(current), BigDecimal(current), BigDecimal(limit))
