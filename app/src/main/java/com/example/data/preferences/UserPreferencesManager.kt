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
        private const val KEY_CUSTOM_API_KEY = "custom_api_key"
        private const val KEY_THEME = "app_theme"
        private const val KEY_DEFAULT_ASPECT_RATIO = "default_aspect_ratio"
        private const val KEY_DEFAULT_DURATION = "default_duration"
        private const val KEY_DEFAULT_STYLE = "default_style"
    }

    private val _themeFlow = MutableStateFlow(getTheme())
    val themeFlow: StateFlow<String> = _themeFlow.asStateFlow()

    fun getApiKey(): String {
        val customKey = prefs.getString(KEY_CUSTOM_API_KEY, "")?.trim() ?: ""
        if (customKey.isNotEmpty()) {
            return customKey
        }
        val buildConfigKey = try {
            BuildConfig.XKIRO_API_KEY
        } catch (e: Exception) {
            ""
        }
        if (buildConfigKey.isNotEmpty() && !buildConfigKey.contains("your_xkiro_api_key")) {
            return buildConfigKey
        }
        return ""
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
