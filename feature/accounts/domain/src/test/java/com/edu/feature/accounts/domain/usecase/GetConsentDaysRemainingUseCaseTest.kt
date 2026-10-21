package com.edu.feature.accounts.domain.usecase

import com.edu.feature.accounts.domain.fixture.CLOCK
import com.edu.feature.accounts.domain.fixture.FakeUserPreferencesStorage
import com.edu.feature.accounts.domain.fixture.TODAY
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetConsentDaysRemainingUseCaseTest {

    private fun useCase(consentDay: Long?) = GetConsentDaysRemainingUseCase(FakeUserPreferencesStorage(consentDay), CLOCK)

    @Test
    fun `Should count down from 90 days after consent`() = runTest {
        assertEquals(68, useCase(TODAY.minusDays(22).toEpochDay())())
    }

    @Test
    fun `Should be 90 on the day of consent`() = runTest {
        assertEquals(90, useCase(TODAY.toEpochDay())())
    }

    @Test
    fun `Should not go below zero When consent has expired`() = runTest {
        assertEquals(0, useCase(TODAY.minusDays(120).toEpochDay())())
    }

    @Test
    fun `Should be null When no consent date is stored`() = runTest {
        assertNull(useCase(null)())
    }
}
