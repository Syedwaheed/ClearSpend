package com.edu.feature.auth.data.repository

import com.edu.core.domain.util.Result
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StaticBankProviderRepositoryTest {

    private val repository = StaticBankProviderRepository()

    @Test
    fun `Should list the ten banks from the design`() = runTest {
        val banks = (repository.getProviders() as Result.Success).data

        assertEquals(10, banks.size)
    }

    @Test
    fun `Should feature the first seven banks`() = runTest {
        val all = (repository.getProviders() as Result.Success).data
        val featured = (repository.getFeaturedProviders() as Result.Success).data

        assertEquals(all.take(7), featured)
    }

    @Test
    fun `Should use Open Banking provider ids`() = runTest {
        val banks = (repository.getProviders() as Result.Success).data

        assertTrue(banks.all { it.providerId.startsWith("ob-") })
    }
}
