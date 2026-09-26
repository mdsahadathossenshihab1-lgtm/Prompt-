package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.FirebaseManager
import com.example.data.firebase.FirestorePromptModel
import com.example.data.local.AiModelEntity
import com.example.data.local.AppDatabase
import com.example.data.local.PromptHistoryEntity
import com.example.data.preferences.UserPreferencesManager
import com.example.data.remote.AiModelGeneratorService
import com.example.data.remote.GeneratedModelResult
import com.example.data.remote.PromptGeneratorService
import com.example.data.repository.AiModelRepository
import com.example.data.repository.FirestorePromptRepository
import com.example.data.repository.FirestorePromptRepositoryImpl
import com.example.data.repository.PromptHistoryRepository
import com.example.model.AiModelConfig
import com.example.model.ModelPreset
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
    val isSyncingCloud: Boolean = false,
    val cloudMessage: String? = null,
    val lastGeneratedConfig: PromptConfig? = null
)

data class AiModelUiState(
    val isGenerating: Boolean = false,
    val generatedResult: GeneratedModelResult? = null,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val isModelLocked: Boolean = false,
    val lockedModelSeed: Long? = null,
    val lockedModelName: String? = null,
    val isSavedToLibrary: Boolean = false
)

class PromptFlowViewModel(application: Application) : AndroidViewModel(application) {

    val preferencesManager = UserPreferencesManager(application)
    private val database = AppDatabase.getDatabase(application)
    private val historyRepository = PromptHistoryRepository(database.promptHistoryDao())
    val aiModelRepository = AiModelRepository(database.aiModelDao())
    private val promptService = PromptGeneratorService()
    val aiModelGeneratorService = AiModelGeneratorService(application)
    val firebaseManager = FirebaseManager(application)
    val firestoreRepository: FirestorePromptRepository = FirestorePromptRepositoryImpl(firebaseManager)

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

    // AI Model Creator State
    private val _modelConfig = MutableStateFlow(AiModelConfig())
    val modelConfig: StateFlow<AiModelConfig> = _modelConfig.asStateFlow()

    private val _modelUiState = MutableStateFlow(AiModelUiState())
    val modelUiState: StateFlow<AiModelUiState> = _modelUiState.asStateFlow()

    val savedModels: StateFlow<List<AiModelEntity>> = aiModelRepository.allModels
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val favoriteModels: StateFlow<List<AiModelEntity>> = aiModelRepository.favoriteModels
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val historyList: StateFlow<List<PromptHistoryEntity>> = historyRepository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val currentTheme: StateFlow<String> = preferencesManager.themeFlow

