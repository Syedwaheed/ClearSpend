package com.edu.feature.auth.presentation.mapper

import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Error
import com.edu.feature.auth.domain.model.AuthError
import com.edu.feature.auth.presentation.model.ConnectionErrorUi

class ConnectionErrorUiMapperImpl : ConnectionErrorUiMapper {
    override fun map(domain: Error): ConnectionErrorUi = when (domain) {
        AuthError.STATE_MISMATCH -> ConnectionErrorUi.SECURITY_CHECK_FAILED
        AuthError.ACCESS_DENIED -> ConnectionErrorUi.CANCELLED
        AuthError.MISSING_CODE -> ConnectionErrorUi.CODE_REJECTED
        DataError.NetworkError.UNAUTHORIZED -> ConnectionErrorUi.CODE_REJECTED
        DataError.NetworkError.NO_INTERNET -> ConnectionErrorUi.NO_INTERNET
        DataError.NetworkError.REQUEST_TIMEOUT -> ConnectionErrorUi.TIMEOUT
        DataError.NetworkError.SERVER_ERROR,
        DataError.NetworkError.TOO_MANY_REQUESTS -> ConnectionErrorUi.SERVER
        else -> ConnectionErrorUi.UNKNOWN
    }
}
