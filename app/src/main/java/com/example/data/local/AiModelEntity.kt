package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_models")
data class AiModelEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val imageUrl: String,
    val localFilePath: String? = null,
    val promptText: String,
    val gender: String,
    val modelType: String,
    val style: String,
    val environment: String,
    val cameraFraming: String,
    val aspectRatio: String,
    val seed: Long,
    val isFavorite: Boolean = false,
    val isLocked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
