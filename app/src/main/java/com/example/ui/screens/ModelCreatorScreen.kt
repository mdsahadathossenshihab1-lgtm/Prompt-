package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.AiModelEntity
import com.example.model.AiModelConfig
import com.example.model.ModelPreset
import com.example.model.ModelPresets
import com.example.ui.PromptFlowViewModel
import com.example.ui.components.AiPulseLoader
import com.example.ui.components.AppBadge
import com.example.ui.components.SectionHeader
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ModelCreatorScreen(
    viewModel: PromptFlowViewModel,
    onNavigateToVideoGenerator: () -> Unit,
    modifier: Modifier = Modifier
) {
    val modelConfig by viewModel.modelConfig.collectAsState()
    val modelUiState by viewModel.modelUiState.collectAsState()
    val savedModels by viewModel.savedModels.collectAsState()
    val context = LocalContext.current

    var selectedSubTab by remember { mutableIntStateOf(0) } // 0: Creator, 1: Library
    var showSaveDialog by remember { mutableStateOf(false) }
    var saveModelName by remember { mutableStateOf("") }
    var showPromptDetails by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editInstructionText by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.updateModelConfig { it.copy(referenceImageUri = uri.toString()) }
            Toast.makeText(context, "Reference photo uploaded for model creation", Toast.LENGTH_SHORT).show()
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWideScreen = maxWidth > 840.dp

        Column(modifier = Modifier.fillMaxSize()) {
            // Header Banner
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "AI MODEL CREATOR",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                AppBadge(
                                    text = "MODULE 2",
                                    backgroundColor = NeonPurple.copy(alpha = 0.2f),
                                    textColor = NeonPurple
                                )
                            }
                            Text(
                                text = "Create realistic, video-ready human models for your AI videos.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Model Locked Status Badge
                        if (modelUiState.isModelLocked) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = NeonGreen.copy(alpha = 0.15f),
                                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(NeonGreen, NeonCyan))),
                                modifier = Modifier.clickable { viewModel.toggleLockModel(false) }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Model Locked",
                                        tint = NeonGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "MODEL LOCKED",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonGreen
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Sub Tabs: Create Model vs Model Library
                    TabRow(
                        selectedTabIndex = selectedSubTab,
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedSubTab == 0,
                            onClick = { selectedSubTab = 0 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Create Model")
                                }
                            }
                        )
                        Tab(
                            selected = selectedSubTab == 1,
                            onClick = { selectedSubTab = 1 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Collections, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Model Library (${savedModels.size})")
                                }
                            }
                        )
                    }
                }
            }

            // Messages / Info banner
            AnimatedVisibility(
                visible = modelUiState.infoMessage != null || modelUiState.errorMessage != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    color = if (modelUiState.errorMessage != null)
                        MaterialTheme.colorScheme.errorContainer
                    else
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = modelUiState.errorMessage ?: modelUiState.infoMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (modelUiState.errorMessage != null)
                                MaterialTheme.colorScheme.onErrorContainer
                            else
                                MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearModelMessages() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Dismiss",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Main Content Area
            if (selectedSubTab == 0) {
                // Creator View
                if (isWideScreen) {
                    // Split screen: Controls on Left, Generated Image on Right
                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .weight(1.1f)
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp)
                        ) {
                            ModelControlsSection(
                                config = modelConfig,
                                onConfigChange = { viewModel.updateModelConfig(it) },
                                onApplyPreset = { viewModel.applyModelPreset(it) },
                                onPickImage = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                onGenerate = { viewModel.generateAiModel(isSimilar = false) },
                                isGenerating = modelUiState.isGenerating
                            )
                        }

                        VerticalDivider()

                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp)
                        ) {
                            GeneratedModelPreviewSection(
                                uiState = modelUiState,
                                config = modelConfig,
                                onUseThisModel = {
                                    viewModel.useThisModelInPromptGenerator()
                                    onNavigateToVideoGenerator()
                                },
                                onRegenerate = { viewModel.generateAiModel(isSimilar = false) },
                                onGenerateSimilar = { viewModel.generateAiModel(isSimilar = true) },
                                onToggleLock = { viewModel.toggleLockModel(!modelUiState.isModelLocked) },
                                onSaveToLibrary = {
                                    saveModelName = modelUiState.generatedResult?.suggestedName ?: ""
                                    showSaveDialog = true
                                },
                                onOpenEditDialog = { showEditDialog = true },
                                onSaveToGallery = { viewModel.saveModelToPhoneGallery() },
                                showPromptDetails = showPromptDetails,
                                onTogglePromptDetails = { showPromptDetails = !showPromptDetails }
                            )
                        }
                    }
                } else {
                    // Compact Single-column scrollable
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            ModelControlsSection(
                                config = modelConfig,
                                onConfigChange = { viewModel.updateModelConfig(it) },
                                onApplyPreset = { viewModel.applyModelPreset(it) },
                                onPickImage = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                onGenerate = { viewModel.generateAiModel(isSimilar = false) },
                                isGenerating = modelUiState.isGenerating
                            )
                        }

                        // Generated Image Preview if available
                        if (modelUiState.generatedResult != null || modelUiState.isGenerating || modelUiState.isEditing) {
                            item {
                                GeneratedModelPreviewSection(
                                    uiState = modelUiState,
                                    config = modelConfig,
                                    onUseThisModel = {
                                        viewModel.useThisModelInPromptGenerator()
                                        onNavigateToVideoGenerator()
                                    },
                                    onRegenerate = { viewModel.generateAiModel(isSimilar = false) },
                                    onGenerateSimilar = { viewModel.generateAiModel(isSimilar = true) },
                                    onToggleLock = { viewModel.toggleLockModel(!modelUiState.isModelLocked) },
                                    onSaveToLibrary = {
                                        saveModelName = modelUiState.generatedResult?.suggestedName ?: ""
                                        showSaveDialog = true
                                    },
                                    onOpenEditDialog = { showEditDialog = true },
                                    onSaveToGallery = { viewModel.saveModelToPhoneGallery() },
                                    showPromptDetails = showPromptDetails,
                                    onTogglePromptDetails = { showPromptDetails = !showPromptDetails }
                                )
                            }
                        }
                    }
                }
            } else {
                // Model Library View
                ModelLibrarySection(
                    savedModels = savedModels,
                    onUseModel = { model ->
                        viewModel.useThisModelInPromptGenerator(
                            imageUrl = model.localFilePath ?: model.imageUrl,
                            modelName = model.name,
                            promptDesc = model.promptText
                        )
                        Toast.makeText(context, "Model '${model.name}' loaded into Video Prompt Generator!", Toast.LENGTH_SHORT).show()
                        onNavigateToVideoGenerator()
                    },
                    onToggleFavorite = { id, isFav ->
                        viewModel.toggleFavoriteModel(id, isFav)
                    },
                    onDeleteModel = { model ->
                        viewModel.deleteSavedModel(model)
                    },
                    onDownloadModel = { model ->
                        viewModel.saveModelToPhoneGallery(model.name)
                    }
                )
            }
        }
    }

    // Edit Model with Prompt Dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ছবি এডিট করুন (Edit Model)", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        "মডেলের মূল পরিচয় এবং চেহারা অপরিবর্তিত রেখে কি পরিবর্তন করতে চান তা লিখুন (বাংলা বা ইংরেজিতে):",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = editInstructionText,
                        onValueChange = { editInstructionText = it },
                        label = { Text("এডিট প্রম্পট (Edit Instruction)") },
                        placeholder = { Text("e.g. নীল রঙের শার্ট পরাও, মুখে মৃদু হাসি দাও, চোখে চশমা পরাও...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("দ্রুত পরিবর্তনের আইডিয়া:", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("নীল শার্ট দাও", "মুখে হাসি দাও", "চশমা পরাও", "কালো ব্লেজার দাও", "ব্যাকগ্রাউন্ড অফিস করো", "হাতে মাইক দাও").forEach { tip ->
                            FilterChip(
                                selected = false,
                                onClick = {
                                    editInstructionText = if (editInstructionText.isBlank()) tip else "$editInstructionText, $tip"
                                },
                                label = { Text(tip, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editInstructionText.isNotBlank()) {
                            viewModel.editModelWithPrompt(editInstructionText)
                            showEditDialog = false
                            editInstructionText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                ) {
                    Text("পরিবর্তন প্রয়োগ করুন", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Save Model to Library Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Model to Library") },
            text = {
                Column {
                    Text(
                        "Give this character model a recognizable name so you can reuse it across video generations:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = saveModelName,
                        onValueChange = { saveModelName = it },
                        label = { Text("Model Name") },
                        placeholder = { Text("e.g. Male Rural Presenter 01") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveCurrentModelToLibrary(saveModelName)
                        showSaveDialog = false
                        Toast.makeText(context, "Saved to Model Library!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Save Model")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun VerticalDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(1.dp)
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ModelControlsSection(
    config: AiModelConfig,
    onConfigChange: ((AiModelConfig) -> AiModelConfig) -> Unit,
    onApplyPreset: (ModelPreset) -> Unit,
    onPickImage: () -> Unit,
    onGenerate: () -> Unit,
    isGenerating: Boolean
) {
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

        // 1. One-click Presets (Section 18)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                SectionHeader(
                    title = "Model Presets",
                    subtitle = "One-click templates with optimal Bangladeshi & presenter setups",
                    icon = Icons.Default.AutoAwesome
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ModelPresets.presets.forEach { preset ->
                        FilterChip(
                            selected = false,
                            onClick = { onApplyPreset(preset) },
                            label = { Text(preset.title, fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
            }
        }

        // 2. Describe Your Model (Most Important Input - Section 3 & 4)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                SectionHeader(
                    title = "Describe Your Model",
                    subtitle = "Type naturally in Bengali, Banglish, or English (AI infers all missing details)",
                    icon = Icons.Default.Edit
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = config.description,
                    onValueChange = { newDesc -> onConfigChange { it.copy(description = newDesc) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("model_description_input"),
                    placeholder = {
                        Text(
                            "e.g. একজন ৩০ বছরের বাংলাদেশি পুরুষ মডেল তৈরি করো। শার্ট পরা থাকবে, গ্রামের মাঠে দাঁড়িয়ে থাকবে। নির্বাচনী ভিডিওর presenter-এর মতো professional কিন্তু natural look চাই। বুকে ছোট lavalier mic থাকবে।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = clipboard.primaryClip
                                if (clip != null && clip.itemCount > 0) {
                                    val text = clip.getItemAt(0).coerceToText(context).toString()
                                    onConfigChange { it.copy(description = text) }
                                    Toast.makeText(context, "Pasted from clipboard", Toast.LENGTH_SHORT).show()
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Paste", fontSize = 12.sp)
                        }

                        if (config.description.isNotBlank()) {
                            OutlinedButton(
                                onClick = { onConfigChange { it.copy(description = "") } },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Clear", fontSize = 12.sp)
                            }
                        }
                    }

                    Text(
                        text = "${config.description.length} chars",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 3. Model Attributes & Identity Controls
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeader(
                    title = "Model Identity & Features",
                    subtitle = "Fine-tune demographics, environment, wardrobe, and framing",
                    icon = Icons.Default.Tune
                )

                // Model Type Dropdown
                var expandedModelType by remember { mutableStateOf(false) }
                val modelTypes = listOf(
                    "Public / Election / Community Video Model",
                    "News / Media Presenter",
                    "Corporate Model",
                    "Studio Presenter",
                    "Outdoor Presenter",
                    "Educational Model",
                    "Commercial / Advertising Model",
                    "Social Media Model",
                    "Custom Model"
                )

                ExposedDropdownMenuBox(
                    expanded = expandedModelType,
                    onExpandedChange = { expandedModelType = !expandedModelType }
                ) {
                    OutlinedTextField(
                        value = config.modelType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Model Type / Purpose") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedModelType) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedModelType,
                        onDismissRequest = { expandedModelType = false }
                    ) {
                        modelTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type, style = MaterialTheme.typography.bodyMedium) },
                                onClick = {
                                    onConfigChange { it.copy(modelType = type) }
                                    expandedModelType = false
                                }
                            )
                        }
                    }
                }

                // Gender & Age Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Gender
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Gender", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Male", "Female", "Other").forEach { g ->
                                FilterChip(
                                    selected = config.gender == g,
                                    onClick = { onConfigChange { it.copy(gender = g) } },
                                    label = { Text(g, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    // Aspect Ratio (Section 19: Default 9:16)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Aspect Ratio", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("9:16", "16:9", "1:1").forEach { ratio ->
                                FilterChip(
                                    selected = config.aspectRatio == ratio,
                                    onClick = { onConfigChange { it.copy(aspectRatio = ratio) } },
                                    label = { Text(ratio, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }

                // Age Range Chips
                Column {
                    Text("Age Range", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("18–25", "25–35", "35–45", "45–60", "Custom").forEach { age ->
                            FilterChip(
                                selected = config.ageRange == age,
                                onClick = { onConfigChange { it.copy(ageRange = age) } },
                                label = { Text(age, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Location / Environment Chips
                Column {
                    Text("Location / Environment", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Studio", "Office", "Urban", "Rural", "Street", "Field", "Custom").forEach { loc ->
                            FilterChip(
                                selected = config.location == loc,
                                onClick = { onConfigChange { it.copy(location = loc) } },
                                label = { Text(loc, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Camera Framing (Section 5 & 6)
                Column {
                    Text("Camera Framing", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Chest-up", "Close-up", "Waist-up", "Full-body").forEach { framing ->
                            FilterChip(
                                selected = config.cameraFraming == framing,
                                onClick = { onConfigChange { it.copy(cameraFraming = framing) } },
                                label = { Text(framing, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Clothing Text Input
                OutlinedTextField(
                    value = config.clothing,
                    onValueChange = { newCloth -> onConfigChange { it.copy(clothing = newCloth) } },
                    label = { Text("Clothing / Wardrobe") },
                    placeholder = { Text(if (config.gender == "Female") "e.g. Saree, Salwar Kameez, Blazer, Abaya" else "e.g. Formal shirt, Panjabi, Blazer, Polo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Quick Wardrobe suggestions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val clothes = if (config.gender == "Female")
                        listOf("Saree", "Salwar Kameez", "Formal Blazer", "Three-piece", "Abaya")
                    else
                        listOf("Formal Shirt", "Panjabi", "Dark Blazer", "Polo Shirt", "Suit")

                    clothes.forEach { c ->
                        FilterChip(
                            selected = false,
                            onClick = { onConfigChange { it.copy(clothing = c) } },
                            label = { Text(c, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        )
                    }
                }

                // Regional Bangladeshi Context (Section 9)
                Column {
                    Text("Regional Appearance Context", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("None", "Dhaka", "Chattogram", "Sylhet", "Barisal", "Cumilla", "Rajshahi", "Khulna", "Rangpur").forEach { reg ->
                            FilterChip(
                                selected = config.regionContext == reg,
                                onClick = { onConfigChange { it.copy(regionContext = reg) } },
                                label = { Text(reg, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Microphone Option (Section 7)
                Column {
                    Text("Microphone & Audio Accessory", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Auto (Lavalier if suitable)", "No microphone", "Handheld mic", "Headset").forEach { mic ->
                            FilterChip(
                                selected = config.microphoneOption == mic,
                                onClick = { onConfigChange { it.copy(microphoneOption = mic) } },
                                label = { Text(mic, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }

        // 4. Reference Image / Image-To-Model (Section 16)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                SectionHeader(
                    title = "Image-to-Model (Optional Reference)",
                    subtitle = "Upload an existing photo to extract features, pose, or appearance",
                    icon = Icons.Default.AddPhotoAlternate
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (config.referenceImageUri != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        AsyncImage(
                            model = config.referenceImageUri,
                            contentDescription = "Uploaded reference",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { onConfigChange { it.copy(referenceImageUri = null) } },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Reference Adaptation Mode:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Preserve Face",
                            "Change Clothing",
                            "Change Background",
                            "Change Pose",
                            "Change Camera Framing",
                            "Keep Overall Appearance"
                        ).forEach { opt ->
                            FilterChip(
                                selected = config.imageToModelOption == opt,
                                onClick = { onConfigChange { it.copy(imageToModelOption = opt) } },
                                label = { Text(opt, fontSize = 11.sp) }
                            )
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = onPickImage,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Upload Reference Photo")
                    }
                }
            }
        }

        // 5. Two-Person Scene Support (Section 17)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Two-Person Scene", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Create male + female co-presenters in one frame", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = config.isTwoPersonScene,
                        onCheckedChange = { isChecked -> onConfigChange { it.copy(isTwoPersonScene = isChecked) } }
                    )
                }

                if (config.isTwoPersonScene) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = config.model2Description,
                        onValueChange = { desc -> onConfigChange { it.copy(model2Description = desc) } },
                        label = { Text("Model 2 Description & Role") },
                        placeholder = { Text("e.g. Female co-presenter standing beside him in traditional blue saree, smiling") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        // 6. Main Action Button: GENERATE MODEL (Section 13)
        Button(
            onClick = onGenerate,
            enabled = !isGenerating,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("generate_model_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            if (isGenerating)
                                listOf(Color.Gray, Color.DarkGray)
                            else
                                listOf(NeonCyan, NeonPurple)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isGenerating) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("GENERATING MODEL...", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "GENERATE MODEL",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GeneratedModelPreviewSection(
    uiState: com.example.ui.AiModelUiState,
    config: AiModelConfig,
    onUseThisModel: () -> Unit,
    onRegenerate: () -> Unit,
    onGenerateSimilar: () -> Unit,
    onToggleLock: () -> Unit,
    onSaveToLibrary: () -> Unit,
    onOpenEditDialog: () -> Unit = {},
    onSaveToGallery: () -> Unit = {},
    showPromptDetails: Boolean,
    onTogglePromptDetails: () -> Unit
) {
    val context = LocalContext.current
    val result = uiState.generatedResult

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(NeonCyan, NeonPurple)))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "GENERATED MODEL",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AppBadge(
                        text = config.aspectRatio,
                        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                        textColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                if (result != null) {
                    Text(
                        text = "Seed #${result.seed}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Image Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        when (config.aspectRatio) {
                            "16:9" -> Modifier.aspectRatio(16f / 9f)
                            "1:1" -> Modifier.aspectRatio(1f)
                            else -> Modifier.height(380.dp) // 9:16 vertical
                        }
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isGenerating || uiState.isEditing) {
                    AiPulseLoader(
                        statusText = if (uiState.isEditing) "ছবি এআই দিয়ে এডিট করা হচ্ছে..." else "ফটোরিয়ালিস্টিক মডেল জেনারেট হচ্ছে...",
                        subtitleText = if (uiState.isEditing) "মুখ ও ফেস স্ট্রাকচার অক্ষুণ্ণ রেখে প্রম্পট প্রয়োগ করা হচ্ছে" else "ফটোরিয়ালিস্টিক সাউথ এশিয়ান হিউম্যান মডেল তৈরি করা হচ্ছে",
                        size = 90.dp
                    )
                } else if (result != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(result.localFilePath ?: result.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Generated Model Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Overlay badge: MODEL LOCKED if active
                    if (uiState.isModelLocked) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(10.dp),
                            shape = RoundedCornerShape(6.dp),
                            color = Color.Black.copy(alpha = 0.75f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("MODEL LOCKED", color = NeonGreen, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Edit History Badges
            if (result != null && result.editHistory.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("এডিট হিস্ট্রি:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    result.editHistory.forEach { edit ->
                        AppBadge(
                            text = "✓ $edit",
                            backgroundColor = NeonPurple.copy(alpha = 0.15f),
                            textColor = NeonPurple
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (result != null) {
                // PRIMARY ACTION: USE THIS MODEL (Section 22 & 23)
                Button(
                    onClick = onUseThisModel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("use_this_model_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(listOf(NeonGreen, NeonCyan))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Movie, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "USE THIS MODEL (ATTACH TO VIDEO PROMPT)",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Black,
                                color = Color.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // HIGH PRIORITY: EDIT MODEL & DIRECT DOWNLOAD ROW
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Edit Model with Prompt (ছবি এডিট করুন)
                    Button(
                        onClick = onOpenEditDialog,
                        enabled = !uiState.isEditing && !uiState.isGenerating,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ছবি এডিট করুন", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Download directly to Phone Gallery (ফোনে ডাউনলোড)
                    Button(
                        onClick = onSaveToGallery,
                        enabled = !uiState.isSavingToGallery,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        if (uiState.isSavingToGallery) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Download, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ফোনে ডাউনলোড", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Secondary Action Row (Section 13)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Regenerate
                    OutlinedButton(
                        onClick = onRegenerate,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Regenerate", fontSize = 11.sp)
                    }

                    // Generate Similar (Section 14)
                    OutlinedButton(
                        onClick = onGenerateSimilar,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Similar", fontSize = 11.sp)
                    }

                    // Lock Model (Section 15)
                    OutlinedButton(
                        onClick = onToggleLock,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            if (uiState.isModelLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = null,
                            tint = if (uiState.isModelLocked) NeonGreen else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (uiState.isModelLocked) "Locked" else "Lock", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Save to Library
                    OutlinedButton(
                        onClick = onSaveToLibrary,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Collections, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (uiState.isSavedToLibrary) "Saved ✓" else "Save to Library", fontSize = 11.sp)
                    }

                    // Copy Image Prompt
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Image Prompt", result.detailedPrompt))
                            Toast.makeText(context, "25-point visual prompt copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy Prompt", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Expandable 25-Point Visual Prompt Details
                Surface(
                    onClick = onTogglePromptDetails,
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("View 25-Point Image Prompt Engine Details", style = MaterialTheme.typography.labelSmall)
                        }
                        Icon(
                            if (showPromptDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                if (showPromptDetails) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = result.detailedPrompt,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModelLibrarySection(
    savedModels: List<AiModelEntity>,
    onUseModel: (AiModelEntity) -> Unit,
    onToggleFavorite: (Long, Boolean) -> Unit,
    onDeleteModel: (AiModelEntity) -> Unit,
    onDownloadModel: (AiModelEntity) -> Unit = {}
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var filterFavoritesOnly by remember { mutableStateOf(false) }

    val filteredList = remember(savedModels, searchQuery, filterFavoritesOnly) {
        savedModels.filter { model ->
            val matchesQuery = searchQuery.isBlank() ||
                    model.name.contains(searchQuery, ignoreCase = true) ||
                    model.gender.contains(searchQuery, ignoreCase = true) ||
                    model.style.contains(searchQuery, ignoreCase = true) ||
                    model.environment.contains(searchQuery, ignoreCase = true)

            val matchesFav = !filterFavoritesOnly || model.isFavorite
            matchesQuery && matchesFav
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search & Filters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Models") },
                placeholder = { Text("Search by name, gender, style...") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            )

            FilterChip(
                selected = filterFavoritesOnly,
                onClick = { filterFavoritesOnly = !filterFavoritesOnly },
                label = { Text("⭐ Favorites") }
            )
        }

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.Collections,
                        contentDescription = null,
                        modifier = Modifier.size(54.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (savedModels.isEmpty()) "No saved models yet" else "No matching models found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (savedModels.isEmpty()) "Generate your first human model in the Creator tab and click 'Save to Library'." else "Try a different search term.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredList, key = { it.id }) { model ->
                    ModelLibraryCard(
                        model = model,
                        onUseModel = { onUseModel(model) },
                        onToggleFavorite = { onToggleFavorite(model.id, !model.isFavorite) },
                        onDeleteModel = { onDeleteModel(model) },
                        onDownloadModel = { onDownloadModel(model) }
                    )
                }
            }
        }
    }
}

@Composable
fun ModelLibraryCard(
    model: AiModelEntity,
    onUseModel: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDeleteModel: () -> Unit,
    onDownloadModel: () -> Unit = {}
) {
    val context = LocalContext.current
    val formattedDate = remember(model.createdAt) {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(model.createdAt))
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = model.localFilePath ?: model.imageUrl,
                    contentDescription = model.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = model.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (model.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (model.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AppBadge(
                        text = model.gender,
                        backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        textColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    AppBadge(
                        text = model.environment,
                        backgroundColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        textColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    AppBadge(
                        text = model.aspectRatio,
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                        textColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Created: $formattedDate",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onUseModel,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Use", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Direct Download to Phone Gallery Button
                    OutlinedButton(
                        onClick = onDownloadModel,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Download to phone", modifier = Modifier.size(14.dp))
                    }

                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Model Prompt", model.promptText))
                            Toast.makeText(context, "Prompt copied!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                    }

                    IconButton(
                        onClick = onDeleteModel,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
