package com.example.data.firebase

data class FirestoreAiModel(
    val id: String = "",
    val name: String = "",
    val imageUrl: String = "",
    val localFilePath: String? = null,
    val promptText: String = "",
    val gender: String = "Female",
    val modelType: String = "Realistic",
    val style: String = "Photorealistic",
    val environment: String = "Studio",
    val cameraFraming: String = "Medium Shot",
    val aspectRatio: String = "9:16",
    val seed: Long = 0L,
    val isFavorite: Boolean = false,
    val isLocked: Boolean = false,
    val userId: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "imageUrl" to imageUrl,
            "localFilePath" to localFilePath,
            "promptText" to promptText,
            "gender" to gender,
            "modelType" to modelType,
            "style" to style,
            "environment" to environment,
            "cameraFraming" to cameraFraming,
            "aspectRatio" to aspectRatio,
            "seed" to seed,
            "isFavorite" to isFavorite,
            "isLocked" to isLocked,
            "userId" to userId,
            "createdAt" to createdAt
        )
    }

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): FirestoreAiModel {
            return FirestoreAiModel(
                id = id,
                name = map["name"] as? String ?: "",
                imageUrl = map["imageUrl"] as? String ?: "",
                localFilePath = map["localFilePath"] as? String,
                promptText = map["promptText"] as? String ?: "",
                gender = map["gender"] as? String ?: "Female",
                modelType = map["modelType"] as? String ?: "Realistic",
                style = map["style"] as? String ?: "Photorealistic",
                environment = map["environment"] as? String ?: "Studio",
                cameraFraming = map["cameraFraming"] as? String ?: "Medium Shot",
                aspectRatio = map["aspectRatio"] as? String ?: "9:16",
                seed = (map["seed"] as? Number)?.toLong() ?: 0L,
                isFavorite = map["isFavorite"] as? Boolean ?: false,
                isLocked = map["isLocked"] as? Boolean ?: false,
                userId = map["userId"] as? String ?: "",
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}
