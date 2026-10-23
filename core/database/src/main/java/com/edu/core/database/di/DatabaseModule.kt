package com.edu.core.database.di

import androidx.room.Room
import com.edu.core.database.ClearSpendDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private const val DATABASE_NAME = "clearspend.db"

/** Room database + DAOs (offline cache, DESIGN_SPEC §7). Included by :app. */
val databaseModule = module {
    single {
        Room.databaseBuilder(androidContext(), ClearSpendDatabase::class.java, DATABASE_NAME)
            // The cache is re-fetchable from TrueLayer, so a schema bump may simply drop it.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }
    single { get<ClearSpendDatabase>().accountDao() }
    single { get<ClearSpendDatabase>().balanceDao() }
    single { get<ClearSpendDatabase>().transactionDao() }
    single { get<ClearSpendDatabase>().cardDao() }
    single { get<ClearSpendDatabase>().cardBalanceDao() }
    single { get<ClearSpendDatabase>().directDebitDao() }
    single { get<ClearSpendDatabase>().standingOrderDao() }
    single { get<ClearSpendDatabase>().savedPayeeDao() }
}
