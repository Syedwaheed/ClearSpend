package com.edu.feature.accounts.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.edu.core.presentation.designsystem.ClearSpendTheme
import com.edu.feature.accounts.presentation.accounts.AccountsAction
import com.edu.feature.accounts.presentation.accounts.AccountsContent
import com.edu.feature.accounts.presentation.accounts.AccountsPreviewData
import com.edu.feature.accounts.presentation.accounts.AccountsUiState
import com.edu.feature.accounts.presentation.model.AccountFilterUi
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccountsScreensTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun refreshShowsSyncingLabel() {
        composeRule.setContent {
            ClearSpendTheme(darkTheme = false) {
                AccountsContent(AccountsUiState(content = AccountsPreviewData.content, isRefreshing = true), onAction = {})
            }
        }

        composeRule.onNodeWithText("Syncing…").assertIsDisplayed()
    }

    @Test
    fun tappingAChipSelectsTheFilter() {
        val actions = mutableListOf<AccountsAction>()
        composeRule.setContent {
            ClearSpendTheme(darkTheme = false) {
                AccountsContent(AccountsUiState(content = AccountsPreviewData.content)) { actions += it }
            }
        }

        composeRule.onNodeWithText("Savings").performClick()

        assertEquals(AccountsAction.FilterSelected(AccountFilterUi.SAVINGS), actions.last())
    }
}
