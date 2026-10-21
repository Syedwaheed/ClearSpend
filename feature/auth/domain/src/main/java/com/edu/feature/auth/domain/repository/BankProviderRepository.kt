package com.edu.feature.auth.domain.repository

import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result
import com.edu.feature.auth.domain.model.BankProvider

interface BankProviderRepository {
    /** Every bank offered on Bank Selection (06). */
    suspend fun getProviders(): Result<List<BankProvider>, DataError>

    /** The short list shown as "Supported banks" chips on Connect Bank (05). */
    suspend fun getFeaturedProviders(): Result<List<BankProvider>, DataError>
}
