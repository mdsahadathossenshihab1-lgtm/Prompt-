package com.example.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.example.data.preferences.UserPreferencesManager
import com.example.model.AiModelConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit
import kotlin.random.Random

data class GeneratedModelResult(
    val imageUrl: String,
    val localFilePath: String? = null,
    val detailedPrompt: String,
    val seed: Long,
    val suggestedName: String
)

class AiModelGeneratorService(private val context: Context) {

    private val prefs = UserPreferencesManager(context)
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val TAG = "AiModelGenService"
    }

    /**
     * Synthesizes a photorealistic 25-point visual specification conforming to Section 12
     * of the Master Build Prompt instructions.
     */
    fun build25PointPrompt(config: AiModelConfig, seed: Long): String {
        val userDesc = config.description.trim()

        val effectiveAge = if (config.ageRange == "Custom" && config.customAge.isNotBlank()) {
            config.customAge
        } else config.ageRange

        val effectiveLocation = if (config.location == "Custom" && config.customLocation.isNotBlank()) {
            config.customLocation
        } else config.location

        val effectiveStyle = if (config.style == "Custom" && config.customStyle.isNotBlank()) {
            config.customStyle
        } else config.style

        // Smart Bangladeshi inference heuristics
        val isBengaliContext = userDesc.contains("বাংলা", ignoreCase = true) ||
                userDesc.contains("bangla", ignoreCase = true) ||
                userDesc.contains("বাংলাদেশ", ignoreCase = true) ||
                userDesc.contains("নির্বাচন", ignoreCase = true) ||
                userDesc.contains("গ্রাম", ignoreCase = true) ||
                config.modelType.contains("Election", ignoreCase = true) ||
                config.regionContext != "None"

        val ethnicityDesc = if (isBengaliContext || config.appearance.contains("Bangladeshi", ignoreCase = true)) {
            val regionNote = if (config.regionContext != "None") "with subtle ${config.regionContext} regional context" else ""
            "Authentic South Asian Bangladeshi heritage $regionNote, natural warm olive/brown skin tone, realistic facial bone structure, natural dark brown/black hair, lifelike authentic proportions"
        } else {
            "Professional South Asian / international model, authentic natural skin tones, realistic bone structure"
        }

        val microphoneDesc = when (config.microphoneOption) {
            "No microphone" -> "No microphone, no visible audio equipment on body"
            "Handheld mic" -> "Holding professional black broadcast handheld news microphone"
            "Headset" -> "Wearing sleek lightweight presenter headset microphone"
            else -> "Discreet miniature black lavalier lapel microphone neatly clipped to upper chest clothing collar"
        }

        val dimensions = when (config.aspectRatio) {
            "16:9" -> "16:9 widescreen composition (1920x1080)"
            "1:1" -> "1:1 square composition (1080x1080)"
            else -> "9:16 vertical composition (1080x1920) optimized for vertical Reels and video presenter generation"
        }

        val twoPersonDesc = if (config.isTwoPersonScene) {
            """
            TWO-PERSON SCENE:
            Model 1 (Primary): ${config.gender}, ${effectiveAge} years old, wearing ${config.clothing}.
            Model 2 (Secondary): ${config.model2Gender}, wearing ${config.model2Clothing}. ${config.model2Description}
            Both subjects standing naturally together in frame, professional cooperative chemistry, identical lighting and depth.
            """.trimIndent()
        } else ""

        return buildString {
            appendLine("RAW PHOTOGRAPH, ULTRA-REALISTIC CINEMATIC PRODUCTION STILL:")
            appendLine("1. SUBJECT: Professional ${config.gender} video presenter (${effectiveAge} age bracket), $ethnicityDesc.")
            if (config.isTwoPersonScene) {
                appendLine(twoPersonDesc)
            }
            appendLine("2. APPEARANCE: ${config.appearance.ifBlank { "Authentic, charismatic, healthy natural skin texture, clear expressive eyes" }}.")
            appendLine("3. FACE: Razor-sharp facial features, natural symmetry without artificial smoothing, subtle realistic skin pores and micro-textures, genuine engaging expression.")
            appendLine("4. HAIR: Perfectly groomed natural dark hair, realistic individual strands, authentic styling suited for a professional broadcast presenter.")
            if (config.gender == "Male") {
                appendLine("5. FACIAL HAIR: Cleanly groomed neat beard or smooth clean-shaven skin matching executive standards.")
            } else {
                appendLine("5. MAKEUP & STYLING: Tasteful, minimal camera-ready makeup, elegant authentic South Asian styling, natural lip tone.")
            }
            appendLine("6. SKIN TONE: Rich natural warm South Asian skin tone with realistic subsurface light scattering, no plastic sheen, no over-smoothing.")
            appendLine("7. BODY TYPE: Anatomically accurate, natural presenter posture, relaxed shoulders, grounded upright stance.")
            appendLine("8. CLOTHING: ${config.clothing.ifBlank { "Smart, neatly pressed contemporary professional outfit matching context" }}, authentic fabric folds and realistic textile texture.")
            appendLine("9. ACCESSORIES: $microphoneDesc, subtle wristwatch, no unnecessary bulky props.")
            appendLine("10. POSE: Standing naturally facing camera, relaxed posture, hands positioned naturally, slight confident poise suitable for video-generation reference.")
            appendLine("11. EXPRESSION: ${config.expression.ifBlank { "Calm, confident, warm professional smile, steady direct eye contact with camera lens" }}.")
            appendLine("12. CAMERA: Photographed with 85mm f/1.4 portrait prime lens on professional full-frame cinema camera, eye-level angle.")
            appendLine("13. FRAMING: ${config.cameraFraming} framing, subject centered with generous headroom and breathing space.")
            appendLine("14. COMPOSITION: $dimensions, professional depth separation, rule of thirds balance.")
            appendLine("15. ENVIRONMENT: $effectiveLocation setting with genuine environmental depth and texture.")
            appendLine("16. BACKGROUND: Softly defocused background of $effectiveLocation, natural atmospheric separation that never overpowers the presenter.")
            appendLine("17. LIGHTING: Soft wrap-around three-point key light on face, subtle cinematic rim light outlining shoulders and hair, natural ambient fill.")
            appendLine("18. COLOR: Natural realistic color science, lifelike skin tone fidelity, calibrated white balance.")
            appendLine("19. DEPTH OF FIELD: Shallow cinematic depth of field, sharp subject, buttery smooth bokeh background.")
            appendLine("20. REALISM: Supreme photorealism, professional commercial photography, authentic South Asian appearance, zero cartoon/CGI aesthetic.")
            appendLine("21. ASPECT RATIO: ${config.aspectRatio}")
            appendLine("22. VIDEO REFERENCE SUITABILITY: Optimal reference photo for Google Flow and Sora video generation with clear facial silhouette and stable identity.")
            appendLine("23. CONTINUITY: Seed #$seed, fixed facial geometry and consistent character identity.")
            appendLine("24. NEGATIVE CONSTRAINTS: NO deformed hands, NO extra fingers, NO duplicated limbs, NO plastic skin, NO text, NO watermarks, NO election propaganda slogans or party banners, NO artificial glare, NO oversaturated CGI, NO distorted eyes.")
            if (userDesc.isNotBlank()) {
                appendLine("25. USER CUSTOM INSTRUCTION: \"$userDesc\"")
            }
            if (config.additionalInstructions.isNotBlank()) {
                appendLine("ADDITIONAL NOTES: ${config.additionalInstructions.trim()}")
            }
        }
    }

    /**
     * Generates a realistic human model image based on the user's config and prompt instructions.
     */
    suspend fun generateModelImage(
        config: AiModelConfig,
        isSimilar: Boolean = false
    ): Result<GeneratedModelResult> = withContext(Dispatchers.IO) {
        try {
            // Determine seed: if locked or similar, keep same seed base or vary slightly
            val seed = when {
                config.isModelLocked && config.lockedSeed != null -> config.lockedSeed
                isSimilar && config.lockedSeed != null -> config.lockedSeed + Random.nextLong(1, 5)
                else -> Random.nextLong(100000, 99999999)
            }

            val detailedPrompt = build25PointPrompt(config, seed)

            // Calculate width and height based on aspect ratio
            val (width, height) = when (config.aspectRatio) {
                "16:9" -> Pair(1280, 720)
                "1:1" -> Pair(1024, 1024)
                else -> Pair(768, 1344) // 9:16 vertical optimized
            }

            // Compact prompt for the image rendering pipeline
            val condensedPrompt = buildString {
                append("Professional award-winning portrait photograph, 8k resolution, ")
                append("${config.gender} presenter, ${config.ageRange} years old, ")
                if (config.appearance.isNotBlank()) append("${config.appearance}, ")
                else append("authentic South Asian Bangladeshi appearance, natural warm skin, ")
                append("${config.clothing}, ")
                append("in ${config.location} background, ")
                append("${config.cameraFraming} framing, eye-level, ")
                append("${config.expression}, ")
                append("photographed on 85mm lens, soft natural lighting, shallow depth of field, photorealistic, ultra-detailed skin texture, realistic hands, no watermark, no text")
                if (config.description.isNotBlank()) {
                    append(", ${config.description.take(150)}")
                }
            }

            val encodedPrompt = URLEncoder.encode(condensedPrompt, StandardCharsets.UTF_8.toString())
            val imageUrl = "https://image.pollinations.ai/prompt/$encodedPrompt?width=$width&height=$height&model=flux&nologo=true&seed=$seed"

            Log.d(TAG, "Requesting generated image from: $imageUrl")

            // Download and cache image locally so it is fast, offline-ready and can be passed to prompt generator
            var localPath: String? = null
            try {
                val request = Request.Builder().url(imageUrl).build()
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val bytes = response.body?.bytes()
                        if (bytes != null && bytes.isNotEmpty()) {
                            val dir = File(context.filesDir, "generated_models")
                            if (!dir.exists()) dir.mkdirs()
                            val file = File(dir, "model_${seed}_${System.currentTimeMillis()}.jpg")
                            FileOutputStream(file).use { it.write(bytes) }
                            localPath = file.absolutePath
                            Log.d(TAG, "Cached model image locally at: $localPath")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to download image locally, will use web URL: ${e.message}")
            }

            // Generate a smart suggested name
            val suggestedName = when {
                config.gender == "Male" && config.location == "Rural" -> "Male Rural Presenter"
                config.gender == "Female" && config.location == "Studio" -> "Female Studio Presenter"
                config.gender == "Male" && config.modelType.contains("News") -> "Male News Presenter"
                config.gender == "Female" && config.modelType.contains("News") -> "Female News Anchor"
                config.gender == "Male" && config.modelType.contains("Corporate") -> "Corporate Executive Male"
                config.gender == "Female" && config.modelType.contains("Corporate") -> "Corporate Female Leader"
                else -> "${config.gender} ${config.location} Model"
            }

            Result.success(
                GeneratedModelResult(
                    imageUrl = localPath ?: imageUrl,
                    localFilePath = localPath,
                    detailedPrompt = detailedPrompt,
                    seed = seed,
                    suggestedName = suggestedName
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error generating model image: ${e.message}", e)
            Result.failure(e)
        }
    }
}
