package com.example.data.repository

import com.example.data.local.AiModelDao
import com.example.data.local.AiModelEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class AiModelRepository(private val aiModelDao: AiModelDao) {

    val allModels: Flow<List<AiModelEntity>> = aiModelDao.getAllModels().flowOn(Dispatchers.IO)
    val favoriteModels: Flow<List<AiModelEntity>> = aiModelDao.getFavoriteModels().flowOn(Dispatchers.IO)

    suspend fun getModelById(id: Long): AiModelEntity? = withContext(Dispatchers.IO) {
        aiModelDao.getModelById(id)
    }

    suspend fun insertModel(model: AiModelEntity): Long = withContext(Dispatchers.IO) {
        aiModelDao.insertModel(model)
    }

    suspend fun updateModel(model: AiModelEntity) = withContext(Dispatchers.IO) {
        aiModelDao.updateModel(model)
    }

    suspend fun deleteModel(model: AiModelEntity) = withContext(Dispatchers.IO) {
        aiModelDao.deleteModel(model)
    }

    suspend fun deleteModelById(id: Long) = withContext(Dispatchers.IO) {
        aiModelDao.deleteModelById(id)
    }

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        aiModelDao.updateFavoriteStatus(id, isFavorite)
    }

    suspend fun toggleLock(id: Long, isLocked: Boolean) = withContext(Dispatchers.IO) {
        aiModelDao.updateLockStatus(id, isLocked)
    }
}
