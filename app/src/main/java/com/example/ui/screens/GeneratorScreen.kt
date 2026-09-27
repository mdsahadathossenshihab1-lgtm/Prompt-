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
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.PromptConfig
import com.example.ui.PromptFlowViewModel
import com.example.ui.PromptUiState
import com.example.ui.components.AiPulseLoader
import com.example.ui.components.AppBadge
import com.example.ui.components.FilterChipGroup
import com.example.ui.components.OptionCheckboxItem
import com.example.ui.components.SectionHeader
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GeneratorScreen(
    viewModel: PromptFlowViewModel,
    onNavigateToModelCreator: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val config by viewModel.config.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setReferenceImageUri(uri.toString())
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWideScreen = maxWidth > 840.dp

        if (isWideScreen) {
            // Two-column layout for tablets / desktop
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Left Column: Inputs & Controls
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IntroBanner()
                    ScriptCard(config, viewModel, context)
                    ReferenceImageCard(
                        config = config,
                        viewModel = viewModel,
                        onPickImage = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onNavigateToModelCreator = onNavigateToModelCreator
                    )
                    VideoSettingsCard(config, viewModel)
                    VisualSettingsCard(config, viewModel)
                    GenerationOptionsCard(config, viewModel)
                    CustomInstructionsCard(config, viewModel)
                    GenerateButton(uiState, onGenerate = { viewModel.generateMasterPrompt() })
                    Spacer(modifier = Modifier.height(32.dp))
                }

                // Right Column: Master Prompt Output
                Column(
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    GeneratedPromptCard(
                        uiState = uiState,
                        viewModel = viewModel,
                        context = context
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        } else {
            // Single-column layout for phones
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IntroBanner()
                ScriptCard(config, viewModel, context)
                ReferenceImageCard(
                    config = config,
                    viewModel = viewModel,
                    onPickImage = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onNavigateToModelCreator = onNavigateToModelCreator
                )
                VideoSettingsCard(config, viewModel)
                VisualSettingsCard(config, viewModel)
                GenerationOptionsCard(config, viewModel)
                CustomInstructionsCard(config, viewModel)
                GenerateButton(uiState, onGenerate = { viewModel.generateMasterPrompt() })

                // Result display
                if (uiState.generatedPrompt != null || uiState.isGenerating || uiState.errorMessage != null) {
                    GeneratedPromptCard(
                        uiState = uiState,
                        viewModel = viewModel,
                        context = context
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun IntroBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "PromptFlow AI",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AppBadge(text = "DeepSeek V4.1")
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Create Professional AI Video Prompts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Turn your script and reference image into a production-ready AI video master prompt for Google Flow, Sora, Kling & Runway.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ScriptCard(
    config: PromptConfig,
    viewModel: PromptFlowViewModel,
    context: Context
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(
                    title = "Voice / Dialogue Script",
                    subtitle = "Bengali, English, or Banglish preserved exactly",
                    icon = Icons.Default.Movie
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Samples Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Sample:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier.clickable { viewModel.loadSampleBengaliScript() }
                ) {
                    Text(
                        text = "Bengali",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                    modifier = Modifier.clickable { viewModel.loadSampleEnglishScript() }
                ) {
                    Text(
                        text = "English",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = config.script,
                onValueChange = { viewModel.updateScript(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .testTag("dialogue_script_input"),
                placeholder = {
                    Text(
                        text = "Paste your voice script here...\n(e.g., আমাদের ডিজিটাল উদ্যোগ এখন প্রত্যন্ত গ্রামেও পৌঁছে গেছে...)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action row: paste, clear, character count
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
                                viewModel.updateScript(text)
                                Toast.makeText(context, "Script pasted from clipboard", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("paste_script_button")
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = "Paste", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Paste", style = MaterialTheme.typography.labelMedium)
                    }

                    if (config.script.isNotEmpty()) {
                        OutlinedButton(
                            onClick = { viewModel.updateScript("") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("clear_script_button")
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                Text(
                    text = "${config.script.length} characters",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ReferenceImageCard(
    config: PromptConfig,
    viewModel: PromptFlowViewModel,
    onPickImage: () -> Unit,
    onNavigateToModelCreator: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = if (config.isModelLocked)
                Brush.linearGradient(listOf(NeonCyan, NeonPurple))
            else
                Brush.linearGradient(listOf(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(
                    title = "Reference Image & Character",
                    subtitle = "Exact visual presenter reference for AI video generation",
                    icon = Icons.Default.AddPhotoAlternate
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Prominent MODEL LOCKED Banner (Section 23 Step 5)
            if (config.isModelLocked) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0F291E),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(0xFF00E676), NeonCyan))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Model Locked",
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "MODEL LOCKED",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF00E676)
                                )
                                Text(
                                    text = config.lockedModelName ?: "AI Model Creator Character Active",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }

                        TextButton(
                            onClick = { viewModel.clearLockedModel() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Unlock", color = Color(0xFF80D8FF), fontSize = 12.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (config.referenceImageUri != null) {
                // Image preview box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, if (config.isModelLocked) NeonCyan else MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                ) {
                    AsyncImage(
                        model = config.referenceImageUri,
                        contentDescription = "Presenter Reference Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Overlay badge
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (config.isModelLocked) "🔒 Locked Model Reference" else "Model Reference Loaded",
                            color = if (config.isModelLocked) Color(0xFF00E676) else NeonCyan,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Delete button overlay
                    IconButton(
                        onClick = { viewModel.clearLockedModel() },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove Image",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onPickImage,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Replace Photo", style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedButton(
                        onClick = onNavigateToModelCreator,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Model Creator", style = MaterialTheme.typography.labelMedium)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = config.referenceImageDescription,
                    onValueChange = { viewModel.setReferenceImageDescription(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Additional presenter notes (e.g. 28yo professional woman in formal blazer)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    shape = RoundedCornerShape(8.dp)
                )
            } else {
                // Upload trigger box + Shortcut to AI Model Creator
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onPickImage() }
                            .padding(18.dp)
                            .testTag("upload_reference_image_box"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Upload",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Upload Reference Image",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Upload photos to preserve facial geometry and appearance",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Quick Jump to AI Model Creator
                    Surface(
                        onClick = onNavigateToModelCreator,
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(NeonCyan, NeonPurple))),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Generate Model with AI Model Creator",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Create realistic video-ready human model & lock character identity",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VideoSettingsCard(
    config: PromptConfig,
    viewModel: PromptFlowViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            SectionHeader(
                title = "Video Settings",
                subtitle = "Aspect ratio, resolution, duration & format",
                icon = Icons.Default.Videocam
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Aspect Ratio
            Text(
                text = "Aspect Ratio",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            FilterChipGroup(
                items = listOf("9:16 (Reel/Shorts)", "16:9 (Landscape)", "1:1 (Square)"),
                selectedItem = when (config.aspectRatio) {
                    "16:9" -> "16:9 (Landscape)"
                    "1:1" -> "1:1 (Square)"
                    else -> "9:16 (Reel/Shorts)"
                },
                onItemSelected = { selected ->
                    val ratio = when {
                        selected.startsWith("16:9") -> "16:9"
                        selected.startsWith("1:1") -> "1:1"
                        else -> "9:16"
                    }
                    viewModel.updateAspectRatio(ratio)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Resolution
            Text(
                text = "Resolution",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            FilterChipGroup(
                items = listOf("1080x1920", "1920x1080", "1080x1080"),
                selectedItem = config.resolution,
                onItemSelected = { viewModel.updateResolution(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Duration
            Text(
                text = "Duration",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            FilterChipGroup(
                items = listOf("8 seconds", "10 seconds", "15 seconds", "20 seconds", "30 seconds"),
                selectedItem = config.duration,
                onItemSelected = { viewModel.updateDuration(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Video Style
            Text(
                text = "Video Style",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            FilterChipGroup(
                items = listOf("Cinematic", "Realistic", "Commercial", "Presenter"),
                selectedItem = when (config.videoStyle) {
                    "Realistic" -> "Realistic"
                    "Commercial" -> "Commercial"
                    "Professional Presenter", "Presenter" -> "Presenter"
                    else -> "Cinematic"
                },
                onItemSelected = { viewModel.updateVideoStyle(if (it == "Presenter") "Professional Presenter" else it) }
            )
        }
    }
}

@Composable
fun VisualSettingsCard(
    config: PromptConfig,
    viewModel: PromptFlowViewModel
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(
                    title = "Visual & Cinematography",
                    subtitle = "Location, presenter, camera angle, and B-roll",
                    icon = Icons.Default.Tune
                )
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle Visual Settings"
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Location
                    Text(
                        text = "Location",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    FilterChipGroup(
                        items = listOf("Outdoor", "Rural", "Urban", "Office", "Studio"),
                        selectedItem = config.location,
                        onItemSelected = { viewModel.updateLocation(it) }
                    )

                    // Presenter
                    Text(
                        text = "Presenter",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    FilterChipGroup(
                        items = listOf("Male", "Female", "Same as image", "No presenter"),
                        selectedItem = when (config.presenter) {
                            "Female" -> "Female"
                            "Same as reference image", "Same as image" -> "Same as image"
                            "No presenter" -> "No presenter"
                            else -> "Male"
                        },
                        onItemSelected = {
                            viewModel.updatePresenter(if (it == "Same as image") "Same as reference image" else it)
                        }
                    )

                    // Camera Framing
                    Text(
                        text = "Camera Angle & Framing",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    FilterChipGroup(
                        items = listOf("Medium shot", "Push-in", "Tracking", "Close-up"),
                        selectedItem = when (config.camera) {
                            "Slow push-in", "Push-in" -> "Push-in"
                            "Tracking" -> "Tracking"
                            "Medium close-up", "Close-up" -> "Close-up"
                            else -> "Medium shot"
                        },
                        onItemSelected = {
                            viewModel.updateCamera(
                                when (it) {
                                    "Push-in" -> "Slow push-in"
                                    "Close-up" -> "Medium close-up"
                                    else -> it
                                }
                            )
                        }
                    )

                    // B-Roll Cutaways (Visual Cutouts)
                    Column {
                        Text(
                            text = "B-Roll Cutaways (ভিজ্যুয়াল কাটআউটস)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ভিডিও চলাকালীন বক্তব্যের সাথে প্রাসঙ্গিক দৃশ্য ও কাটআউট সিন যুক্ত করুন",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                    FilterChipGroup(
                        items = listOf("Yes (কাটআউটস সহ)", "Auto (অটো সিন)", "No (শুধুমাত্র স্পিকার)"),
                        selectedItem = when (config.bRoll) {
                            "Yes" -> "Yes (কাটআউটস সহ)"
                            "No" -> "No (শুধুমাত্র স্পিকার)"
                            else -> "Auto (অটো সিন)"
                        },
                        onItemSelected = {
                            val selected = when {
                                it.startsWith("Yes") -> "Yes"
                                it.startsWith("No") -> "No"
                                else -> "Auto"
                            }
                            viewModel.updateBRoll(selected)
                        }
                    )

                    // On-Screen Text
                    Text(
                        text = "On-Screen Text",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    FilterChipGroup(
                        items = listOf("None", "Custom Text"),
                        selectedItem = if (config.onScreenText == "Custom") "Custom Text" else "None",
                        onItemSelected = {
                            viewModel.updateOnScreenText(if (it.startsWith("Custom")) "Custom" else "None")
                        }
                    )
                    if (config.onScreenText == "Custom") {
                        OutlinedTextField(
                            value = config.customText,
                            onValueChange = { viewModel.updateCustomText(it) },
                            placeholder = { Text("Specify on-screen titles or captions") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GenerationOptionsCard(
    config: PromptConfig,
    viewModel: PromptFlowViewModel
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(
                    title = "Strict Generation Mandates",
                    subtitle = "9 quality constraints enabled by default",
                    icon = Icons.Default.Check
                )
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle Options"
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    OptionCheckboxItem(
                        title = "Preserve exact dialogue",
                        description = "No rewriting, shortening, or translating",
                        checked = config.preserveExactDialogue,
                        onCheckedChange = { viewModel.toggleOption("preserveExactDialogue", it) }
                    )
                    OptionCheckboxItem(
                        title = "Maintain character consistency",
                        description = "100% facial geometry, hair, and wardrobe stability",
                        checked = config.maintainCharacterConsistency,
                        onCheckedChange = { viewModel.toggleOption("maintainCharacterConsistency", it) }
                    )
                    OptionCheckboxItem(
                        title = "Synchronize B-roll with dialogue",
                        description = "Cutaways match semantic meaning of spoken words",
                        checked = config.synchronizeBroll,
                        onCheckedChange = { viewModel.toggleOption("synchronizeBroll", it) }
                    )
                    OptionCheckboxItem(
                        title = "Natural lip-sync",
                        description = "Precise syllable and mouth alignment in single take",
                        checked = config.naturalLipSync,
                        onCheckedChange = { viewModel.toggleOption("naturalLipSync", it) }
                    )
                    OptionCheckboxItem(
                        title = "One continuous voice take",
                        description = "Single voice, no restarts, stops at final word",
                        checked = config.oneContinuousVoiceTake,
                        onCheckedChange = { viewModel.toggleOption("oneContinuousVoiceTake", it) }
                    )
                    OptionCheckboxItem(
                        title = "No dialogue repetition",
                        description = "Zero echoes, loops, or duplicate voices",
                        checked = config.noDialogueRepetition,
                        onCheckedChange = { viewModel.toggleOption("noDialogueRepetition", it) }
                    )
                    OptionCheckboxItem(
                        title = "Professional camera direction",
                        description = "Cinematic framing and smooth lens movement",
                        checked = config.professionalCameraDirection,
                        onCheckedChange = { viewModel.toggleOption("professionalCameraDirection", it) }
                    )
                    OptionCheckboxItem(
                        title = "Cinematic lighting",
                        description = "Volumetric ambient, key light, and rim shadows",
                        checked = config.cinematicLighting,
                        onCheckedChange = { viewModel.toggleOption("cinematicLighting", it) }
                    )
                    OptionCheckboxItem(
                        title = "Negative prompt",
                        description = "Strict anti-artifact and anti-distortion directives",
                        checked = config.negativePrompt,
                        onCheckedChange = { viewModel.toggleOption("negativePrompt", it) }
                    )
                }
            }
        }
    }
}

@Composable
fun CustomInstructionsCard(
    config: PromptConfig,
    viewModel: PromptFlowViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            SectionHeader(
                title = "Additional Instructions (Optional)",
                subtitle = "Custom constraints, background details, or visual cues"
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = config.customInstructions,
                onValueChange = { viewModel.updateCustomInstructions(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                placeholder = {
                    Text(
                        "Example:\nPresenter should stand beside a rural road.\nNo text on screen.\nSynchronize road development visual during dialogue.",
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
fun GenerateButton(
    uiState: PromptUiState,
    onGenerate: () -> Unit
) {
    Button(
        onClick = onGenerate,
        enabled = !uiState.isGenerating,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .testTag("generate_master_prompt_button"),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(NeonCyan, NeonPurple)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isGenerating) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Creating your master prompt...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Generate Master Prompt",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GeneratedPromptCard(
    uiState: PromptUiState,
    viewModel: PromptFlowViewModel,
    context: Context
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("generated_prompt_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(NeonCyan.copy(alpha = 0.5f), NeonPurple.copy(alpha = 0.5f))
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(
                    title = "Your Master Prompt",
                    subtitle = "Production-ready for Google Flow & AI Video generators",
                    icon = Icons.Default.AutoAwesome
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Error or info banners
            if (uiState.errorMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Text(
                        text = uiState.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            if (uiState.infoMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Text(
                        text = uiState.infoMessage,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            val prompt = uiState.generatedPrompt ?: ""

            if (uiState.isGenerating) {
                AiPulseLoader(
                    statusText = "AI মাস্টার প্রম্পট তৈরি করছে...",
                    subtitleText = "Google Flow, Sora, Runway ও Kling অপটিমাইজেশন চলছে",
                    size = 90.dp
                )
            } else if (uiState.isEditMode) {
                // Edit Mode Text Field
                OutlinedTextField(
                    value = uiState.editedPromptText,
                    onValueChange = { viewModel.updateEditedPromptText(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .testTag("edit_prompt_textfield"),
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 18.sp
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.saveEditedPrompt() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_changes_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Save Changes")
                    }

                    OutlinedButton(
                        onClick = { viewModel.cancelEditMode() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("cancel_edit_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel")
                    }
                }
            } else {
                // View Mode
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    SelectionContainer {
                        Text(
                            text = prompt.ifBlank { "Prompt will appear here once generated." },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Actions: Copy, Regenerate, Edit, Download, Clear
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Copy Prompt
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Master Prompt", prompt)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Prompt copied!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("copy_prompt_button")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy Prompt")
                    }

                    // Regenerate
                    OutlinedButton(
                        onClick = { viewModel.regeneratePrompt() },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("regenerate_prompt_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Regenerate")
                    }

                    // Edit
                    OutlinedButton(
                        onClick = { viewModel.startEditMode() },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("edit_prompt_button")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit")
                    }

                    // Download / Export
                    OutlinedButton(
                        onClick = {
                            saveOrSharePrompt(context, prompt)
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("download_prompt_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Download")
                    }

                    // Clear
                    OutlinedButton(
                        onClick = { viewModel.clearPrompt() },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("clear_prompt_result_button")
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear")
                    }
                }
            }
        }
    }
}

fun saveOrSharePrompt(context: Context, promptText: String) {
    try {
        val fileName = "promptflow-master-prompt.txt"
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { out ->
            out.write(promptText.toByteArray())
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "PromptFlow AI - Master Prompt")
            putExtra(Intent.EXTRA_TEXT, promptText)
        }
        val shareIntent = Intent.createChooser(sendIntent, "Export Master Prompt")
        context.startActivity(shareIntent)
        Toast.makeText(context, "Exporting prompt...", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to export: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
