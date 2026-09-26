package com.example.data.remote

import android.util.Log
import com.example.data.preferences.UserPreferencesManager
import com.example.model.PromptConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class PromptGeneratorService {

    companion object {
        private const val TAG = "PromptGeneratorService"
        const val API_URL = UserPreferencesManager.DEFAULT_API_URL
        const val MODEL_NAME = UserPreferencesManager.DEFAULT_MODEL
        const val FALLBACK_MODEL = "qwen/qwen3.7-max:free"

        const val SYSTEM_PROMPT = """You are an expert AI Video Prompt Engineer.

Your job is to convert the user's dialogue script, reference-image information, and video settings into a professional production-ready AI VIDEO MASTER PROMPT.

The user may provide:
- Bengali dialogue
- English dialogue
- Banglish dialogue
- Reference image
- Presenter instructions
- Location
- Camera instructions
- Duration
- Aspect ratio
- B-roll requirements
- Visual instructions

Create a complete prompt that can be copied directly into an AI video generation platform.

IMPORTANT:
If the user provides exact dialogue, preserve the dialogue exactly.
Never unnecessarily rewrite, shorten, translate, reorder, or modify the dialogue.

The final prompt must include:
1. Video format
2. Resolution
3. Duration
4. Reference character
5. Character consistency
6. Location
7. Environment
8. Wardrobe
9. Camera
10. Camera movement
11. Lighting
12. Facial expression
13. Body language
14. Dialogue
15. Voice
16. Lip-sync
17. B-roll
18. Dialogue-to-visual synchronization
19. Audio
20. Continuity
21. Negative instructions
22. Final generation instruction

REFERENCE IMAGE RULE:
If a reference image is provided, treat it as the primary visual character reference.
Preserve:
- Face
- Identity
- Hairstyle
- Clothing
- Accessories
- Skin tone
- Body proportions
- General appearance
Do not identify the person.
Do not redesign the person.

ONE VOICE TAKE:
The generated prompt must explicitly instruct:
- One single voice
- One continuous take
- No repeated dialogue
- No repeated words
- No echo
- No duplicate voice
- No second take
- No restart
- No voice overlap
- No unnecessary pauses
- Voice stops immediately after the final word

LIP-SYNC:
Require natural and accurate lip-sync.

B-ROLL:
If B-roll is enabled or appropriate, synchronize every visual with the exact meaning of the spoken words.
Example:
"রাস্তা-ঘাট" -> realistic road improvement visual
"শিক্ষা" -> classroom/school visual
"স্বাস্থ্য" -> healthcare visual
"কর্মসংস্থান" -> legitimate work/training visual
Do not add random B-roll.
If B-roll is disabled, keep the presenter visible throughout unless otherwise instructed.

CAMERA:
Use realistic professional cinematography.
Do not use excessive camera movement.

LIGHTING:
Use realistic cinematic lighting appropriate to the environment.

AUDIO:
Prioritize clean dialogue.
Keep ambient audio subtle.
Do not overpower the voice.

ON-SCREEN TEXT:
Do not add text unless the user explicitly requests it.

NEGATIVE PROMPT:
Include relevant restrictions such as:
NO face change.
NO identity change.
NO character replacement.
NO clothing change.
NO hairstyle change.
NO duplicate person.
NO duplicate voice.
NO repeated dialogue.
NO echo.
NO lip-sync mismatch.
NO distorted face.
NO unnatural mouth movement.
NO deformed hands.
NO extra fingers.
NO random B-roll.
NO unrelated background.
NO unwanted text.
NO watermark.
NO excessive camera shake.
NO unnecessary cuts.

OUTPUT:
Return only the final professional MASTER VIDEO PROMPT.
Do not explain your reasoning.
Do not discuss how the prompt was created."""
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    fun buildUserPrompt(config: PromptConfig): String {
        val effectiveLocation = if (config.location == "Custom" && config.customLocation.isNotBlank()) {
            config.customLocation
        } else config.location

        val effectiveCamera = if (config.camera == "Custom" && config.customCamera.isNotBlank()) {
            config.customCamera
        } else config.camera

        val effectiveStyle = if (config.videoStyle == "Custom" && config.customStyle.isNotBlank()) {
            config.customStyle
        } else config.videoStyle

        val effectiveDuration = if (config.duration == "Custom" && config.customDuration.isNotBlank()) {
            config.customDuration
        } else config.duration

        val effectiveText = if (config.onScreenText == "Custom" && config.customText.isNotBlank()) {
            config.customText
        } else config.onScreenText

        return buildString {
            appendLine("GENERATE AI VIDEO MASTER PROMPT WITH THE FOLLOWING SPECIFICATIONS:")
            appendLine()
            appendLine("=== DIALOGUE / VOICE SCRIPT ===")
            appendLine("\"\"\"")
            appendLine(config.script.trim())
            appendLine("\"\"\"")
            appendLine()
            appendLine("=== VIDEO FORMAT & TECHNICAL SETTINGS ===")
            appendLine("- Aspect Ratio: ${config.aspectRatio}")
            appendLine("- Resolution: ${config.resolution}")
            appendLine("- Target Duration: $effectiveDuration")
            appendLine("- Video Style: $effectiveStyle")
            appendLine()
            appendLine("=== VISUAL & CINEMATIC SETTINGS ===")
            appendLine("- Location / Environment: $effectiveLocation")
            appendLine("- Presenter Type: ${config.presenter}")
            if (config.referenceImageUri != null) {
                appendLine("- Reference Image: Provided. Preserve the exact face, identity, hair, skin tone, clothing, and proportions from reference image.")
                if (config.referenceImageDescription.isNotBlank()) {
                    appendLine("  Additional Character Notes: ${config.referenceImageDescription}")
                }
            } else {
                appendLine("- Reference Image: None provided. Generate realistic presenter matching the script context.")
            }
            appendLine("- Camera Angle & Framing: $effectiveCamera")
            appendLine("- B-Roll Generation: ${config.bRoll} (Synchronize contextually with spoken words)")
            appendLine("- On-Screen Text: $effectiveText")
            appendLine()
            appendLine("=== STRICT MANDATES & GENERATION OPTIONS ===")
            if (config.preserveExactDialogue) appendLine("- [MANDATORY] Preserve exact dialogue without any modification, translation, or shortening.")
            if (config.maintainCharacterConsistency) appendLine("- [MANDATORY] Maintain 100% character identity, facial features, wardrobe, and appearance consistency.")
            if (config.synchronizeBroll) appendLine("- [MANDATORY] Synchronize all visual cutaways/B-roll directly with the exact meaning of spoken dialogue words.")
            if (config.naturalLipSync) appendLine("- [MANDATORY] Precise, natural lip-synchronization matching speech cadences.")
            if (config.oneContinuousVoiceTake) appendLine("- [MANDATORY] Single voice, one continuous audio take, zero audio repetition, zero restarts, cleanly ending with last word.")
            if (config.noDialogueRepetition) appendLine("- [MANDATORY] No dialogue loops, echo, or duplicate audio tracks.")
            if (config.professionalCameraDirection) appendLine("- [MANDATORY] Professional cinematic camera direction, smooth realistic movements.")
            if (config.cinematicLighting) appendLine("- [MANDATORY] High-end cinematic lighting appropriate to $effectiveLocation.")
            if (config.negativePrompt) appendLine("- [MANDATORY] Explicit comprehensive negative constraints (no morphing, no artifacts, no watermarks, no unwanted text).")
            if (config.customInstructions.isNotBlank()) {
                appendLine()
                appendLine("=== ADDITIONAL CUSTOM INSTRUCTIONS ===")
                appendLine(config.customInstructions.trim())
            }
            appendLine()
            appendLine("Please generate the complete, ready-to-use AI VIDEO MASTER PROMPT now.")
        }
    }

    suspend fun generateMasterPrompt(
        config: PromptConfig,
        apiKey: String
    ): Result<String> = withContext(Dispatchers.IO) {
        if (config.script.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Please enter your dialogue script first."))
        }

        val effectiveApiKey = apiKey.ifBlank { UserPreferencesManager.DEFAULT_INBUILT_API_KEY }
        val userPrompt = buildUserPrompt(config)

        // Try primary model (DeepSeek v4.1 Flash)
        val primaryResult = invokeApi(MODEL_NAME, userPrompt, effectiveApiKey)
        if (primaryResult.isSuccess) {
            return@withContext primaryResult
        }

        val primaryError = primaryResult.exceptionOrNull()
        Log.w(TAG, "Primary model ($MODEL_NAME) failed: ${primaryError?.message}. Attempting fallback model ($FALLBACK_MODEL)...")

        // Try fallback model (Qwen 3.7 Max Free)
        val fallbackResult = invokeApi(FALLBACK_MODEL, userPrompt, effectiveApiKey)
        if (fallbackResult.isSuccess) {
            return@withContext fallbackResult
        }

        Log.w(TAG, "Both cloud models failed. Synthesizing high-fidelity local master prompt.")
        val synthesized = generateHighFidelityLocalPrompt(config)
        Result.success(synthesized)
    }

    private fun invokeApi(model: String, prompt: String, apiKey: String): Result<String> {
        try {
            val jsonBody = JSONObject().apply {
                put("model", model)
                val messages = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", SYSTEM_PROMPT)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                }
                put("messages", messages)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonBody.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    Log.e(TAG, "API call for $model failed with HTTP ${response.code}: $responseBody")
                    return Result.failure(IOException("HTTP ${response.code}: $responseBody"))
                }

                if (responseBody.isBlank()) {
                    return Result.failure(IOException("Empty response received from AI service."))
                }

                val jsonResponse = JSONObject(responseBody)
                val choices = jsonResponse.optJSONArray("choices")
                if (choices == null || choices.length() == 0) {
                    return Result.failure(IOException("No completion returned by AI model."))
                }

                val choice = choices.getJSONObject(0)
                val message = choice.optJSONObject("message")
                val content = message?.optString("content")?.trim() ?: ""

                if (content.isBlank()) {
                    return Result.failure(IOException("Empty AI generated prompt."))
                }

                return Result.success(content)
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun testConnection(apiKey: String): Result<String> = withContext(Dispatchers.IO) {
        val effectiveKey = apiKey.ifBlank { UserPreferencesManager.DEFAULT_INBUILT_API_KEY }
        try {
            // First test primary model
            val primaryTest = testSingleModel(MODEL_NAME, effectiveKey)
            if (primaryTest.isSuccess) {
                return@withContext Result.success("Connected to XKIRO AI! Active model: $MODEL_NAME")
            }

            // Test fallback model
            val fallbackTest = testSingleModel(FALLBACK_MODEL, effectiveKey)
            if (fallbackTest.isSuccess) {
                return@withContext Result.success("Connected to XKIRO AI! Active free model: $FALLBACK_MODEL (DeepSeek v4.1 will activate automatically when wallet is topped up).")
            }

            Result.failure(IOException(fallbackTest.exceptionOrNull()?.message ?: "Failed to connect to XKIRO API."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun testSingleModel(model: String, apiKey: String): Result<String> {
        return try {
            val jsonBody = JSONObject().apply {
                put("model", model)
                val messages = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", "Respond with 'OK' if you can read this message.")
                    })
                }
                put("messages", messages)
                put("max_tokens", 10)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonBody.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    Result.success("Success")
                } else {
                    Result.failure(IOException("HTTP ${response.code}: $body"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Local synthesis engine conforming strictly to the 22-part AI Video Master Prompt structure
     * defined in Section 15 of user guidelines.
     */
    fun generateHighFidelityLocalPrompt(config: PromptConfig): String {
        val dialogue = config.script.trim()
        val effectiveLocation = if (config.location == "Custom" && config.customLocation.isNotBlank()) {
            config.customLocation
        } else config.location

        val effectiveCamera = if (config.camera == "Custom" && config.customCamera.isNotBlank()) {
            config.customCamera
        } else config.camera

        val effectiveStyle = if (config.videoStyle == "Custom" && config.customStyle.isNotBlank()) {
            config.customStyle
        } else config.videoStyle

        val effectiveDuration = if (config.duration == "Custom" && config.customDuration.isNotBlank()) {
            config.customDuration
        } else config.duration

        val effectiveText = if (config.onScreenText == "Custom" && config.customText.isNotBlank()) {
            config.customText
        } else config.onScreenText

        val presenterDesc = when {
            config.isModelLocked && config.lockedModelName != null -> "LOCKED MODEL REFERENCE [${config.lockedModelName}]: Strict identity continuity adhering precisely to the attached AI Model Creator character (lock identical South Asian / Bangladeshi facial geometry, eye shape, nose structure, skin tone, hair styling, and body proportions across all scenes)"
            config.referenceImageUri != null -> "Consistent presenter directly referencing uploaded model image (match identical facial features, skin tone, hair styling, eye structure, body proportions, and wardrobe seamlessly)"
            config.presenter == "Female" -> "Professional female presenter, charismatic, natural expressions, neat contemporary attire"
            config.presenter == "Male" -> "Professional male presenter, engaging and authoritative presence, clean contemporary attire"
            config.presenter == "No presenter" -> "Voiceover narration driven visual sequence without on-camera presenter"
            else -> "Professional presenter matching scene tone and context"
        }

        val consistencyRule = if (config.isModelLocked) {
            "STRICT MODEL LOCK ACTIVE: Absolute 100% facial geometry, skin texture, hairstyle, eye color, and wardrobe preservation across all scenes and camera angles. Zero facial drift, zero morphing, zero identity replacement. The AI video generator MUST treat the locked model as the persistent recurring actor."
        } else {
            "Absolute 100% facial geometry, skin texture, hairstyle, eye color, and wardrobe preservation across all frames. Zero facial drift, zero morphing."
        }

        return buildString {
            appendLine("=== MASTER AI VIDEO GENERATION PROMPT ===")
            appendLine()
            appendLine("1. VIDEO FORMAT: ${config.aspectRatio} Aspect Ratio")
            appendLine("2. RESOLUTION: ${config.resolution} Ultra HD cinematic fidelity")
            appendLine("3. DURATION: $effectiveDuration")
            appendLine("4. REFERENCE CHARACTER: $presenterDesc")
            appendLine("5. CHARACTER CONSISTENCY: $consistencyRule")
            appendLine("6. LOCATION: $effectiveLocation")
            appendLine("7. ENVIRONMENT: Realistic, deeply textured atmosphere in $effectiveLocation with true-to-life environmental ambience and authentic depth.")
            appendLine("8. WARDROBE: Perfectly tailored modern professional attire matching the setting, keeping clothing colors and fabric textures constant throughout.")
            appendLine("9. CAMERA: Professional cinematography, $effectiveCamera framing with shallow depth of field, 35mm / 50mm anamorphic prime lens feel.")
            appendLine("10. CAMERA MOVEMENT: Smooth, controlled cinematic movement; subtle organic breathing motion; perfectly stabilized framing without jarring transitions.")
            appendLine("11. LIGHTING: Volumetric natural cinematic lighting suited for $effectiveLocation, soft key light on subject face, subtle rim backlight, rich natural shadows.")
            appendLine("12. FACIAL EXPRESSION: Expressive, authentic, contextually reactive micro-expressions aligned seamlessly with dialogue sentiment.")
            appendLine("13. BODY LANGUAGE: Natural, grounded posture and subtle hand gestures that punctuate key dialogue points without exaggerated motion.")
            appendLine("14. DIALOGUE (PRESERVE EXACT ORIGINAL):")
            appendLine("    \"\"\"")
            appendLine("    $dialogue")
            appendLine("    \"\"\"")
            appendLine("15. VOICE: Crystal clear single voice track, natural human cadence, authentic resonance, no synthetic digital artifacts.")
            appendLine("16. LIP-SYNC: Frame-accurate, natural lip-synchronization matching exact phonemes and syllables of the dialogue in a single unbroken take.")
            appendLine("17. B-ROLL: ${if (config.bRoll == "No") "Disabled. Maintain unbroken on-camera focus on subject throughout." else "Synchronized B-roll cutaways directly illustrating the spoken concepts in real time with cinematic photorealism."}")
            appendLine("18. DIALOGUE-TO-VISUAL SYNCHRONIZATION: Every cutaway or camera accentuation coincides precisely with spoken semantic cues; visual pacing harmonizes with vocal tempo.")
            appendLine("19. AUDIO: Pristine broadcast-grade vocal track in the foreground; very subtle, organic ambient room tone in the background that never overpowers the speech.")
            appendLine("20. CONTINUITY: One continuous take, single voice actor, zero restarts, no stutter, no echo, no repeated lines; voice track concludes cleanly precisely when the dialogue ends.")
            appendLine("21. NEGATIVE INSTRUCTIONS:")
            appendLine("    NO face change, NO identity drift, NO character replacement, NO wardrobe shifts, NO hairstyle alterations, NO duplicate presenter, NO duplicate voice tracks, NO repeated dialogue words, NO voice overlap, NO robotic lip-sync mismatch, NO distorted fingers or hands, NO uncanny valley mouth movements, NO random unrelated B-roll, ${if (effectiveText == "None") "NO on-screen text, NO captions, " else ""}NO watermarks, NO excessive camera shake, NO unnatural morphing cuts.")
            appendLine("22. FINAL GENERATION INSTRUCTION: Render this video as a high-end $effectiveStyle production matching Google Flow and modern AI video generator standards with supreme photorealism, exact dialogue preservation, and flawless audiovisual coherence.")
            if (config.customInstructions.isNotBlank()) {
                appendLine()
                appendLine("ADDITIONAL USER DIRECTIVES:")
                appendLine(config.customInstructions.trim())
            }
        }
    }
}
