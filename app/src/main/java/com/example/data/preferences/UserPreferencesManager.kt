package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("promptflow_prefs", Context.MODE_PRIVATE)

    companion object {
        const val DEFAULT_INBUILT_API_KEY = "sk-F1Rka67ovnISbLz3oTYSnVDmaU5gOZGcWRi9dNN4dnvPxt2p"
        const val DEFAULT_API_URL = "https://flushapi.fun/v1/chat/completions"
        const val MODEL_CLAUDE_OPUS = "claude-opus-4-7"
        const val MODEL_GPT_LUNA = "gpt-5.6-luna"
        const val DEFAULT_MODEL = "hybrid-auto"

        private const val KEY_CUSTOM_API_KEY = "custom_api_key"
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

    fun getApiKey(): String {
        val customKey = prefs.getString(KEY_CUSTOM_API_KEY, "")?.trim() ?: ""
        // Purge any stale legacy XKIRO key that might be cached
        if (customKey.startsWith("sk-xt-") || customKey.endsWith("b7b4")) {
            prefs.edit().remove(KEY_CUSTOM_API_KEY).apply()
        } else if (customKey.isNotEmpty()) {
            return customKey
        }
        // Always default to the active inbuilt FlushAPI key
        return DEFAULT_INBUILT_API_KEY
    }

    fun hasApiKey(): Boolean {
        return getApiKey().isNotEmpty()
    }

    fun getMaskedApiKey(): String {
        val key = getApiKey()
        if (key.isEmpty()) return "Not configured"
        if (key.length <= 8) return "••••••••"
        return "••••••••" + key.takeLast(4)
    }

    fun setCustomApiKey(key: String) {
        prefs.edit().putString(KEY_CUSTOM_API_KEY, key.trim()).apply()
    }

    fun clearCustomApiKey() {
        prefs.edit().remove(KEY_CUSTOM_API_KEY).apply()
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
