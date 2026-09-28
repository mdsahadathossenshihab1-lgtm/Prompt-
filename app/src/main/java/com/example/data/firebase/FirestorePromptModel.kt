package com.example.data.firebase

data class FirestorePromptModel(
    val id: String = "",
    val title: String = "",
    val scriptExcerpt: String = "",
    val fullScript: String = "",
    val generatedPrompt: String = "",
    val aspectRatio: String = "9:16",
    val resolution: String = "1080x1920",
    val duration: String = "8 seconds",
    val videoStyle: String = "Cinematic",
    val location: String = "Outdoor",
    val presenter: String = "Male",
    val camera: String = "Medium shot",
    val hasReferenceImage: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val isPublic: Boolean = false,
    val userId: String = ""
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "title" to title,
            "scriptExcerpt" to scriptExcerpt,
            "fullScript" to fullScript,
            "generatedPrompt" to generatedPrompt,
            "aspectRatio" to aspectRatio,
            "resolution" to resolution,
            "duration" to duration,
            "videoStyle" to videoStyle,
            "location" to location,
            "presenter" to presenter,
            "camera" to camera,
            "hasReferenceImage" to hasReferenceImage,
            "timestamp" to timestamp,
            "isPublic" to isPublic,
            "userId" to userId
        )
    }

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): FirestorePromptModel {
            return FirestorePromptModel(
                id = id,
                title = map["title"] as? String ?: "",
                scriptExcerpt = map["scriptExcerpt"] as? String ?: "",
                fullScript = map["fullScript"] as? String ?: "",
                generatedPrompt = map["generatedPrompt"] as? String ?: "",
                aspectRatio = map["aspectRatio"] as? String ?: "9:16",
                resolution = map["resolution"] as? String ?: "1080x1920",
                duration = map["duration"] as? String ?: "8 seconds",
                videoStyle = map["videoStyle"] as? String ?: "Cinematic",
                location = map["location"] as? String ?: "Outdoor",
                presenter = map["presenter"] as? String ?: "Male",
                camera = map["camera"] as? String ?: "Medium shot",
                hasReferenceImage = map["hasReferenceImage"] as? Boolean ?: false,
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                isPublic = map["isPublic"] as? Boolean ?: false,
                userId = map["userId"] as? String ?: ""
            )
        }
    }
}
