package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PromptHistoryDao {
    @Query("SELECT * FROM prompt_history ORDER BY timestamp DESC")
    fun getAll(): Flow<List<PromptHistoryEntity>>

    @Query("SELECT * FROM prompt_history WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): PromptHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PromptHistoryEntity): Long

    @Update
    suspend fun update(entity: PromptHistoryEntity)

    @Query("DELETE FROM prompt_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM prompt_history")
    suspend fun clearAll()
}
