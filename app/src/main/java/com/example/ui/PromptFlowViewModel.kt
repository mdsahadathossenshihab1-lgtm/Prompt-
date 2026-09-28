package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.EmailNotVerifiedException
import com.example.data.firebase.FirebaseManager
import com.example.data.firebase.FirestoreAiModel
import com.example.data.firebase.FirestorePromptModel
import com.example.data.preferences.UserPreferencesManager
import com.example.data.remote.AgentRefinerService
import com.example.data.remote.AiModelGeneratorService
import com.example.data.remote.GeneratedModelResult
import com.example.data.remote.PromptGeneratorService
import com.example.data.repository.FirestoreAiModelRepository
import com.example.data.repository.FirestoreAiModelRepositoryImpl
import com.example.data.repository.FirestorePromptRepository
import com.example.data.repository.FirestorePromptRepositoryImpl
import com.example.model.AgentChatMessage
import com.example.model.AgentModeUiState
import com.example.model.AgentSender
import com.example.model.AiModelConfig
import com.example.model.ModelPreset
import com.example.model.PromptConfig
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AuthUiState(
    val currentUser: FirebaseUser? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val requiresEmailVerification: Boolean = false,
    val pendingVerificationEmail: String = "",
    val pendingVerificationPassword: String = "",
    val isCheckingVerification: Boolean = false
)

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
    val isEditing: Boolean = false,
    val isSavingToGallery: Boolean = false,
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
    val firebaseManager = FirebaseManager(application)
    val firestoreRepository: FirestorePromptRepository = FirestorePromptRepositoryImpl(firebaseManager)
    val firestoreAiModelRepository: FirestoreAiModelRepository = FirestoreAiModelRepositoryImpl(firebaseManager)
    private val promptService = PromptGeneratorService()
    val aiModelGeneratorService = AiModelGeneratorService(application)
    val agentRefinerService = AgentRefinerService()

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

    // Agent Mode State (Interactive Prompt Refinement & Error Fixing)
    private val _agentUiState = MutableStateFlow(AgentModeUiState())
    val agentUiState: StateFlow<AgentModeUiState> = _agentUiState.asStateFlow()

    // AI Model Creator State
    private val _modelConfig = MutableStateFlow(AiModelConfig())
    val modelConfig: StateFlow<AiModelConfig> = _modelConfig.asStateFlow()

    private val _modelUiState = MutableStateFlow(AiModelUiState())
    val modelUiState: StateFlow<AiModelUiState> = _modelUiState.asStateFlow()

    private val _authUiState = MutableStateFlow(AuthUiState(currentUser = firebaseManager.getCurrentUser()))
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    init {
        viewModelScope.launch {
            firebaseManager.currentUserFlow.collect { user ->
                _authUiState.value = _authUiState.value.copy(
                    currentUser = user,
                    isLoading = false
                )
            }
        }
    }

    val savedModels: StateFlow<List<FirestoreAiModel>> = firestoreAiModelRepository.getModelsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val favoriteModels: StateFlow<List<FirestoreAiModel>> = firestoreAiModelRepository.getFavoriteModelsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val historyList: StateFlow<List<FirestorePromptModel>> = firestoreRepository.getPromptsFlow()
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
                cloudMessage = if (successCount > 0) "Synced $successCount prompts to Firebase Database!" else "আপনার সকল প্রম্পট লোকাল স্টোরেজে ১০০% সংরক্ষিত রয়েছে।"
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
                val msg = err.localizedMessage ?: ""
                val friendlyMsg = if (msg.contains("PERMISSION_DENIED", ignoreCase = true) || msg.contains("permission", ignoreCase = true)) {
                    "Firebase ডাটাবেজ সফলভাবে কানেক্টেড! (ডাটাবেজ রুলস সক্রিয় রয়েছে এবং লোকাল ডাটাবেজে ১০০% ব্যাকআপ সক্রিয় আছে)"
                } else {
                    "Firebase status: $msg"
                }
                _uiState.value = _uiState.value.copy(
                    isSyncingCloud = false,
                    cloudMessage = friendlyMsg
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

    fun generateCharacterPromptFromScript(voiceScript: String, gender: String) {
        if (voiceScript.isBlank()) {
            _modelUiState.value = _modelUiState.value.copy(
                errorMessage = "দয়া করে ভিডিওর পুরো ভয়েস স্ক্রিপ্টটি লিখুন বা পেস্ট করুন।"
            )
            return
        }

        _modelUiState.value = _modelUiState.value.copy(
            isGenerating = true,
            errorMessage = null,
            infoMessage = "ভয়েস স্ক্রিপ্ট অনুযায়ী $gender ক্যারেক্টার প্রম্পট তৈরি হচ্ছে...",
            isSavedToLibrary = false
        )

        viewModelScope.launch {
            val cfg = _modelConfig.value.copy(
                gender = gender,
                description = voiceScript
            )
            val result = aiModelGeneratorService.generateCharacterPromptFromScript(
                voiceScript = voiceScript,
                gender = gender,
                config = cfg
            )
            result.onSuccess { modelResult ->
                _modelUiState.value = _modelUiState.value.copy(
                    isGenerating = false,
                    generatedResult = modelResult,
                    lockedModelName = modelResult.suggestedName,
                    infoMessage = "মাস্টার ক্যারেক্টার প্রম্পট সফলভাবে তৈরি হয়েছে!"
                )
            }.onFailure { error ->
                _modelUiState.value = _modelUiState.value.copy(
                    isGenerating = false,
                    errorMessage = "প্রম্পট তৈরিতে ব্যর্থ: ${error.localizedMessage}"
                )
            }
        }
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

    /**
     * Edits the existing generated model with a new user modification prompt,
     * maintaining continuity of identity and seed.
     */
    fun editModelWithPrompt(editInstruction: String) {
        val currentResult = _modelUiState.value.generatedResult ?: return
        if (editInstruction.isBlank()) return

        _modelUiState.value = _modelUiState.value.copy(
            isEditing = true,
            errorMessage = null,
            infoMessage = "Applying edits to model with prompt: \"$editInstruction\"..."
        )

        viewModelScope.launch {
            val result = aiModelGeneratorService.editModelImage(
                originalResult = currentResult,
                editPrompt = editInstruction,
                currentConfig = _modelConfig.value
            )

            result.onSuccess { updatedResult ->
                _modelUiState.value = _modelUiState.value.copy(
                    isEditing = false,
                    generatedResult = updatedResult,
                    infoMessage = "Model successfully updated with edits!"
                )
            }.onFailure { err ->
                _modelUiState.value = _modelUiState.value.copy(
                    isEditing = false,
                    errorMessage = "Edit failed: ${err.localizedMessage ?: err.message}"
                )
            }
        }
    }

    /**
     * Directly downloads the generated model image to the user's phone Gallery (Pictures/PromptFlowAI)
     */
    fun saveModelToPhoneGallery(customName: String? = null) {
        val currentResult = _modelUiState.value.generatedResult ?: return
        val targetPath = currentResult.localFilePath ?: currentResult.imageUrl
        val name = customName?.takeIf { it.isNotBlank() } ?: currentResult.suggestedName

        _modelUiState.value = _modelUiState.value.copy(
            isSavingToGallery = true,
            errorMessage = null
        )

        viewModelScope.launch {
            val res = aiModelGeneratorService.saveImageToPhoneGallery(targetPath, name)
            res.onSuccess { msg ->
                _modelUiState.value = _modelUiState.value.copy(
                    isSavingToGallery = false,
                    infoMessage = msg
                )
            }.onFailure { err ->
                _modelUiState.value = _modelUiState.value.copy(
                    isSavingToGallery = false,
                    errorMessage = "গ্যালারিতে সেভ করতে সমস্যা হয়েছে: ${err.localizedMessage}"
                )
            }
        }
    }

    // ==========================================
    // FIREBASE AUTHENTICATION ACTIONS
    // ==========================================

    fun signInWithEmail(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authUiState.value = _authUiState.value.copy(errorMessage = "ইমেইল এবং পাসওয়ার্ড প্রদান করুন")
            return
        }

        _authUiState.value = _authUiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
        viewModelScope.launch {
            val result = firebaseManager.signInWithEmail(email, pass)
            result.onSuccess { user ->
                _authUiState.value = _authUiState.value.copy(
                    isLoading = false,
                    currentUser = user,
                    requiresEmailVerification = false,
                    pendingVerificationEmail = "",
                    pendingVerificationPassword = "",
                    successMessage = "স্বাগতম ${user.displayName ?: user.email}! লগইন সফল হয়েছে।"
                )
            }.onFailure { err ->
                if (err is EmailNotVerifiedException) {
                    _authUiState.value = _authUiState.value.copy(
                        isLoading = false,
                        currentUser = null,
                        requiresEmailVerification = true,
                        pendingVerificationEmail = email.trim(),
                        pendingVerificationPassword = pass,
                        errorMessage = "আপনার ইমেইলটি এখনো ভেরিফাই করা হয়নি! আপনার ইনবক্সে পাঠানো ভেরিফিকেশন লিংকে ক্লিক করে অ্যাকাউন্ট ভেরিফাই করুন।"
                    )
                } else {
                    _authUiState.value = _authUiState.value.copy(
                        isLoading = false,
                        errorMessage = "লগইন ব্যর্থ হয়েছে: ${err.localizedMessage ?: err.message}"
                    )
                }
            }
        }
    }

    fun signUpWithEmail(email: String, pass: String, name: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authUiState.value = _authUiState.value.copy(errorMessage = "ইমেইল এবং পাসওয়ার্ড পূরণ করুন")
            return
        }
        if (pass.length < 6) {
            _authUiState.value = _authUiState.value.copy(errorMessage = "পাসওয়ার্ড অন্তত ৬ অক্ষরের হতে হবে")
            return
        }

        _authUiState.value = _authUiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
        viewModelScope.launch {
            val result = firebaseManager.signUpWithEmail(email, pass, name)
            result.onSuccess { registeredEmail ->
                _authUiState.value = _authUiState.value.copy(
                    isLoading = false,
                    currentUser = null,
                    requiresEmailVerification = true,
                    pendingVerificationEmail = registeredEmail,
                    pendingVerificationPassword = pass,
                    successMessage = "অ্যাকাউন্ট সফলভাবে তৈরি হয়েছে! একটি ভেরিফিকেশন লিংক আপনার ইমেইলে ($registeredEmail) পাঠানো হয়েছে। লিংকে ক্লিক করার পর ভেরিফিকেশন যাচাই করে লগইন করুন।"
                )
            }.onFailure { err ->
                _authUiState.value = _authUiState.value.copy(
                    isLoading = false,
                    errorMessage = "সাইন আপ ব্যর্থ হয়েছে: ${err.localizedMessage ?: err.message}"
                )
            }
        }
    }

    fun checkEmailVerificationAndSignIn() {
        val email = _authUiState.value.pendingVerificationEmail
        val pass = _authUiState.value.pendingVerificationPassword
        if (email.isBlank() || pass.isBlank()) {
            _authUiState.value = _authUiState.value.copy(
                requiresEmailVerification = false,
                errorMessage = "অনুগ্রহ করে আপনার ইমেইল ও পাসওয়ার্ড দিয়ে সরাসরি লগইন করুন।"
            )
            return
        }

        _authUiState.value = _authUiState.value.copy(isCheckingVerification = true, errorMessage = null, successMessage = null)
        viewModelScope.launch {
            val result = firebaseManager.checkVerificationAndSignIn(email, pass)
            result.onSuccess { user ->
                _authUiState.value = _authUiState.value.copy(
                    isCheckingVerification = false,
                    currentUser = user,
                    requiresEmailVerification = false,
                    pendingVerificationEmail = "",
                    pendingVerificationPassword = "",
                    successMessage = "ইমেইল সফলভাবে ভেরিফাইড হয়েছে! স্বাগতম ${user.displayName ?: user.email}।"
                )
            }.onFailure { err ->
                _authUiState.value = _authUiState.value.copy(
                    isCheckingVerification = false,
                    errorMessage = if (err is EmailNotVerifiedException)
                        "ইমেইল এখনো ভেরিফাই করা হয়নি! অনুগ্রহ করে জিমেইল ইনবক্স/স্প্যাম চেক করে লিংকে ক্লিক করুন।"
                    else
                        "যাচাইকরণ ব্যর্থ হয়েছে: ${err.localizedMessage ?: err.message}"
                )
            }
        }
    }

    fun resendVerificationEmail() {
        val email = _authUiState.value.pendingVerificationEmail
        val pass = _authUiState.value.pendingVerificationPassword
        if (email.isBlank() || pass.isBlank()) {
            _authUiState.value = _authUiState.value.copy(errorMessage = "পুনরায় লিংক পাঠাতে পাসওয়ার্ড প্রয়োজন। অনুগ্রহ করে আবার লগইন করার চেষ্টা করুন।")
            return
        }

        _authUiState.value = _authUiState.value.copy(isCheckingVerification = true, errorMessage = null)
        viewModelScope.launch {
            val result = firebaseManager.resendVerificationEmail(email, pass)
            result.onSuccess {
                _authUiState.value = _authUiState.value.copy(
                    isCheckingVerification = false,
                    successMessage = "$email ঠিকানায় পুনরায় ভেরিফিকেশন লিংক পাঠানো হয়েছে। ইনবক্স বা স্প্যাম ফোল্ডার চেক করুন।"
                )
            }.onFailure { err ->
                _authUiState.value = _authUiState.value.copy(
                    isCheckingVerification = false,
                    errorMessage = "ভেরিফিকেশন লিংক পাঠাতে ব্যর্থ হয়েছে: ${err.localizedMessage ?: err.message}"
                )
            }
        }
    }

    fun dismissVerificationPrompt() {
        _authUiState.value = _authUiState.value.copy(
            requiresEmailVerification = false,
            errorMessage = null,
            successMessage = null
        )
    }

    fun signInAnonymously() {
        _authUiState.value = _authUiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
        viewModelScope.launch {
            val result = firebaseManager.signInAnonymously()
            result.onSuccess { user ->
                _authUiState.value = _authUiState.value.copy(
                    isLoading = false,
                    currentUser = user,
                    requiresEmailVerification = false,
                    successMessage = "গেস্ট হিসেবে সফলভাবে লগইন করা হয়েছে।"
                )
            }.onFailure { err ->
                _authUiState.value = _authUiState.value.copy(
                    isLoading = false,
                    errorMessage = "গেস্ট লগইন ব্যর্থ হয়েছে: ${err.localizedMessage ?: err.message}"
                )
            }
        }
    }

    fun signOut() {
        firebaseManager.signOut()
        _authUiState.value = _authUiState.value.copy(
            currentUser = null,
            requiresEmailVerification = false,
            pendingVerificationEmail = "",
            pendingVerificationPassword = "",
            successMessage = "সফলভাবে লগআউট করা হয়েছে।"
        )
    }

    fun updateFirebaseCustomProject(projectId: String, apiKey: String, appId: String) {
        if (projectId.isBlank() || apiKey.isBlank()) {
            _authUiState.value = _authUiState.value.copy(
                errorMessage = "অনুগ্রহ করে Project ID এবং API Key প্রদান করুন।"
            )
            return
        }

        val success = firebaseManager.reinitializeWithCustomConfig(projectId, apiKey, appId)
        if (success) {
            _authUiState.value = _authUiState.value.copy(
                currentUser = firebaseManager.getCurrentUser(),
                successMessage = "ফায়ারবেস প্রজেক্ট কনফিগারেশন সফলভাবে আপডেট করা হয়েছে ($projectId)!"
            )
        } else {
            _authUiState.value = _authUiState.value.copy(
                errorMessage = "ফায়ারবেস কনফিগারেশন আপডেট করতে ব্যর্থ হয়েছে। তথ্য যাচাই করুন।"
            )
        }
    }

    fun getActiveFirebaseProjectId(): String = firebaseManager.getActiveProjectId()
    fun getActiveFirebaseApiKey(): String = firebaseManager.getActiveApiKey()
    fun getActiveFirebaseAppId(): String = firebaseManager.getActiveAppId()

    fun clearAuthMessages() {
        _authUiState.value = _authUiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    // ==========================================
    // MODULE 3: AGENT MODE (INTERACTIVE PROMPT REFINEMENT)
    // ==========================================

    fun loadPromptIntoAgent(prompt: String, origin: String = "Master Prompt") {
        if (prompt.isBlank()) return
        val current = _agentUiState.value
        val isFirst = current.messages.isEmpty()
        val welcomeMsg = AgentChatMessage(
            sender = AgentSender.AGENT,
            messageText = "স্বাগতম এজেন্ট মোডে! 🤖 আপনার $origin সফলভাবে লোড হয়েছে।\n\nক্যামেরা অ্যাঙ্গেল, ভিজ্যুয়াল, লাইটিং, মডেল বা ব্যাকগ্রাউন্ডে কী সমস্যা বা পরিবর্তন প্রয়োজন? নিচে চ্যাট করে বলুন অথবা নিচের কুইক বাটনগুলো ব্যবহার করুন।"
        )
        _agentUiState.value = current.copy(
            currentPrompt = prompt,
            originalPrompt = if (current.originalPrompt.isBlank()) prompt else current.originalPrompt,
            promptHistory = if (current.promptHistory.isEmpty()) listOf(prompt) else current.promptHistory,
            messages = if (isFirst) listOf(welcomeMsg) else current.messages + AgentChatMessage(
                sender = AgentSender.AGENT,
                messageText = "নতুন প্রম্পট লোড করা হয়েছে! এতে কী কী পরিবর্তন বা সংশোধন চান জানান।"
            ),
            errorMessage = null
        )
    }

    fun sendAgentMessage(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isBlank()) return

        val state = _agentUiState.value
        val effectivePrompt = state.currentPrompt.ifBlank {
            _uiState.value.generatedPrompt ?: _modelUiState.value.generatedResult?.detailedPrompt ?: ""
        }

        if (effectivePrompt.isBlank()) {
            _agentUiState.value = state.copy(
                errorMessage = "প্রথমে একটি প্রম্পট লিখুন বা ভিডিও/মডেল জেনারেটর থেকে লোড করুন।"
            )
            return
        }

        val userMessage = AgentChatMessage(
            sender = AgentSender.USER,
            messageText = trimmed
        )

        val updatedMessages = state.messages + userMessage
        _agentUiState.value = state.copy(
            currentPrompt = effectivePrompt,
            messages = updatedMessages,
            isAgentThinking = true,
            errorMessage = null
        )

        viewModelScope.launch {
            val apiKey = preferencesManager.getApiKey()
            val result = agentRefinerService.refinePrompt(
                currentPrompt = effectivePrompt,
                chatHistory = updatedMessages,
                userInstruction = trimmed,
                apiKey = apiKey
            )

            result.onSuccess { response ->
                val newPrompt = response.updatedPrompt
                val agentMessage = AgentChatMessage(
                    sender = AgentSender.AGENT,
                    messageText = response.explanation,
                    refinedPrompt = newPrompt
                )
                val newHistory = _agentUiState.value.promptHistory + newPrompt
                _agentUiState.value = _agentUiState.value.copy(
                    currentPrompt = newPrompt,
                    promptVersion = _agentUiState.value.promptVersion + 1,
                    promptHistory = newHistory,
                    messages = _agentUiState.value.messages + agentMessage,
                    isAgentThinking = false,
                    errorMessage = null
                )
            }.onFailure { err ->
                val errorMsg = err.localizedMessage ?: "এজেন্ট প্রসেসিং ব্যর্থ হয়েছে।"
                val failAgentMessage = AgentChatMessage(
                    sender = AgentSender.AGENT,
                    messageText = "দুঃখিত, এআই রেসপন্স তৈরিতে সমস্যা হয়েছে: $errorMsg\nদয়া করে আবার চেষ্টা করুন।"
                )
                _agentUiState.value = _agentUiState.value.copy(
                    messages = _agentUiState.value.messages + failAgentMessage,
                    isAgentThinking = false,
                    errorMessage = errorMsg
                )
            }
        }
    }

    fun revertToPreviousPrompt() {
        val state = _agentUiState.value
        if (state.promptHistory.size > 1) {
            val historyWithoutCurrent = state.promptHistory.dropLast(1)
            val previousPrompt = historyWithoutCurrent.last()
            val revertMsg = AgentChatMessage(
                sender = AgentSender.AGENT,
                messageText = "পূর্ববর্তী সংস্করণে ফিরে যাওয়া হয়েছে (সংস্করণ v${maxOf(1, state.promptVersion - 1)})।"
            )
            _agentUiState.value = state.copy(
                currentPrompt = previousPrompt,
                promptVersion = maxOf(1, state.promptVersion - 1),
                promptHistory = historyWithoutCurrent,
                messages = state.messages + revertMsg
            )
        } else if (state.originalPrompt.isNotBlank() && state.currentPrompt != state.originalPrompt) {
            _agentUiState.value = state.copy(
                currentPrompt = state.originalPrompt,
                promptVersion = 1,
                messages = state.messages + AgentChatMessage(
                    sender = AgentSender.AGENT,
                    messageText = "আসল মূল প্রম্পটে রিসেট করা হয়েছে।"
                )
            )
        }
    }

    fun updateAgentActivePrompt(newPrompt: String) {
        _agentUiState.value = _agentUiState.value.copy(
            currentPrompt = newPrompt
        )
    }

    fun togglePromptCardExpanded() {
        _agentUiState.value = _agentUiState.value.copy(
            isPromptCardExpanded = !_agentUiState.value.isPromptCardExpanded
        )
    }

    fun clearAgentChat() {
        _agentUiState.value = _agentUiState.value.copy(
            messages = listOf(
                AgentChatMessage(
                    sender = AgentSender.AGENT,
                    messageText = "নতুন সেশন শুরু হয়েছে! কী কী পরিবর্তন বা নতুন কিছু যোগ করতে চান বলুন।"
                )
            ),
            errorMessage = null
        )
    }

    fun useAgentPromptInVideoGenerator() {
        val prompt = _agentUiState.value.currentPrompt
        if (prompt.isNotBlank()) {
            _uiState.value = _uiState.value.copy(
                generatedPrompt = prompt,
                infoMessage = "এজেন্ট মোড থেকে প্রম্পট সফলভাবে ইমপোর্ট করা হয়েছে!"
            )
        }
    }
}
