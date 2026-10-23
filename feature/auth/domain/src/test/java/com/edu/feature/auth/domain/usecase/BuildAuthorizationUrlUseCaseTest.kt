package com.edu.feature.auth.domain.usecase

import com.edu.core.domain.util.Result
import com.edu.feature.auth.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class BuildAuthorizationUrlUseCaseTest {

    private val authRepository: AuthRepository = mockk()

    @Before
    fun setUp() {
        coEvery { authRepository.createAuthorizationUrl(any()) } returns Result.Success("https://auth")
    }

    @Test
    fun `Should offer mock plus every provider When sandbox and no bank picked`() = runTest {
        BuildAuthorizationUrlUseCase(authRepository, isSandbox = true)(selectedProviderId = null)

        coVerify { authRepository.createAuthorizationUrl(listOf("uk-cs-mock", "uk-ob-all", "uk-oauth-all")) }
    }

    @Test
    fun `Should offer mock plus picked bank When sandbox and bank picked`() = runTest {
        BuildAuthorizationUrlUseCase(authRepository, isSandbox = true)(selectedProviderId = "ob-barclays")

        coVerify { authRepository.createAuthorizationUrl(listOf("uk-cs-mock", "ob-barclays")) }
    }

    @Test
    fun `Should omit mock bank When not sandbox`() = runTest {
        BuildAuthorizationUrlUseCase(authRepository, isSandbox = false)(selectedProviderId = "ob-hsbc")

        coVerify { authRepository.createAuthorizationUrl(listOf("ob-hsbc")) }
    }

    @Test
    fun `Should not duplicate mock When mock bank picked`() = runTest {
        BuildAuthorizationUrlUseCase(authRepository, isSandbox = true)(selectedProviderId = "uk-cs-mock")

        coVerify { authRepository.createAuthorizationUrl(listOf("uk-cs-mock")) }
    }

    @Test
    fun `Should return the repository url`() = runTest {
        val result = BuildAuthorizationUrlUseCase(authRepository, isSandbox = true)(null)

        assertEquals(Result.Success("https://auth"), result)
    }
}
