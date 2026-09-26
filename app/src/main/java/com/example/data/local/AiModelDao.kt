package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AiModelDao {

    @Query("SELECT * FROM ai_models ORDER BY createdAt DESC")
    fun getAllModels(): Flow<List<AiModelEntity>>

    @Query("SELECT * FROM ai_models WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteModels(): Flow<List<AiModelEntity>>

    @Query("SELECT * FROM ai_models WHERE id = :id LIMIT 1")
    suspend fun getModelById(id: Long): AiModelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModel(model: AiModelEntity): Long

    @Update
    suspend fun updateModel(model: AiModelEntity)

    @Delete
    suspend fun deleteModel(model: AiModelEntity)

    @Query("DELETE FROM ai_models WHERE id = :id")
    suspend fun deleteModelById(id: Long)

    @Query("UPDATE ai_models SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)

    @Query("UPDATE ai_models SET isLocked = :isLocked WHERE id = :id")
    suspend fun updateLockStatus(id: Long, isLocked: Boolean)

    @Query("SELECT COUNT(*) FROM ai_models")
    suspend fun getCount(): Int
}
