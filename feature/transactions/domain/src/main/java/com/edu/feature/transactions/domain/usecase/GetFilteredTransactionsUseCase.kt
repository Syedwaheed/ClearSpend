package com.edu.feature.transactions.domain.usecase

import com.edu.core.domain.model.Transaction
import com.edu.core.domain.model.TransactionCategory
import com.edu.feature.transactions.domain.model.CategoryFilter

/**
 * Client-side search and category filter (DESIGN_SPEC §4 · 12):
 * the query matches `merchant_name` ∨ `description`, case-insensitively,
 * ignoring surrounding whitespace; a blank query matches everything.
 */
class GetFilteredTransactionsUseCase {

    operator fun invoke(
        transactions: List<Transaction>,
        query: String,
        category: CategoryFilter,
    ): List<Transaction> {
        val needle = query.trim()
        return transactions.filter { it.matches(needle) && it.isIn(category) }
    }

    private fun Transaction.matches(needle: String): Boolean =
        needle.isEmpty() ||
            merchantName?.contains(needle, ignoreCase = true) == true ||
            description.contains(needle, ignoreCase = true)

    private fun Transaction.isIn(category: CategoryFilter): Boolean = when (category) {
        CategoryFilter.ALL -> true
        CategoryFilter.PURCHASE -> transactionCategory == TransactionCategory.PURCHASE
        CategoryFilter.DIRECT_DEBIT -> transactionCategory == TransactionCategory.DIRECT_DEBIT
        CategoryFilter.BILL_PAYMENT -> transactionCategory == TransactionCategory.BILL_PAYMENT
        CategoryFilter.TRANSFER -> transactionCategory == TransactionCategory.TRANSFER
        CategoryFilter.CASH -> transactionCategory == TransactionCategory.CASH || transactionCategory == TransactionCategory.ATM
        CategoryFilter.CREDIT -> transactionCategory == TransactionCategory.CREDIT
    }
}
