package com.example.data.repository

import com.example.data.local.PromptHistoryDao
import com.example.data.local.PromptHistoryEntity
import kotlinx.coroutines.flow.Flow

class PromptHistoryRepository(private val dao: PromptHistoryDao) {
    val allHistory: Flow<List<PromptHistoryEntity>> = dao.getAll()

    suspend fun getById(id: Long): PromptHistoryEntity? = dao.getById(id)

    suspend fun insert(entity: PromptHistoryEntity): Long = dao.insert(entity)

    suspend fun update(entity: PromptHistoryEntity) = dao.update(entity)

    suspend fun updateCloudSyncStatus(id: Long, isSynced: Boolean, cloudId: String?) =
        dao.updateCloudSyncStatus(id, isSynced, cloudId)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun clearAll() = dao.clearAll()
}
