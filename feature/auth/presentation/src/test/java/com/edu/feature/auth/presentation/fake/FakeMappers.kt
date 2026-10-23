package com.edu.feature.auth.presentation.fake

import com.edu.core.domain.util.DataError
import com.edu.core.domain.util.Error
import com.edu.feature.auth.domain.model.AuthError
import com.edu.feature.auth.domain.model.BankProvider
import com.edu.feature.auth.domain.model.LoginProfile
import com.edu.feature.auth.presentation.mapper.BankUiMapper
import com.edu.feature.auth.presentation.mapper.ConnectionErrorUiMapper
import com.edu.feature.auth.presentation.mapper.WelcomeUiMapper
import com.edu.feature.auth.presentation.model.BankUi
import com.edu.feature.auth.presentation.model.ConnectionErrorUi
import com.edu.feature.auth.presentation.model.WelcomeUi

fun fakeBankUi(providerId: String) = BankUi(providerId, providerId, "X", 0L, "Personal")

class FakeBankUiMapper : BankUiMapper {
    override fun map(domain: BankProvider) = fakeBankUi(domain.providerId)
}

class FakeWelcomeUiMapper : WelcomeUiMapper {
    override fun map(domain: LoginProfile) = WelcomeUi(firstName = domain.accountHolderName)
}

/** Deterministic: offline is retryable, a state mismatch is a security failure, anything else is rejected. */
class FakeConnectionErrorUiMapper : ConnectionErrorUiMapper {
    override fun map(domain: Error) = when (domain) {
        DataError.NetworkError.NO_INTERNET -> ConnectionErrorUi.NO_INTERNET
        AuthError.STATE_MISMATCH -> ConnectionErrorUi.SECURITY_CHECK_FAILED
        else -> ConnectionErrorUi.CODE_REJECTED
    }
}
