package com.edu.feature.transactions.presentation.mapper

import com.edu.core.domain.model.Account
import com.edu.core.presentation.ui.mapper.UiMapper
import com.edu.feature.transactions.domain.model.CategoryFilter
import com.edu.feature.transactions.domain.model.TransactionDetail
import com.edu.feature.transactions.domain.model.TransactionPeriod
import com.edu.feature.transactions.domain.model.TransactionsOverview
import com.edu.feature.transactions.presentation.model.AccountOptionUi
import com.edu.feature.transactions.presentation.model.CategoryChipUi
import com.edu.feature.transactions.presentation.model.PeriodSelectionUi
import com.edu.feature.transactions.presentation.model.TransactionDetailUi
import com.edu.feature.transactions.presentation.model.TransactionsContentUi

interface TransactionsContentUiMapper : UiMapper<TransactionsOverview, TransactionsContentUi>
interface AccountOptionUiMapper : UiMapper<Account, AccountOptionUi>
interface TransactionDetailUiMapper : UiMapper<TransactionDetail, TransactionDetailUi>

/**
 * The opposite direction: the filter selection held in the UiState → the
 * domain inputs of the Transactions use cases. Kept out of the ViewModel so it
 * never converts dates itself.
 */
interface TransactionFilterMapper {
    fun toPeriod(selection: PeriodSelectionUi): TransactionPeriod
    fun toCategory(chip: CategoryChipUi): CategoryFilter
}
