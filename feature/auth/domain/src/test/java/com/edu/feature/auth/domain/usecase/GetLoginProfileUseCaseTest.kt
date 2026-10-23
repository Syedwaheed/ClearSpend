package com.edu.feature.auth.domain.usecase

import com.edu.feature.auth.domain.fake.FakeUserPreferencesStorage
import com.edu.feature.auth.domain.model.LoginProfile
import com.edu.feature.auth.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class GetLoginProfileUseCaseTest {

    private val authRepository: AuthRepository = mockk()

    @Test
    fun `Should combine stored name and last login`() = runTest {
        coEvery { authRepository.getAccountHolderName() } returns "Jane Doe"
        val useCase = GetLoginProfileUseCase(
            authRepository,
            FakeUserPreferencesStorage(lastLoginEpochMillis = 1_000L),
        )

        assertEquals(LoginProfile("Jane Doe", Instant.ofEpochMilli(1_000L)), useCase())
    }

    @Test
    fun `Should treat a blank name as unknown`() = runTest {
        coEvery { authRepository.getAccountHolderName() } returns "  "
        val useCase = GetLoginProfileUseCase(authRepository, FakeUserPreferencesStorage())

        assertEquals(LoginProfile(null, null), useCase())
    }
}
