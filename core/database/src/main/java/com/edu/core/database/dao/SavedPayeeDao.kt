package com.edu.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.edu.core.database.entity.SavedPayeeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedPayeeDao {

    @Query("SELECT * FROM saved_payees ORDER BY createdAtEpochMillis DESC")
    fun observeSavedPayees(): Flow<List<SavedPayeeEntity>>

    @Insert
    suspend fun insert(savedPayee: SavedPayeeEntity): Long

    @Query("DELETE FROM saved_payees WHERE id = :id")
    suspend fun delete(id: Long)
}
