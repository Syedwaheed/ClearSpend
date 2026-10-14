package com.edu.core.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private typealias FakeError = DataError.NetworkError

class ResultTest {

    @Test
    fun `onSuccess invokes action When result is Success`() {
        val result: Result<Int, FakeError> = Result.Success(5)
        var observed = -1

        result.onSuccess { observed = it }

        assertEquals(5, observed)
    }

    @Test
    fun `onSuccess does not invoke action When result is Error`() {
        val result: Result<Int, FakeError> = Result.Error(FakeError.SERVER_ERROR)
        var invoked = false

        result.onSuccess { invoked = true }

        assertFalse(invoked)
    }

    @Test
    fun `onError invokes action with unwrapped error When result is Error`() {
        val result: Result<Int, FakeError> = Result.Error(FakeError.SERVER_ERROR)
        var observed: FakeError? = null

        result.onError { observed = it }

        assertEquals(FakeError.SERVER_ERROR, observed)
    }

    @Test
    fun `map transforms success data`() {
        val result: Result<Int, FakeError> = Result.Success(2)

        val mapped = result.map { it * 10 }

        assertEquals(Result.Success(20), mapped)
    }

    @Test
    fun `map preserves error without invoking transform`() {
        val result: Result<Int, FakeError> = Result.Error(FakeError.SERVER_ERROR)

        val mapped = result.map { it * 10 }

        assertEquals(Result.Error(FakeError.SERVER_ERROR), mapped)
    }

    @Test
    fun `asEmptyDataResult discards success data`() {
        val result: Result<Int, FakeError> = Result.Success(99)

        val empty: EmptyResult<FakeError> = result.asEmptyDataResult()

        assertTrue(empty is Result.Success)
    }
}
