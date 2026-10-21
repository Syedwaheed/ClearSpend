package com.edu.feature.auth.domain.usecase

import com.edu.core.domain.util.Result
import com.edu.feature.auth.domain.model.AuthError
import com.edu.feature.auth.domain.model.AuthRedirect
import com.edu.feature.auth.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ValidateAuthRedirectUseCaseTest {

    private val authRepository: AuthRepository = mockk()
    private val useCase = ValidateAuthRedirectUseCase(authRepository)

    @Test
    fun `Should return code When state matches`() = runTest {
        coEvery { authRepository.consumePendingState() } returns "state-1"

        val result = useCase(AuthRedirect(code = "code-1", state = "state-1", error = null))

        assertEquals(Result.Success("code-1"), result)
    }

    @Test
    fun `Should reject When state does not match`() = runTest {
        coEvery { authRepository.consumePendingState() } returns "state-1"

        val result = useCase(AuthRedirect(code = "code-1", state = "forged", error = null))

        assertEquals(Result.Error(AuthError.STATE_MISMATCH), result)
    }

    @Test
    fun `Should reject When no state is pending`() = runTest {
        coEvery { authRepository.consumePendingState() } returns null

        val result = useCase(AuthRedirect(code = "code-1", state = "state-1", error = null))

        assertEquals(Result.Error(AuthError.STATE_MISMATCH), result)
    }

    @Test
    fun `Should reject When redirect has no state`() = runTest {
        coEvery { authRepository.consumePendingState() } returns "state-1"

        val result = useCase(AuthRedirect(code = "code-1", state = null, error = null))

        assertEquals(Result.Error(AuthError.STATE_MISMATCH), result)
    }

    @Test
    fun `Should report access denied When redirect carries an error`() = runTest {
        coEvery { authRepository.consumePendingState() } returns "state-1"

        val result = useCase(AuthRedirect(code = null, state = "state-1", error = "access_denied"))

        assertEquals(Result.Error(AuthError.ACCESS_DENIED), result)
    }

    @Test
    fun `Should report missing code When redirect has neither code nor error`() = runTest {
        coEvery { authRepository.consumePendingState() } returns "state-1"

        val result = useCase(AuthRedirect(code = null, state = "state-1", error = null))

        assertEquals(Result.Error(AuthError.MISSING_CODE), result)
    }

    @Test
    fun `Should consume the pending state on every attempt`() = runTest {
        coEvery { authRepository.consumePendingState() } returns "state-1"

        useCase(AuthRedirect(code = "code-1", state = "forged", error = null))

        coVerify(exactly = 1) { authRepository.consumePendingState() }
    }
}
