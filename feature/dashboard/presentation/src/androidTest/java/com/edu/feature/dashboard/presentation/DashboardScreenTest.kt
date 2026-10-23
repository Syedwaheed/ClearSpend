package com.edu.feature.dashboard.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.edu.core.presentation.designsystem.ClearSpendTheme
import com.edu.feature.dashboard.presentation.home.DashboardAction
import com.edu.feature.dashboard.presentation.home.DashboardContent
import com.edu.feature.dashboard.presentation.home.DashboardPreviewData
import com.edu.feature.dashboard.presentation.home.DashboardTestTags
import com.edu.feature.dashboard.presentation.home.DashboardUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loadingSkeletonIsReplacedByContent() {
        var state by mutableStateOf(DashboardUiState(isLoading = true))
        composeRule.setContent {
            ClearSpendTheme(darkTheme = false) { DashboardContent(state = state, onAction = {}) }
        }
        composeRule.onNodeWithTag(DashboardTestTags.SKELETON).assertIsDisplayed()

        state = DashboardUiState(content = DashboardPreviewData.content)

        composeRule.onNodeWithTag(DashboardTestTags.CONTENT).assertIsDisplayed()
        composeRule.onNodeWithText("Good morning, Alex").assertIsDisplayed()
        composeRule.onNodeWithTag(DashboardTestTags.SKELETON).assertDoesNotExist()
    }

    @Test
    fun errorStateOffersTryAgain() {
        val actions = mutableListOf<DashboardAction>()
        composeRule.setContent {
            ClearSpendTheme(darkTheme = false) { DashboardContent(DashboardUiState(hasError = true)) { actions += it } }
        }

        composeRule.onNodeWithText("Couldn't load your data").assertIsDisplayed()
        composeRule.onNodeWithText("Try again").performClick()

        assertEquals(DashboardAction.RetryClicked, actions.last())
    }
}
