package com.edu.feature.accounts.presentation.accounts

import com.edu.core.presentation.ui.model.CardSummaryUi
import com.edu.feature.accounts.presentation.model.AccountFilterUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountsUiStateTest {

    private val content = AccountsPreviewData.content

    @Test
    fun `Should show every group under All`() {
        assertEquals(2, AccountsUiState(content = content).visibleGroups.size)
    }

    @Test
    fun `Should keep only matching rows for a chip`() {
        val groups = AccountsUiState(content = content, selectedFilter = AccountFilterUi.SAVINGS).visibleGroups

        assertEquals(listOf("a2"), groups.flatMap { it.accounts }.map { it.account.accountId })
    }

    @Test
    fun `Should drop groups left empty by a chip`() {
        assertEquals(listOf("bank-b"), AccountsUiState(content = content, selectedFilter = AccountFilterUi.BUSINESS).visibleGroups.map { it.id })
    }

    @Test
    fun `Should show cards only under All`() {
        assertFalse(AccountsUiState(content = content, selectedFilter = AccountFilterUi.CURRENT).showsCards)
    }

    @Test
    fun `Should be empty When there are no accounts or cards`() {
        assertTrue(AccountsUiState(content = content.copy(groups = emptyList(), cards = emptyList<CardSummaryUi>())).isEmpty)
    }
}
