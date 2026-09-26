package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.PromptHistoryEntity
import com.example.data.preferences.UserPreferencesManager
import com.example.data.remote.PromptGeneratorService
import com.example.data.repository.PromptHistoryRepository
import com.example.model.PromptConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PromptUiState(
    val isGenerating: Boolean = false,
    val generatedPrompt: String? = null,
    val isEditMode: Boolean = false,
    val editedPromptText: String = "",
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val isTestingApi: Boolean = false,
    val apiTestResult: String? = null,
    val lastGeneratedConfig: PromptConfig? = null
)

class PromptFlowViewModel(application: Application) : AndroidViewModel(application) {

    val preferencesManager = UserPreferencesManager(application)
    private val database = AppDatabase.getDatabase(application)
    private val historyRepository = PromptHistoryRepository(database.promptHistoryDao())
    private val promptService = PromptGeneratorService()

    private val _config = MutableStateFlow(
        PromptConfig(
            aspectRatio = preferencesManager.getDefaultAspectRatio(),
            duration = preferencesManager.getDefaultDuration(),
            videoStyle = preferencesManager.getDefaultStyle()
        )
    )
    val config: StateFlow<PromptConfig> = _config.asStateFlow()

    private val _uiState = MutableStateFlow(PromptUiState())
    val uiState: StateFlow<PromptUiState> = _uiState.asStateFlow()

    val historyList: StateFlow<List<PromptHistoryEntity>> = historyRepository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val currentTheme: StateFlow<String> = preferencesManager.themeFlow

    fun updateScript(script: String) {
        _config.value = _config.value.copy(script = script)
    }

    fun updateAspectRatio(ratio: String) {
        _config.value = _config.value.copy(
            aspectRatio = ratio,
            resolution = when (ratio) {
                "16:9" -> "1920x1080"
                "1:1" -> "1080x1080"
                else -> "1080x1920"
            }
        )
    }

    fun updateResolution(resolution: String) {
        _config.value = _config.value.copy(resolution = resolution)
    }

    fun updateDuration(duration: String) {
        _config.value = _config.value.copy(duration = duration)
    }

    fun updateVideoStyle(style: String) {
        _config.value = _config.value.copy(videoStyle = style)
    }

    fun updateLocation(location: String) {
        _config.value = _config.value.copy(location = location)
    }

    fun updatePresenter(presenter: String) {
        _config.value = _config.value.copy(presenter = presenter)
    }

    fun updateCamera(camera: String) {
        _config.value = _config.value.copy(camera = camera)
    }

    fun updateBRoll(bRoll: String) {
        _config.value = _config.value.copy(bRoll = bRoll)
    }

    fun updateOnScreenText(text: String) {
        _config.value = _config.value.copy(onScreenText = text)
    }

    fun updateCustomText(text: String) {
        _config.value = _config.value.copy(customText = text)
    }

    fun updateCustomInstructions(instructions: String) {
        _config.value = _config.value.copy(customInstructions = instructions)
    }

    fun updateCustomLocation(loc: String) {
        _config.value = _config.value.copy(customLocation = loc)
    }

    fun updateCustomCamera(cam: String) {
        _config.value = _config.value.copy(customCamera = cam)
    }

    fun updateCustomStyle(style: String) {
        _config.value = _config.value.copy(customStyle = style)
    }

    fun updateCustomDuration(dur: String) {
        _config.value = _config.value.copy(customDuration = dur)
    }

    fun setReferenceImageUri(uriString: String?) {
        _config.value = _config.value.copy(
            referenceImageUri = uriString,
            presenter = if (uriString != null) "Same as reference image" else _config.value.presenter
        )
    }

    fun setReferenceImageDescription(desc: String) {
        _config.value = _config.value.copy(referenceImageDescription = desc)
    }

    fun toggleOption(optionName: String, enabled: Boolean) {
        _config.value = when (optionName) {
            "preserveExactDialogue" -> _config.value.copy(preserveExactDialogue = enabled)
            "maintainCharacterConsistency" -> _config.value.copy(maintainCharacterConsistency = enabled)
            "synchronizeBroll" -> _config.value.copy(synchronizeBroll = enabled)
            "naturalLipSync" -> _config.value.copy(naturalLipSync = enabled)
            "oneContinuousVoiceTake" -> _config.value.copy(oneContinuousVoiceTake = enabled)
            "noDialogueRepetition" -> _config.value.copy(noDialogueRepetition = enabled)
            "professionalCameraDirection" -> _config.value.copy(professionalCameraDirection = enabled)
            "cinematicLighting" -> _config.value.copy(cinematicLighting = enabled)
            "negativePrompt" -> _config.value.copy(negativePrompt = enabled)
            else -> _config.value
        }
    }

