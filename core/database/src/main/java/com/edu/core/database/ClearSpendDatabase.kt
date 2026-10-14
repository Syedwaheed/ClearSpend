package com.edu.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.edu.core.database.dao.AccountDao
import com.edu.core.database.dao.BalanceDao
import com.edu.core.database.dao.CardBalanceDao
import com.edu.core.database.dao.CardDao
import com.edu.core.database.dao.DirectDebitDao
import com.edu.core.database.dao.SavedPayeeDao
import com.edu.core.database.dao.StandingOrderDao
import com.edu.core.database.dao.TransactionDao
import com.edu.core.database.entity.AccountEntity
import com.edu.core.database.entity.BalanceEntity
import com.edu.core.database.entity.CardBalanceEntity
import com.edu.core.database.entity.CardEntity
import com.edu.core.database.entity.DirectDebitEntity
import com.edu.core.database.entity.SavedPayeeEntity
import com.edu.core.database.entity.StandingOrderEntity
import com.edu.core.database.entity.TransactionEntity

@Database(
    entities = [
        AccountEntity::class,
        BalanceEntity::class,
        TransactionEntity::class,
        CardEntity::class,
        CardBalanceEntity::class,
        DirectDebitEntity::class,
        StandingOrderEntity::class,
        SavedPayeeEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class ClearSpendDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun balanceDao(): BalanceDao
    abstract fun transactionDao(): TransactionDao
    abstract fun cardDao(): CardDao
    abstract fun cardBalanceDao(): CardBalanceDao
    abstract fun directDebitDao(): DirectDebitDao
    abstract fun standingOrderDao(): StandingOrderDao
    abstract fun savedPayeeDao(): SavedPayeeDao
}
