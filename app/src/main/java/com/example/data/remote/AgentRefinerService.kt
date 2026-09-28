package com.example.data.remote

import android.util.Log
import com.example.data.preferences.UserPreferencesManager
import com.example.model.AgentChatMessage
import com.example.model.AgentSender
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

data class AgentRefineResponse(
    val explanation: String,
    val updatedPrompt: String
)

class AgentRefinerService {

    companion object {
        private const val TAG = "AgentRefinerService"
        const val API_URL = UserPreferencesManager.DEFAULT_API_URL
        const val PRIMARY_MODEL = UserPreferencesManager.DEFAULT_MODEL
        const val SECONDARY_MODEL = UserPreferencesManager.MODEL_DEEPSEEK

        const val AGENT_SYSTEM_PROMPT = """You are 'PromptFlow Agent' - an elite AI Cinematographer, Visual Director, and Master AI Prompt Optimizer for video platforms (Google Flow, Sora, Kling, Runway Gen-3, Midjourney v6.1, Flux.1).

Your primary job is to interactively chat with the user, understand errors or adjustments needed in their AI prompt, and iteratively refine, fix, and optimize the prompt.

COMMON ISSUES USERS WILL ASK YOU TO FIX:
1. Camera Angle & Framing: e.g. change close-up to high-angle aerial drone shot, FPV drone dive, low-angle dynamic hero shot, Dutch angle, 50mm portrait prime, 24mm wide angle, steady gimbal tracking.
2. Visual Elements & Realism: e.g. fix facial warping, add micro-skin pores, realistic eye catchlights, eliminate artificial plastic look, Hassleblad 8K medium format texture.
3. Lighting & Ambience: e.g. change harsh light to moody golden hour, cyberpunk volumetric neon, soft diffused 3-point commercial studio lighting, natural dusk ambience.
4. Character Attire & Look: e.g. switch traditional attire to sharp executive suit, casual streetwear, adjust grooming and expression.
5. Background & Environment: e.g. modern Dhaka skyline, futuristic tech lab, bustling market street, serene lakeside, depth-of-field separation.
6. Pacing & Motion: e.g. smooth cinematic slow motion, subtle breathing camera movement, frame-accurate lip-sync synchronization.
7. Negative Constraints: e.g. eliminate morphing, glitches, unwanted text, watermarks, distorted hands.

STRICT INSTRUCTIONS FOR RESPONSE:
1. The user may speak in Bengali (বাংলা), English, or Banglish.
2. In your response:
   - Provide a helpful, clear, and polite explanation in BENGALI (বাংলা) explaining what errors you corrected and what enhancements were made.
   - Then provide the COMPLETE, UPDATED, production-ready Master Prompt in ENGLISH, enclosed strictly between:
     [UPDATED_PROMPT]
     <Your full updated prompt in English>
     [/UPDATED_PROMPT]
3. Maintain all existing dialogue and core video parameters unless the user requested changes to them.
4. Never return an empty prompt."""
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(35, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun refinePrompt(
        currentPrompt: String,
        chatHistory: List<AgentChatMessage>,
        userInstruction: String,
        apiKey: String
    ): Result<AgentRefineResponse> = withContext(Dispatchers.IO) {
        val effectiveApiKey = apiKey.ifBlank { UserPreferencesManager.DEFAULT_INBUILT_API_KEY }

        val messagesJson = JSONArray()
        // System Prompt
        messagesJson.put(JSONObject().apply {
            put("role", "system")
            put("content", AGENT_SYSTEM_PROMPT)
        })

        // Initial context message giving the current prompt
        messagesJson.put(JSONObject().apply {
            put("role", "user")
            put(
                "content",
                """Here is the active Master AI Prompt that requires refinement:
--- CURRENT PROMPT START ---
$currentPrompt
--- CURRENT PROMPT END ---
Please prepare to receive my adjustments and chat corrections."""
            )
        })

        messagesJson.put(JSONObject().apply {
            put("role", "assistant")
            put(
                "content",
                "আমি আপনার প্রম্পটটি বিশ্লেষণ করেছি। ক্যামেরা অ্যাঙ্গেল, ভিজ্যুয়াল, লাইটিং, মডেল বা ব্যাকগ্রাউন্ডে কী পরিবর্তন চান বলুন, আমি এখনি আপডেট করে দিচ্ছি।"
            )
        })

        // Prior conversation turns (up to last 6 for context efficiency)
        val recentHistory = chatHistory.takeLast(6)
        for (msg in recentHistory) {
            val role = if (msg.sender == AgentSender.USER) "user" else "assistant"
            val content = if (msg.sender == AgentSender.USER) {
                msg.messageText
            } else {
                buildString {
                    append(msg.messageText)
                    if (!msg.refinedPrompt.isNullOrBlank()) {
                        append("\n\n[UPDATED_PROMPT]\n")
                        append(msg.refinedPrompt)
                        append("\n[/UPDATED_PROMPT]")
                    }
                }
            }
            messagesJson.put(JSONObject().apply {
                put("role", role)
                put("content", content)
            })
        }

        // Current user instruction
        messagesJson.put(JSONObject().apply {
            put("role", "user")
            put(
                "content",
                """User Adjustment / Correction Request:
"$userInstruction"

Please apply these adjustments to the active prompt. Remember to provide the Bengali explanation followed by the complete updated prompt inside [UPDATED_PROMPT] and [/UPDATED_PROMPT]."""
            )
        })

        // First attempt with primary model
        val primaryResult = invokeChatApi(PRIMARY_MODEL, messagesJson, effectiveApiKey)
        if (primaryResult.isSuccess) {
            val raw = primaryResult.getOrNull() ?: ""
            return@withContext parseResponse(raw, currentPrompt)
        }

        val primaryErr = primaryResult.exceptionOrNull()
        Log.w(TAG, "Primary model failed: ${primaryErr?.message}. Falling back to $SECONDARY_MODEL")

        // Fallback attempt with secondary model
        val secondaryResult = invokeChatApi(SECONDARY_MODEL, messagesJson, effectiveApiKey)
        if (secondaryResult.isSuccess) {
            val raw = secondaryResult.getOrNull() ?: ""
            return@withContext parseResponse(raw, currentPrompt)
        }

        val secondaryErr = secondaryResult.exceptionOrNull()
        val errMsg = secondaryErr?.message ?: primaryErr?.message ?: "AI সার্ভারে কানেক্ট করা সম্ভব হয়নি।"
        Result.failure(IOException("এজেন্ট মোড এরর: $errMsg। দয়া করে ইন্টারনেট সংযোগ চেক করুন।"))
    }

    private fun invokeChatApi(model: String, messages: JSONArray, apiKey: String): Result<String> {
        try {
            val jsonBody = JSONObject().apply {
                put("model", model)
                put("messages", messages)
                put("temperature", 0.7)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonBody.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "PromptFlowAI/AgentMode")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    Log.e(TAG, "HTTP ${response.code} error: $body")
                    return Result.failure(IOException("HTTP ${response.code}: $body"))
                }

                if (body.isBlank()) {
                    return Result.failure(IOException("Empty response from AI server"))
                }

                val jsonResponse = JSONObject(body)
                if (jsonResponse.has("error")) {
                    val errorObj = jsonResponse.optJSONObject("error")
                    val errorMsg = errorObj?.optString("message") ?: "API Error"
                    return Result.failure(IOException(errorMsg))
                }

                val choices = jsonResponse.optJSONArray("choices")
                if (choices == null || choices.length() == 0) {
                    return Result.failure(IOException("No completion choices returned"))
                }

                val choice = choices.getJSONObject(0)
                val content = choice.optJSONObject("message")?.optString("content")?.trim() ?: ""
                if (content.isBlank()) {
                    return Result.failure(IOException("Blank response from model"))
                }

                return Result.success(content)
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    private fun parseResponse(rawResponse: String, originalPrompt: String): Result<AgentRefineResponse> {
        return try {
            val promptTagStart = "[UPDATED_PROMPT]"
            val promptTagEnd = "[/UPDATED_PROMPT]"

            val startIndex = rawResponse.indexOf(promptTagStart)
            val endIndex = rawResponse.indexOf(promptTagEnd)

            if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                val explanation = rawResponse.substring(0, startIndex).trim()
                val updatedPrompt = rawResponse.substring(startIndex + promptTagStart.length, endIndex).trim()

                Result.success(
                    AgentRefineResponse(
                        explanation = explanation.ifBlank { "আপনার নির্দেশনা অনুযায়ী প্রম্পটটি আপডেট করা হয়েছে।" },
                        updatedPrompt = updatedPrompt.ifBlank { originalPrompt }
                    )
                )
            } else if (rawResponse.contains("```")) {
                // Markdown code block fallback
                val codeBlockRegex = Regex("```(?:[a-zA-Z]*\n)?([\\s\\S]*?)```")
                val match = codeBlockRegex.find(rawResponse)
                if (match != null) {
                    val codeContent = match.groupValues[1].trim()
                    val explanation = rawResponse.replace(match.value, "").trim()
                    Result.success(
                        AgentRefineResponse(
                            explanation = explanation.ifBlank { "আপনার অনুরোধ অনুযায়ী প্রম্পটটি আপডেট করা হয়েছে।" },
                            updatedPrompt = codeContent
                        )
                    )
                } else {
                    Result.success(
                        AgentRefineResponse(
                            explanation = rawResponse.take(300),
                            updatedPrompt = rawResponse
                        )
                    )
                }
            } else {
                Result.success(
                    AgentRefineResponse(
                        explanation = "আপনার নির্দেশনা অনুযায়ী প্রম্পট আপডেট করা হয়েছে:",
                        updatedPrompt = rawResponse
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
