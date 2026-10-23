package com.edu.feature.auth.presentation.connectbank

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.feature.auth.domain.model.BankProvider
import com.edu.feature.auth.domain.repository.BankProviderRepository
import com.edu.feature.auth.presentation.fake.FakeBankUiMapper
import com.edu.feature.auth.presentation.fake.fakeBankUi
import com.edu.feature.auth.presentation.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ConnectBankViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: BankProviderRepository = mockk()

    private fun createViewModel(
        initialState: ConnectBankUiState = ConnectBankUiState(),
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
    ) = ConnectBankViewModel(savedStateHandle, initialState, repository, FakeBankUiMapper())

    @Test
    fun `Should show featured banks When load succeeds`() = runTest {
        coEvery { repository.getFeaturedProviders() } returns Result.Success(listOf(BankProvider("ob-hsbc", "HSBC")))
        val viewModel = createViewModel()

        advanceUntilIdle()

        assertEquals(listOf(fakeBankUi("ob-hsbc")), viewModel.uiState.value.featuredBanks)
    }

    @Test
    fun `Should show error When load fails`() = runTest {
        coEvery { repository.getFeaturedProviders() } returns Result.Error(DataError.NetworkError.UNKNOWN)
        val viewModel = createViewModel()

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasBanksError)
    }

    @Test
    fun `Should not reload When state was restored`() = runTest {
        coEvery { repository.getFeaturedProviders() } returns Result.Success(emptyList())
        createViewModel(savedStateHandle = SavedStateHandle(mapOf("ui_state" to ConnectBankUiState())))

        advanceUntilIdle()

        coVerify(exactly = 0) { repository.getFeaturedProviders() }
    }

    @Test
    fun `Should open bank selection When connect clicked`() = runTest {
        coEvery { repository.getFeaturedProviders() } returns Result.Success(emptyList())
        val viewModel = createViewModel()

        viewModel.effect.test {
            viewModel.onAction(ConnectBankAction.ConnectClicked)
            assertEquals(ConnectBankEffect.NavigateToBankSelection, awaitItem())
        }
    }
}
