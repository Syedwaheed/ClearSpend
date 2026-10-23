package com.edu.feature.auth.domain.usecase

import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.EmptyResult
import com.edu.core.domain.util.Result
import com.edu.feature.auth.domain.repository.AuthRepository

/** Exchanges the code for tokens and, on success, records the login time. */
class ConnectBankUseCase(
    private val authRepository: AuthRepository,
    private val recordSuccessfulLogin: RecordSuccessfulLoginUseCase,
) {
    suspend operator fun invoke(code: String): EmptyResult<DataError.NetworkError> {
        val result = authRepository.exchangeAuthorizationCode(code)
        if (result is Result.Success) {
            recordSuccessfulLogin()
        }
        return result
    }
}