    fun loadSampleBengaliScript() {
        val sample = "আমাদের ডিজিটাল বাংলাদেশ এখন নতুন উচ্চতায় পৌঁছে গেছে। স্মার্ট ভবিষ্যতের দিকে এগিয়ে যাচ্ছে পুরো দেশ। তরুণ প্রজন্ম আনছে বৈপ্লবিক পরিবর্তন।"
        _config.value = _config.value.copy(script = sample)
    }

    fun loadSampleEnglishScript() {
        val sample = "Artificial intelligence isn't just transforming how we work; it is redefining how human imagination becomes reality. Welcome to the next creative frontier."
        _config.value = _config.value.copy(script = sample)
    }

    fun generateMasterPrompt() {
        val current = _config.value
        if (current.script.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please enter your dialogue script first."
            )
            return
        }

        if (_uiState.value.isGenerating) return

        _uiState.value = _uiState.value.copy(
            isGenerating = true,
            errorMessage = null,
            infoMessage = null
        )

        viewModelScope.launch {
            val apiKey = preferencesManager.getApiKey()
            val result = promptService.generateMasterPrompt(current, apiKey)

            result.onSuccess { prompt ->
                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    generatedPrompt = prompt,
                    editedPromptText = prompt,
                    isEditMode = false,
                    lastGeneratedConfig = current,
                    infoMessage = if (apiKey.isNotBlank()) "Master prompt generated via DeepSeek V4.1 AI!" else "Master prompt synthesized with strict 22-point engine."
                )

                // Save to history
                saveToHistory(current, prompt)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    errorMessage = error.localizedMessage ?: "Failed to generate prompt. Please check your network and API key."
                )
            }
        }
    }

    fun regeneratePrompt() {
        generateMasterPrompt()
    }

    fun startEditMode() {
        _uiState.value = _uiState.value.copy(
            isEditMode = true,
            editedPromptText = _uiState.value.generatedPrompt ?: ""
        )
    }

    fun updateEditedPromptText(text: String) {
        _uiState.value = _uiState.value.copy(editedPromptText = text)
    }

    fun saveEditedPrompt() {
        val edited = _uiState.value.editedPromptText
        _uiState.value = _uiState.value.copy(
            isEditMode = false,
            generatedPrompt = edited,
            infoMessage = "Changes saved to Master Prompt."
        )
    }

    fun cancelEditMode() {
        _uiState.value = _uiState.value.copy(
            isEditMode = false,
            editedPromptText = _uiState.value.generatedPrompt ?: ""
        )
    }

    fun clearPrompt() {
        _uiState.value = _uiState.value.copy(
            generatedPrompt = null,
            isEditMode = false,
            editedPromptText = "",
            errorMessage = null,
            infoMessage = null
        )
    }

    private suspend fun saveToHistory(config: PromptConfig, prompt: String) {
        val title = config.script.trim().take(40).let { if (config.script.length > 40) "$it..." else it }
        val excerpt = config.script.trim().lines().firstOrNull()?.take(60) ?: title

        val entity = PromptHistoryEntity(
            title = title,
            scriptExcerpt = excerpt,
            fullScript = config.script,
            generatedPrompt = prompt,
            aspectRatio = config.aspectRatio,
            resolution = config.resolution,
            duration = config.duration,
            videoStyle = config.videoStyle,
            location = config.location,
            presenter = config.presenter,
            camera = config.camera,
            hasReferenceImage = config.referenceImageUri != null
        )
        historyRepository.insert(entity)
    }

    fun loadFromHistory(history: PromptHistoryEntity) {
        _config.value = _config.value.copy(
            script = history.fullScript,
            aspectRatio = history.aspectRatio,
            resolution = history.resolution,
            duration = history.duration,
            videoStyle = history.videoStyle,
            location = history.location,
            presenter = history.presenter,
            camera = history.camera
        )
        _uiState.value = _uiState.value.copy(
            generatedPrompt = history.generatedPrompt,
            editedPromptText = history.generatedPrompt,
            isEditMode = false,
            infoMessage = "Loaded prompt from history (${history.title})"
        )
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            historyRepository.deleteById(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            historyRepository.clearAll()
        }
    }

    fun testApiConnection(key: String) {
        _uiState.value = _uiState.value.copy(isTestingApi = true, apiTestResult = null)
        viewModelScope.launch {
            val result = promptService.testConnection(key.ifBlank { preferencesManager.getApiKey() })
            result.onSuccess { msg ->
                _uiState.value = _uiState.value.copy(
                    isTestingApi = false,
                    apiTestResult = msg
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isTestingApi = false,
                    apiTestResult = "Test failed: ${err.localizedMessage}"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, infoMessage = null)
    }
}
