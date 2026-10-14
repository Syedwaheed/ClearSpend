package com.edu.core.domain.repository

import com.edu.core.domain.model.StandingOrder
import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Result

interface StandingOrderRepository {
    suspend fun getStandingOrders(accountId: String): Result<List<StandingOrder>, DataError>
}
