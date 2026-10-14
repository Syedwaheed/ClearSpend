package com.edu.core.domain.repository

import com.edu.core.domain.model.Account
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result

interface AccountRepository {
    suspend fun getAccounts(): Result<List<Account>, DataError>
}
