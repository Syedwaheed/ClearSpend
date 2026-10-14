package com.edu.core.domain.repository

import com.edu.core.domain.model.DirectDebit
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result

interface DirectDebitRepository {
    suspend fun getDirectDebits(accountId: String): Result<List<DirectDebit>, DataError>
}
