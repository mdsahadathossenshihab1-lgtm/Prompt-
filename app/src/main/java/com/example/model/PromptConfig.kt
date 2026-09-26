package com.example.model

data class PromptConfig(
    val script: String = "",
    val referenceImageUri: String? = null,
    val referenceImageDescription: String = "",
    val aspectRatio: String = "9:16",
    val resolution: String = "1080x1920",
    val duration: String = "8 seconds",
    val videoStyle: String = "Cinematic",
    val location: String = "Outdoor",
    val presenter: String = "Male",
    val camera: String = "Medium shot",
    val bRoll: String = "Auto", // Auto, Yes, No
    val onScreenText: String = "None",
    val customText: String = "",
    val customLocation: String = "",
    val customCamera: String = "",
    val customStyle: String = "",
    val customDuration: String = "",
    val customInstructions: String = "",
    // Generation option checkboxes (all enabled by default)
    val preserveExactDialogue: Boolean = true,
    val maintainCharacterConsistency: Boolean = true,
    val synchronizeBroll: Boolean = true,
    val naturalLipSync: Boolean = true,
    val oneContinuousVoiceTake: Boolean = true,
    val noDialogueRepetition: Boolean = true,
    val professionalCameraDirection: Boolean = true,
    val cinematicLighting: Boolean = true,
    val negativePrompt: Boolean = true,
    // AI Model Creator Integration (Section 23: MODEL LOCKED)
    val isModelLocked: Boolean = false,
    val lockedModelName: String? = null
)
