package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prompt_history")
data class PromptHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val scriptExcerpt: String,
    val fullScript: String,
    val generatedPrompt: String,
    val aspectRatio: String,
    val resolution: String,
    val duration: String,
    val videoStyle: String,
    val location: String,
    val presenter: String,
    val camera: String,
    val hasReferenceImage: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val cloudDocumentId: String? = null,
    val isSyncedWithCloud: Boolean = false
)