    fun isFirebaseAvailable(): Boolean = firebaseManager.isFirebaseInitialized()

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
                    infoMessage = "Master prompt generated successfully!"
                )

                // Save to local history + Firebase cloud sync
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
        val insertedId = historyRepository.insert(entity)

        // Try syncing to Firebase Firestore if initialized
        if (firebaseManager.isFirebaseInitialized()) {
            val firestoreModel = FirestorePromptModel(
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
            val syncResult = firestoreRepository.createPrompt(firestoreModel)
            syncResult.onSuccess { docId ->
                historyRepository.updateCloudSyncStatus(insertedId, true, docId)
            }
        }
    }

    fun syncPromptToCloud(item: PromptHistoryEntity) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncingCloud = true, cloudMessage = null)
            val model = FirestorePromptModel(
                id = item.cloudDocumentId ?: "",
                title = item.title,
                scriptExcerpt = item.scriptExcerpt,
                fullScript = item.fullScript,
                generatedPrompt = item.generatedPrompt,
                aspectRatio = item.aspectRatio,
                resolution = item.resolution,
                duration = item.duration,
                videoStyle = item.videoStyle,
                location = item.location,
                presenter = item.presenter,
                camera = item.camera,
                hasReferenceImage = item.hasReferenceImage,
                timestamp = item.timestamp
            )

            val result = firestoreRepository.createPrompt(model)
            result.onSuccess { docId ->
                historyRepository.updateCloudSyncStatus(item.id, true, docId)
                _uiState.value = _uiState.value.copy(
                    isSyncingCloud = false,
                    cloudMessage = "Prompt synced to Firebase Cloud!"
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isSyncingCloud = false,
                    cloudMessage = "Firebase sync: ${error.localizedMessage}"
                )
            }
        }
    }

    fun syncAllHistoryToCloud() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncingCloud = true, cloudMessage = null)
            val items = historyList.value
            if (items.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    isSyncingCloud = false,
                    cloudMessage = "No prompts to sync."
                )
                return@launch
            }

            var successCount = 0
            for (item in items) {
                val model = FirestorePromptModel(
                    id = item.cloudDocumentId ?: "",
                    title = item.title,
                    scriptExcerpt = item.scriptExcerpt,
                    fullScript = item.fullScript,
                    generatedPrompt = item.generatedPrompt,
                    aspectRatio = item.aspectRatio,
                    resolution = item.resolution,
                    duration = item.duration,
                    videoStyle = item.videoStyle,
                    location = item.location,
                    presenter = item.presenter,
                    camera = item.camera,
                    hasReferenceImage = item.hasReferenceImage,
                    timestamp = item.timestamp
                )
                val result = firestoreRepository.createPrompt(model)
                if (result.isSuccess) {
                    val docId = result.getOrNull()
                    historyRepository.updateCloudSyncStatus(item.id, true, docId)
                    successCount++
                }
            }

            _uiState.value = _uiState.value.copy(
                isSyncingCloud = false,
                cloudMessage = if (successCount > 0) "Synced $successCount prompts to Firebase Database!" else "Sync completed."
            )
        }
    }

    fun testFirebaseDatabase() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncingCloud = true, cloudMessage = null)
            val result = firebaseManager.testConnection()
            result.onSuccess { msg ->
                _uiState.value = _uiState.value.copy(isSyncingCloud = false, cloudMessage = msg)
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isSyncingCloud = false,
                    cloudMessage = "Firebase status: ${err.localizedMessage}"
                )
            }
        }
    }

    fun saveFirebaseConfig(projectId: String, apiKey: String, appId: String) {
        preferencesManager.setFirebaseProjectId(projectId)
        preferencesManager.setFirebaseApiKey(apiKey)
        preferencesManager.setFirebaseAppId(appId)
        testFirebaseDatabase()
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
            val item = historyRepository.getById(id)
            if (item?.cloudDocumentId != null) {
                firestoreRepository.deletePrompt(item.cloudDocumentId)
            }
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
        _uiState.value = _uiState.value.copy(errorMessage = null, infoMessage = null, cloudMessage = null)
    }

    // ==========================================
    // MODULE 2: AI MODEL CREATOR OPERATIONS
    // ==========================================

    fun updateModelConfig(transform: (AiModelConfig) -> AiModelConfig) {
        _modelConfig.value = transform(_modelConfig.value)
    }

    fun applyModelPreset(preset: ModelPreset) {
        _modelConfig.value = preset.config
        _modelUiState.value = _modelUiState.value.copy(
            infoMessage = "Applied '${preset.title}' preset settings."
        )
    }

    fun generateAiModel(isSimilar: Boolean = false) {
        val currentCfg = _modelConfig.value
        _modelUiState.value = _modelUiState.value.copy(
            isGenerating = true,
            errorMessage = null,
            infoMessage = if (isSimilar) "Generating variation with locked identity..." else "Generating video-ready human model...",
            isSavedToLibrary = false
        )

        viewModelScope.launch {
            val result = aiModelGeneratorService.generateModelImage(currentCfg, isSimilar = isSimilar)
            result.onSuccess { modelResult ->
                _modelUiState.value = _modelUiState.value.copy(
                    isGenerating = false,
                    generatedResult = modelResult,
                    lockedModelSeed = if (_modelUiState.value.isModelLocked) _modelUiState.value.lockedModelSeed ?: modelResult.seed else modelResult.seed,
                    lockedModelName = modelResult.suggestedName,
                    infoMessage = "Model generated successfully!"
                )
            }.onFailure { error ->
                _modelUiState.value = _modelUiState.value.copy(
                    isGenerating = false,
                    errorMessage = "Failed to generate model: ${error.localizedMessage}"
                )
            }
        }
    }

    fun toggleLockModel(lock: Boolean) {
        val currentResult = _modelUiState.value.generatedResult
        val seed = currentResult?.seed
        _modelUiState.value = _modelUiState.value.copy(
            isModelLocked = lock,
            lockedModelSeed = if (lock) seed else null,
            infoMessage = if (lock) "Model locked! Subsequent generations will preserve this character identity." else "Model lock disabled."
        )
        _modelConfig.value = _modelConfig.value.copy(
            isModelLocked = lock,
            lockedSeed = if (lock) seed else null
        )
    }

    fun saveCurrentModelToLibrary(customName: String? = null) {
        val result = _modelUiState.value.generatedResult ?: return
        val cfg = _modelConfig.value
        val modelName = customName?.takeIf { it.isNotBlank() } ?: result.suggestedName

        viewModelScope.launch {
            val entity = AiModelEntity(
                name = modelName,
                imageUrl = result.imageUrl,
                localFilePath = result.localFilePath,
                promptText = result.detailedPrompt,
                gender = cfg.gender,
                modelType = cfg.modelType,
                style = cfg.style,
                environment = cfg.location,
                cameraFraming = cfg.cameraFraming,
                aspectRatio = cfg.aspectRatio,
                seed = result.seed,
                isFavorite = false,
                isLocked = _modelUiState.value.isModelLocked
            )
            aiModelRepository.insertModel(entity)
            _modelUiState.value = _modelUiState.value.copy(
                isSavedToLibrary = true,
                infoMessage = "Model saved to library as '$modelName'!"
            )
        }
    }

    /**
     * Section 23: INTEGRATION WITH EXISTING PROMPT GENERATOR
     * Attaches model reference to the Video Prompt Generator, activates MODEL LOCKED badge,
     * and prepares Video Prompt Generator to preserve character identity across scenes.
     */
    fun useThisModelInPromptGenerator(
        imageUrl: String? = null,
        modelName: String? = null,
        promptDesc: String? = null
    ) {
        val activeImageUrl = imageUrl ?: _modelUiState.value.generatedResult?.imageUrl
        val activeName = modelName ?: _modelUiState.value.generatedResult?.suggestedName ?: "AI Model Presenter"
        val activeDesc = promptDesc ?: _modelUiState.value.generatedResult?.detailedPrompt ?: _modelConfig.value.description

        if (activeImageUrl != null) {
            _config.value = _config.value.copy(
                referenceImageUri = activeImageUrl,
                referenceImageDescription = "Locked AI Model: $activeName. Features: ${_modelConfig.value.appearance}",
                presenter = if (_modelConfig.value.gender == "Female") "Female" else "Male",
                isModelLocked = true,
                lockedModelName = activeName
            )
            _modelUiState.value = _modelUiState.value.copy(
                infoMessage = "Model '$activeName' attached to Video Prompt Generator! [MODEL LOCKED]"
            )
        }
    }

    fun clearLockedModel() {
        _config.value = _config.value.copy(
            isModelLocked = false,
            lockedModelName = null,
            referenceImageUri = null,
            referenceImageDescription = ""
        )
    }

    fun deleteSavedModel(model: AiModelEntity) {
        viewModelScope.launch {
            aiModelRepository.deleteModel(model)
            _modelUiState.value = _modelUiState.value.copy(
                infoMessage = "Removed '${model.name}' from library."
            )
        }
    }

    fun toggleFavoriteModel(id: Long, isFav: Boolean) {
        viewModelScope.launch {
            aiModelRepository.toggleFavorite(id, isFav)
        }
    }

    fun clearModelMessages() {
        _modelUiState.value = _modelUiState.value.copy(
            errorMessage = null,
            infoMessage = null
        )
    }
}
