package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("promptflow_prefs", Context.MODE_PRIVATE)

    companion object {
        // Active verified XKIRO API credentials provided by the user
        const val DEFAULT_INBUILT_API_KEY = "sk-xt-0b6892647ce8d495ae8bc2d9a5e212f4f07f805eeceae961"
        const val DEFAULT_API_URL = "https://api.xkiro.com/v1/chat/completions"
        const val DEFAULT_MODELS_URL = "https://api.xkiro.com/v1/models"
        const val DEFAULT_MODEL = "qwen/qwen3.8-omni-flash:free"
        const val MODEL_DEEPSEEK = "deepseek/deepseek-v3.2"
        const val MODEL_MINIMAX = "minimax/minimax-m3:free"
        const val MODEL_QWEN_MAX = "qwen/qwen3.7-max:free"

        private const val KEY_CUSTOM_API_KEY = "custom_api_key"
        private const val KEY_SELECTED_MODEL = "selected_model"
        private const val KEY_THEME = "app_theme"
        private const val KEY_DEFAULT_ASPECT_RATIO = "default_aspect_ratio"
        private const val KEY_DEFAULT_DURATION = "default_duration"
        private const val KEY_DEFAULT_STYLE = "default_style"

        private const val KEY_FIREBASE_PROJECT_ID = "firebase_project_id"
        private const val KEY_FIREBASE_API_KEY = "firebase_api_key"
        private const val KEY_FIREBASE_APP_ID = "firebase_app_id"
    }

    private val _themeFlow = MutableStateFlow(getTheme())
    val themeFlow: StateFlow<String> = _themeFlow.asStateFlow()

    init {
        // Reset any outdated or invalid cached keys to ensure the verified active key is used
        val storedKey = prefs.getString(KEY_CUSTOM_API_KEY, "")?.trim() ?: ""
        if (storedKey.contains("Flush") || storedKey.contains("F1Rka67") || storedKey.startsWith("sk-XgS") || storedKey.endsWith("b7b4")) {
            prefs.edit().remove(KEY_CUSTOM_API_KEY).apply()
        }
    }

    fun getApiKey(): String {
        val customKey = prefs.getString(KEY_CUSTOM_API_KEY, "")?.trim() ?: ""
        if (customKey.isNotEmpty()) {
            return customKey
        }
        return DEFAULT_INBUILT_API_KEY
    }

    fun hasApiKey(): Boolean {
        return getApiKey().isNotEmpty()
    }

    fun getMaskedApiKey(): String {
        val key = getApiKey()
        if (key.isEmpty()) return "Not configured"
        if (key.length <= 8) return "••••••••"
        return "••••••••" + key.takeLast(6)
    }

    fun setCustomApiKey(key: String) {
        prefs.edit().putString(KEY_CUSTOM_API_KEY, key.trim()).apply()
    }

    fun clearCustomApiKey() {
        prefs.edit().remove(KEY_CUSTOM_API_KEY).apply()
    }

    fun getSelectedModel(): String {
        return prefs.getString(KEY_SELECTED_MODEL, DEFAULT_MODEL) ?: DEFAULT_MODEL
    }

    fun setSelectedModel(model: String) {
        prefs.edit().putString(KEY_SELECTED_MODEL, model).apply()
    }

    // Firebase preferences
    fun getFirebaseProjectId(): String {
        return prefs.getString(KEY_FIREBASE_PROJECT_ID, "")?.trim() ?: ""
    }

    fun setFirebaseProjectId(id: String) {
        prefs.edit().putString(KEY_FIREBASE_PROJECT_ID, id.trim()).apply()
    }

    fun getFirebaseApiKey(): String {
        return prefs.getString(KEY_FIREBASE_API_KEY, "")?.trim() ?: ""
    }

    fun setFirebaseApiKey(key: String) {
        prefs.edit().putString(KEY_FIREBASE_API_KEY, key.trim()).apply()
    }

    fun getFirebaseAppId(): String {
        return prefs.getString(KEY_FIREBASE_APP_ID, "")?.trim() ?: ""
    }

    fun setFirebaseAppId(appId: String) {
        prefs.edit().putString(KEY_FIREBASE_APP_ID, appId.trim()).apply()
    }

    fun getTheme(): String {
        return prefs.getString(KEY_THEME, "DARK") ?: "DARK"
    }

    fun setTheme(theme: String) {
        prefs.edit().putString(KEY_THEME, theme).apply()
        _themeFlow.value = theme
    }

    fun getDefaultAspectRatio(): String {
        return prefs.getString(KEY_DEFAULT_ASPECT_RATIO, "9:16") ?: "9:16"
    }

    fun setDefaultAspectRatio(ratio: String) {
        prefs.edit().putString(KEY_DEFAULT_ASPECT_RATIO, ratio).apply()
    }

    fun getDefaultDuration(): String {
        return prefs.getString(KEY_DEFAULT_DURATION, "8 seconds") ?: "8 seconds"
    }

    fun setDefaultDuration(duration: String) {
        prefs.edit().putString(KEY_DEFAULT_DURATION, duration).apply()
    }

    fun getDefaultStyle(): String {
        return prefs.getString(KEY_DEFAULT_STYLE, "Cinematic") ?: "Cinematic"
    }

    fun setDefaultStyle(style: String) {
        prefs.edit().putString(KEY_DEFAULT_STYLE, style).apply()
    }
}
