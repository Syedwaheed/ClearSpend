package com.edu.feature.auth.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.edu.core.presentation.designsystem.ClearSpendTheme
import com.edu.feature.auth.presentation.connecting.ConnectingAction
import com.edu.feature.auth.presentation.connecting.ConnectingContent
import com.edu.feature.auth.presentation.connecting.ConnectingStatus
import com.edu.feature.auth.presentation.connecting.ConnectingUiState
import com.edu.feature.auth.presentation.model.ConnectionErrorUi
import com.edu.feature.auth.presentation.onboarding.OnboardingAction
import com.edu.feature.auth.presentation.onboarding.OnboardingContent
import com.edu.feature.auth.presentation.onboarding.OnboardingUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthScreensTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun onboardingLastPageShowsGetStartedAndSignIn() {
        val actions = mutableListOf<OnboardingAction>()
        composeRule.setContent {
            ClearSpendTheme { OnboardingContent(OnboardingUiState(currentPage = 2)) { actions += it } }
        }

        composeRule.onNodeWithText("Get Started").assertIsDisplayed().performClick()

        assertEquals(OnboardingAction.GetStartedClicked, actions.last())
    }

    @Test
    fun connectingOfflineErrorOffersTryAgain() {
        val actions = mutableListOf<ConnectingAction>()
        composeRule.setContent {
            ClearSpendTheme {
                ConnectingContent(
                    ConnectingUiState(code = "c", status = ConnectingStatus.FAILED, error = ConnectionErrorUi.NO_INTERNET),
                ) { actions += it }
            }
        }

        composeRule.onNodeWithText("Try again").performClick()

        assertEquals(ConnectingAction.RetryClicked, actions.last())
    }

    @Test
    fun connectingSecurityErrorOnlyOffersStartAgain() {
        composeRule.setContent {
            ClearSpendTheme {
                ConnectingContent(
                    ConnectingUiState(status = ConnectingStatus.FAILED, error = ConnectionErrorUi.SECURITY_CHECK_FAILED),
                ) {}
            }
        }

        composeRule.onNodeWithText("Start again").assertIsDisplayed()
    }
}
