package com.edu.feature.accounts.presentation.card

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.edu.core.domain.model.CardSummary
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.feature.accounts.domain.model.CardDetail
import com.edu.feature.accounts.domain.usecase.GetPrimaryCardUseCase
import com.edu.feature.accounts.presentation.fake.FakeCardSummaryUiMapper
import com.edu.feature.accounts.presentation.fake.FakeSyncTimeUiMapper
import com.edu.feature.accounts.presentation.fake.NOW
import com.edu.feature.accounts.presentation.fake.card
import com.edu.feature.accounts.presentation.fake.fakeCardUi
import com.edu.feature.accounts.presentation.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getPrimaryCard: GetPrimaryCardUseCase = mockk()

    @Before
    fun setUp() {
        coEvery { getPrimaryCard() } returns Result.Success(CardDetail(CardSummary(card("c"), null, null), offlineSince = null))
    }

    private fun createViewModel(
        initialState: CardUiState = CardUiState(),
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
    ) = CardViewModel(savedStateHandle, initialState, getPrimaryCard, FakeCardSummaryUiMapper(), FakeSyncTimeUiMapper())

    @Test
    fun `Should show the card When it loads`() = runTest {
        val viewModel = createViewModel()

        advanceUntilIdle()

        assertEquals(fakeCardUi("c"), viewModel.uiState.value.card)
    }

    @Test
    fun `Should show the empty state When there is no card`() = runTest {
        coEvery { getPrimaryCard() } returns Result.Success(CardDetail(null, null))
        val viewModel = createViewModel()

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasNoCard)
    }

    @Test
    fun `Should show the offline banner When cached`() = runTest {
        coEvery { getPrimaryCard() } returns Result.Success(CardDetail(CardSummary(card("c"), null, null), NOW))
        val viewModel = createViewModel()

        advanceUntilIdle()

        assertEquals("synced", viewModel.uiState.value.offlineSyncTime)
    }

    @Test
    fun `Should show the error state When loading fails`() = runTest {
        coEvery { getPrimaryCard() } returns Result.Error(DataError.NetworkError.NO_INTERNET)
        val viewModel = createViewModel()

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasError)
    }

    @Test
    fun `Should route to Connect Bank When unauthorized`() = runTest {
        coEvery { getPrimaryCard() } returns Result.Error(DataError.NetworkError.UNAUTHORIZED)
        val viewModel = createViewModel()

        viewModel.effect.test {
            assertEquals(CardEffect.NavigateToConnectBank, awaitItem())
        }
    }

    @Test
    fun `Should navigate back`() = runTest {
        val viewModel = createViewModel(initialState = CardUiState(card = fakeCardUi("c")))

        viewModel.effect.test {
            viewModel.onAction(CardAction.BackClicked)
            assertEquals(CardEffect.NavigateBack, awaitItem())
        }
    }

    @Test
    fun `Should skip the initial load When state is restored`() = runTest {
        createViewModel(savedStateHandle = SavedStateHandle(mapOf("ui_state" to CardUiState(card = fakeCardUi("c")))))

        advanceUntilIdle()

        coVerify(exactly = 0) { getPrimaryCard() }
    }
}
