package com.edu.core.presentation.ui.di

import com.edu.core.presentation.ui.formatter.AmountFormatter
import com.edu.core.presentation.ui.formatter.AmountFormatterImpl
import com.edu.core.presentation.ui.formatter.DateFormatter
import com.edu.core.presentation.ui.formatter.DateFormatterImpl
import com.edu.core.presentation.ui.formatter.IbanFormatter
import com.edu.core.presentation.ui.formatter.IbanFormatterImpl
import com.edu.core.presentation.ui.mapper.AccountUiMapper
import com.edu.core.presentation.ui.mapper.AccountUiMapperImpl
import com.edu.core.presentation.ui.mapper.BalanceUiMapper
import com.edu.core.presentation.ui.mapper.BalanceUiMapperImpl
import com.edu.core.presentation.ui.mapper.TransactionUiMapper
import com.edu.core.presentation.ui.mapper.TransactionUiMapperImpl
import org.koin.dsl.module
import java.time.Clock
import java.util.Locale

val coreUiModule = module {
    single<AmountFormatter> { AmountFormatterImpl(Locale.UK) }
    single<DateFormatter> { DateFormatterImpl(Locale.UK, Clock.systemDefaultZone()) }
    single<IbanFormatter> { IbanFormatterImpl() }

    factory<AccountUiMapper> { AccountUiMapperImpl(get()) }
    factory<BalanceUiMapper> { BalanceUiMapperImpl(get(), get()) }
    factory<TransactionUiMapper> { TransactionUiMapperImpl(get(), get()) }
}
