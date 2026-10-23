package com.edu.feature.dashboard.domain.usecase

import com.edu.feature.dashboard.domain.fixture.clockAt
import com.edu.feature.dashboard.domain.model.GreetingPeriod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetGreetingUseCaseTest {

    @Test
    fun `Should say morning at 9am`() {
        assertEquals(GreetingPeriod.MORNING, GetGreetingUseCase(clockAt("2026-09-16T09:00:00Z"))(null).period)
    }

    @Test
    fun `Should say afternoon at noon`() {
        assertEquals(GreetingPeriod.AFTERNOON, GetGreetingUseCase(clockAt("2026-09-16T12:00:00Z"))(null).period)
    }

    @Test
    fun `Should say evening at 6pm`() {
        assertEquals(GreetingPeriod.EVENING, GetGreetingUseCase(clockAt("2026-09-16T18:00:00Z"))(null).period)
    }

    @Test
    fun `Should say evening after midnight`() {
        assertEquals(GreetingPeriod.EVENING, GetGreetingUseCase(clockAt("2026-09-16T02:00:00Z"))(null).period)
    }

    @Test
    fun `Should use the first word of the full name`() {
        assertEquals("Waheed", GetGreetingUseCase(clockAt("2026-09-16T09:00:00Z"))("  Waheed  Shah ").firstName)
    }

    @Test
    fun `Should have no name When the full name is missing`() {
        assertNull(GetGreetingUseCase(clockAt("2026-09-16T09:00:00Z"))(" ").firstName)
    }
}
