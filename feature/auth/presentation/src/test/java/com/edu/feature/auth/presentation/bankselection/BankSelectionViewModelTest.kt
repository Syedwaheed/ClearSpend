package com.edu.feature.auth.presentation.bankselection

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.feature.auth.domain.model.BankProvider
import com.edu.feature.auth.domain.usecase.BuildAuthorizationUrlUseCase
import com.edu.feature.auth.domain.usecase.SearchBanksUseCase
import com.edu.feature.auth.presentation.fake.FakeBankUiMapper
import com.edu.feature.auth.presentation.fake.fakeBankUi
import com.edu.feature.auth.presentation.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BankSelectionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val searchBanks: SearchBanksUseCase = mockk()
    private val buildAuthorizationUrl: BuildAuthorizationUrlUseCase = mockk()

    @Before
    fun setUp() {
        coEvery { searchBanks(any()) } returns Result.Success(listOf(BankProvider("ob-hsbc", "HSBC")))
    }

    private fun createViewModel(initialState: BankSelectionUiState = BankSelectionUiState()) =
        BankSelectionViewModel(SavedStateHandle(), initialState, searchBanks, buildAuthorizationUrl, FakeBankUiMapper())

    @Test
    fun `Should list banks When opened`() = runTest {
        val viewModel = createViewModel()

        advanceUntilIdle()

        assertEquals(listOf(fakeBankUi("ob-hsbc")), viewModel.uiState.value.banks)
    }

    @Test
    fun `Should show error When banks fail to load`() = runTest {
        coEvery { searchBanks(any()) } returns Result.Error(DataError.NetworkError.UNKNOWN)
        val viewModel = createViewModel()

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasError)
    }

    @Test
    fun `Should keep the typed query`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(BankSelectionAction.QueryChanged("mon"))
        advanceUntilIdle()

        assertEquals("mon", viewModel.uiState.value.query)
    }

    @Test
    fun `Should show the empty state When search matches nothing`() = runTest {
        coEvery { searchBanks("zzz") } returns Result.Success(emptyList())
        val viewModel = createViewModel()

        viewModel.onAction(BankSelectionAction.QueryChanged("zzz"))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isEmpty)
    }

    @Test
    fun `Should open the auth link for the picked bank`() = runTest {
        coEvery { buildAuthorizationUrl("ob-hsbc") } returns Result.Success("https://auth/hsbc")
        val viewModel = createViewModel()

        viewModel.effect.test {
            viewModel.onAction(BankSelectionAction.BankSelected("ob-hsbc"))
            assertEquals(BankSelectionEffect.OpenAuthLink("https://auth/hsbc"), awaitItem())
        }
    }

    @Test
    fun `Should open the auth link with every provider When bank not listed`() = runTest {
        coEvery { buildAuthorizationUrl(null) } returns Result.Success("https://auth/all")
        val viewModel = createViewModel()

        viewModel.effect.test {
            viewModel.onAction(BankSelectionAction.OtherBankClicked)
            assertEquals(BankSelectionEffect.OpenAuthLink("https://auth/all"), awaitItem())
        }
    }

    @Test
    fun `Should show launch error When the auth link cannot be prepared`() = runTest {
        coEvery { buildAuthorizationUrl(any()) } returns Result.Error(DataError.LocalError.DISKFULL)
        val viewModel = createViewModel()

        viewModel.onAction(BankSelectionAction.BankSelected("ob-hsbc"))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasLaunchError)
    }

    @Test
    fun `Should show launch error When no browser is available`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(BankSelectionAction.BrowserUnavailable)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasLaunchError)
    }

    @Test
    fun `Should navigate back When back clicked`() = runTest {
        val viewModel = createViewModel()

        viewModel.effect.test {
            viewModel.onAction(BankSelectionAction.BackClicked)
            assertEquals(BankSelectionEffect.NavigateBack, awaitItem())
        }
    }
}
