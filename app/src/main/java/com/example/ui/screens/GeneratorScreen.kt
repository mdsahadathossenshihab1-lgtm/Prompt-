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
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.runtime.LaunchedEffect
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
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import kotlinx.coroutines.delay
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

    // Mobile comfort step tabs (0: Script, 1: Actor/Style, 2: Camera/Cutaways, 3: Rules)
    var currentStep by remember { mutableIntStateOf(0) }
    var isFullViewMode by remember { mutableStateOf(false) }

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
                        context = context,
                        onNavigateToModelCreator = onNavigateToModelCreator
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        } else {
            // Mobile-First Ergonomic Layout with Steps Ribbon & Sticky Generate Button
            Box(modifier = Modifier.fillMaxSize()) {
                val scrollState = rememberScrollState()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .padding(bottom = 80.dp), // Padding for sticky bottom generate bar
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IntroBanner()

                    // Mobile Step Navigation Ribbon
                    MobileStepRibbon(
                        currentStep = currentStep,
                        isFullViewMode = isFullViewMode,
                        onStepSelected = {
                            currentStep = it
                            isFullViewMode = false
                        },
                        onToggleFullView = { isFullViewMode = !isFullViewMode }
                    )

                    if (isFullViewMode) {
                        // Full view showing all sections
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
                    } else {
                        // Single Step View with smooth horizontal animation
                        AnimatedContent(
                            targetState = currentStep,
                            transitionSpec = {
                                val enter = slideInHorizontally(
                                    initialOffsetX = { fullWidth -> if (targetState > initialState) fullWidth / 2 else -fullWidth / 2 },
                                    animationSpec = tween(240)
                                ) + fadeIn(animationSpec = tween(240))
                                val exit = slideOutHorizontally(
                                    targetOffsetX = { fullWidth -> if (targetState > initialState) -fullWidth / 2 else fullWidth / 2 },
                                    animationSpec = tween(240)
                                ) + fadeOut(animationSpec = tween(240))
                                enter togetherWith exit
                            },
                            label = "StepContentTransition"
                        ) { step ->
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                when (step) {
                                    0 -> {
                                        ScriptCard(config, viewModel, context)
                                        StepAdvanceButton(
                                            nextStepTitle = "চরিত্র ও স্টাইল (Actor & Style)",
                                            onClick = { currentStep = 1 }
                                        )
                                    }
                                    1 -> {
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
                                        StepAdvanceButton(
                                            nextStepTitle = "ক্যামেরা ও ভিজ্যুয়াল কাটআউটস",
                                            onClick = { currentStep = 2 }
                                        )
                                    }
                                    2 -> {
                                        VisualSettingsCard(config, viewModel)
                                        StepAdvanceButton(
                                            nextStepTitle = "প্রম্পট রুলস ও অপশনস",
                                            onClick = { currentStep = 3 }
                                        )
                                    }
                                    3 -> {
                                        GenerationOptionsCard(config, viewModel)
                                        CustomInstructionsCard(config, viewModel)
                                    }
                                }
                            }
                        }
                    }

                    // Result display card (appears automatically when generating or prompt exists)
                    if (uiState.generatedPrompt != null || uiState.isGenerating || uiState.errorMessage != null) {
                        GeneratedPromptCard(
                            uiState = uiState,
                            viewModel = viewModel,
                            context = context,
                            onNavigateToModelCreator = onNavigateToModelCreator
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Sticky Bottom Action Bar (Easy-to-reach for mobile one-hand thumb operation)
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    shadowElevation = 12.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        GenerateButton(uiState, onGenerate = { viewModel.generateMasterPrompt() })
                    }
                }
            }
        }
    }
}

@Composable
fun MobileStepRibbon(
    currentStep: Int,
    isFullViewMode: Boolean,
    onStepSelected: (Int) -> Unit,
    onToggleFullView: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(NeonCyan.copy(alpha = 0.3f), NeonPurple.copy(alpha = 0.3f))
            )
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val steps = listOf(
                "✍️ স্ক্রিপ্ট",
                "👤 চরিত্র ও স্টাইল",
                "🎬 ক্যামেরা ও সিন",
                "⚙️ রুলস"
            )

            steps.forEachIndexed { index, title ->
                val isSelected = !isFullViewMode && currentStep == index
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) NeonCyan.copy(alpha = 0.22f) else Color.Transparent,
                    border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(NeonCyan, ElectricIndigo))
                    ) else null,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onStepSelected(index) }
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                    )
                }
            }

            // All-in-one Toggle
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isFullViewMode) NeonPurple.copy(alpha = 0.25f) else Color.Transparent,
                border = if (isFullViewMode) CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(NeonPurple, ElectricIndigo))
                ) else null,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onToggleFullView() }
            ) {
                Text(
                    text = "📑 সব একসাথে",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isFullViewMode) FontWeight.Bold else FontWeight.Medium,
                    color = if (isFullViewMode) NeonPurple else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                )
            }
        }
    }
}

@Composable
fun StepAdvanceButton(
    nextStepTitle: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(NeonCyan.copy(alpha = 0.5f), NeonPurple.copy(alpha = 0.5f)))
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "পরবর্তী: $nextStepTitle",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun IntroBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(NeonCyan.copy(alpha = 0.4f), NeonPurple.copy(alpha = 0.3f))
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "AI Video Master Studio",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                AppBadge(text = "Qwen 3.8 Omni")
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "স্ক্রিপ্ট ও চরিত্র দিন—Google Flow, Sora, Kling ও Runway-এর জন্য নিখুঁত মাস্টার প্রম্পট তৈরি করুন।",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 17.sp
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
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(NeonCyan.copy(alpha = 0.4f), ElectricIndigo.copy(alpha = 0.2f)))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            SectionHeader(
                title = "১. ডায়লগ / ভয়েস স্ক্রিপ্ট",
                subtitle = "বাংলা, ইংরেজি বা বাংলিশ স্ক্রিপ্ট হুবহু সংরক্ষিত থাকবে",
                icon = Icons.Default.Movie
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Samples Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "স্যাম্পল স্ক্রিপ্ট:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NeonCyan.copy(alpha = 0.12f),
                    modifier = Modifier.clickable {
                        viewModel.updateScript("আমাদের নতুন স্মার্টফোন কালেকশন এখন লাইভ! সেরা ক্যামেরা এবং প্রিমিয়াম ফিনিশিং নিয়ে চলে এলো ভবিষ্যতের অভিজ্ঞতা। আজই অর্ডার করুন বিশেষ ছাড়ে।")
                    }
                ) {
                    Text(
                        text = "📱 প্রোডাক্ট লঞ্চ",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NeonPurple.copy(alpha = 0.12f),
                    modifier = Modifier.clickable {
                        viewModel.updateScript("আজকে আমরা এমন এক নতুন এআই টুলের রিভিউ করব যা আপনার কন্টেন্ট তৈরির সম্পূর্ণ কাজ বদলে দেবে। নিখুঁত ভিডিও আর ভয়েস সিনক্রোনাইজেশন এক ক্লিপেই সম্ভব।")
                    }
                ) {
                    Text(
                        text = "🚀 এআই রিভিউ",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonPurple,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EmeraldGreen.copy(alpha = 0.12f),
                    modifier = Modifier.clickable {
                        viewModel.updateScript("সফলতা কোনো কাকতালীয় ঘটনা নয়; এটি প্রতিদিনের পরিশ্রম ও ত্যাগের ফল। নিজের স্বপ্নের ওপর বিশ্বাস রাখুন, জয় আপনার হবেই।")
                    }
                ) {
                    Text(
                        text = "💡 মোটিভেশনাল",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldGreen,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = config.script,
                onValueChange = { viewModel.updateScript(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .testTag("dialogue_script_input"),
                placeholder = {
                    Text(
                        text = "আপনার ডায়লগ স্ক্রিপ্ট লিখুন বা পেস্ট করুন...\n(যেমন: আমাদের ডিজিটাল সেবা এখন আপনার হাতের মুঠোয়...)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

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
                                Toast.makeText(context, "স্ক্রিপ্ট পেস্ট হয়েছে!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "ক্লিপবোর্ড খালি", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("paste_script_button")
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = "Paste", modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("পেস্ট", style = MaterialTheme.typography.labelMedium)
                    }

                    if (config.script.isNotEmpty()) {
                        OutlinedButton(
                            onClick = { viewModel.updateScript("") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("clear_script_button")
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("মুছুন", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                val wordCount = config.script.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
                Text(
                    text = "${config.script.length} অক্ষর • $wordCount শব্দ",
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
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = if (config.isModelLocked)
                Brush.linearGradient(listOf(NeonGreen, NeonCyan))
            else
                Brush.linearGradient(listOf(NeonPurple.copy(alpha = 0.4f), MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            SectionHeader(
                title = "২. রেফারেন্স চরিত্র (Character / Actor)",
                subtitle = "চেহারা ও পোশাকের ১০০% ধারাবাহিকতা বজায় থাকবে",
                icon = Icons.Default.Person
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Locked Model Active Banner
            if (config.isModelLocked && config.lockedModelName != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = NeonGreen.copy(alpha = 0.12f),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(NeonGreen, NeonCyan))
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(NeonGreen.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = NeonGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "লক মডেল সক্রিয়: ${config.lockedModelName}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonGreen
                                )
                                Text(
                                    text = "এই ক্যারেক্টারের চেহারা ও শারীরিক মাপকাঠি লক করা হয়েছে",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(onClick = { viewModel.clearLockedModel() }) {
                            Icon(Icons.Default.Close, contentDescription = "Unlock", tint = Color(0xFFF43F5E))
                        }
                    }
                }
            }

            // Image attachment preview or upload box
            if (config.referenceImageUri != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, NeonCyan.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                ) {
                    AsyncImage(
                        model = config.referenceImageUri,
                        contentDescription = "Reference Character Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { viewModel.setReferenceImageUri(null) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                            .size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = config.referenceImageDescription,
                    onValueChange = { viewModel.setReferenceImageDescription(it) },
                    label = { Text("চরিত্রের পোশাক বা বৈশিষ্ট্য নোট (ঐচ্ছিক)") },
                    placeholder = { Text("যেমন: ব্লু ফরমাল স্যুট, চশমা পরা") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onPickImage,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ছবি যুক্ত করুন", style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedButton(
                        onClick = onNavigateToModelCreator,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("মডেল স্টুডিও", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Presenter Type Chips
            Text(
                text = "উপস্থাপক ধরন (Presenter Type):",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            FilterChipGroup(
                options = listOf("Female", "Male", "No presenter", "Custom"),
                selectedOption = config.presenter,
                onOptionSelected = { viewModel.updatePresenter(it) }
            )
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
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(ElectricIndigo.copy(alpha = 0.4f), NeonPurple.copy(alpha = 0.3f)))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SectionHeader(
                title = "৩. ফরম্যাট ও স্টাইল (Format & Style)",
                subtitle = "অ্যাসপেক্ট রেশিও, রেজোলিউশন ও সিনেমাটিক লুক",
                icon = Icons.Default.Videocam
            )

            Text("অ্যাসপেক্ট রেশিও (Aspect Ratio):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FilterChipGroup(
                options = listOf("9:16 (Shorts/Reels)", "16:9 (YouTube/Landscape)", "1:1 (Square)", "4:5 (Instagram Feed)"),
                selectedOption = config.aspectRatio,
                onOptionSelected = { viewModel.updateAspectRatio(it) }
            )

            Text("ভিডিও স্টাইল (Video Style):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FilterChipGroup(
                options = listOf("Cinematic", "Photorealistic", "Studio Commercial", "Anime", "Vintage 90s", "Cyberpunk", "Custom"),
                selectedOption = config.videoStyle,
                onOptionSelected = { viewModel.updateVideoStyle(it) }
            )

            if (config.videoStyle == "Custom") {
                OutlinedTextField(
                    value = config.customStyle,
                    onValueChange = { viewModel.updateCustomStyle(it) },
                    label = { Text("কাস্টম স্টাইলের বিবরণ লিখুন") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Text("রেজোলিউশন ও সময়কাল:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    FilterChipGroup(
                        options = listOf("4K UHD", "1080p FHD"),
                        selectedOption = config.resolution,
                        onOptionSelected = { viewModel.updateResolution(it) }
                    )
                }
                Box(modifier = Modifier.weight(1.2f)) {
                    FilterChipGroup(
                        options = listOf("8 seconds", "15 seconds", "30 seconds"),
                        selectedOption = config.duration,
                        onOptionSelected = { viewModel.updateDuration(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun VisualSettingsCard(
    config: PromptConfig,
    viewModel: PromptFlowViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(NeonCyan.copy(alpha = 0.4f), NeonPurple.copy(alpha = 0.3f)))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SectionHeader(
                title = "৪. ক্যামেরা ও ভিজ্যুয়াল কাটআউটস (Camera & Cutaways)",
                subtitle = "লোকেশন, ক্যামেরার কোণ ও শট-বাই-শট B-Roll দৃশ্য",
                icon = Icons.Default.Tune
            )

            Text("লোকেশন / ব্যাকগ্রাউন্ড:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FilterChipGroup(
                options = listOf("Modern Studio", "Tech Office", "Cozy Cafe", "Outdoor Park", "City Rooftop", "Custom"),
                selectedOption = config.location,
                onOptionSelected = { viewModel.updateLocation(it) }
            )

            if (config.location == "Custom") {
                OutlinedTextField(
                    value = config.customLocation,
                    onValueChange = { viewModel.updateCustomLocation(it) },
                    label = { Text("কাস্টম লোকেশনের নাম") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Text("ক্যামেরা অ্যাঙ্গেল ও ফ্রেমিং:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FilterChipGroup(
                options = listOf("Eye Level", "Close Up", "Medium Shot", "Over The Shoulder", "Cinematic Tracking", "Custom"),
                selectedOption = config.camera,
                onOptionSelected = { viewModel.updateCamera(it) }
            )

            // B-Roll Cutaways (ভিজ্যুয়াল কাটআউটস) Section
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(NeonCyan.copy(alpha = 0.4f), NeonPurple.copy(alpha = 0.3f)))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Movie, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "B-Roll Cutaways (ভিজ্যুয়াল কাটআউটস):",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    }
                    Text(
                        text = "🌟 ভিজ্যুয়াল কাটআউটস নির্বাচন করলে ডায়লগের প্রতি লাইনের প্রাসঙ্গিক দৃশ্য (যেমন ৫০মিমি ম্যাক্রো, ওয়াইড সিন) শট-বাই-শট প্রম্পটে যুক্ত হয়।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FilterChipGroup(
                        options = listOf("Yes (কাটআউটস সহ)", "Auto (সিন অনুযায়ী)", "No (শুধুমাত্র স্পিকার)"),
                        selectedOption = if (config.bRoll.startsWith("Yes", ignoreCase = true)) "Yes (কাটআউটস সহ)"
                                         else if (config.bRoll.startsWith("No", ignoreCase = true)) "No (শুধুমাত্র স্পিকার)"
                                         else "Auto (সিন অনুযায়ী)",
                        onOptionSelected = {
                            val cleanVal = if (it.startsWith("Yes")) "Yes" else if (it.startsWith("No")) "No" else "Auto"
                            viewModel.updateBRoll(cleanVal)
                        }
                    )
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(ElectricIndigo.copy(alpha = 0.3f), NeonCyan.copy(alpha = 0.3f)))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionHeader(
                title = "৫. প্রো প্রম্পট রুলস (Rules & Mandates)",
                subtitle = "নিখুঁত এআই ভিডিওর কঠোর নির্দেশিকা",
                icon = Icons.Default.Tune
            )

            OptionCheckboxItem(
                title = "মূল ডায়লগ ১০০% অবিকৃত রাখা",
                description = "কোনো শব্দ পরিবর্তন, অনুবাদ বা কাটাছেঁড়া হবে না",
                checked = config.preserveExactDialogue,
                onCheckedChange = { viewModel.toggleOption("preserveExactDialogue", it) }
            )
            OptionCheckboxItem(
                title = "ক্যারেক্টার ধারাবাহিকতা ও ফেস লক",
                description = "প্রতি ফ্রেমে একই মুখমণ্ডল, ত্বক ও পোশাক অপরিবর্তিত রাখা",
                checked = config.maintainCharacterConsistency,
                onCheckedChange = { viewModel.toggleOption("maintainCharacterConsistency", it) }
            )
            OptionCheckboxItem(
                title = "একক টেক ভয়েস ও কোনো ইকো নয়",
                description = "এক টেকে ক্লিয়ার ভয়েস, কোনো ডায়লগ পুনরাবৃত্তি বা ইকো হবে না",
                checked = config.oneContinuousVoiceTake,
                onCheckedChange = { viewModel.toggleOption("oneContinuousVoiceTake", it) }
            )
            OptionCheckboxItem(
                title = "ন্যাচারাল লিপ-সিঙ্ক (Lip-Sync)",
                description = "ডায়লগের উচ্চারণের সাথে নিখুঁত ঠোঁট মেলানো",
                checked = config.naturalLipSync,
                onCheckedChange = { viewModel.toggleOption("naturalLipSync", it) }
            )
            OptionCheckboxItem(
                title = "সিনেমাটিক আলো ও ভলিউমেট্রিক লাইটিং",
                description = "সফট কি-লাইট, রিম শ্যাডো ও হাই-এন্ড স্টুডিও আলো",
                checked = config.cinematicLighting,
                onCheckedChange = { viewModel.toggleOption("cinematicLighting", it) }
            )
            OptionCheckboxItem(
                title = "নেগেটিভ প্রম্পট ফিল্টারিং",
                description = "বিকৃত হাত, অপ্রাসঙ্গিক কাট বা অস্পষ্টতা কঠোরভাবে নিষিদ্ধ",
                checked = config.negativePrompt,
                onCheckedChange = { viewModel.toggleOption("negativePrompt", it) }
            )
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
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            SectionHeader(
                title = "অতিরিক্ত নির্দেশাবলী (ঐচ্ছিক)",
                subtitle = "নির্দিষ্ট কোনো ব্যাকগ্রাউন্ড বা ক্যামেরা মুভমেন্টের অনুরোধ"
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = config.customInstructions,
                onValueChange = { viewModel.updateCustomInstructions(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(85.dp),
                placeholder = {
                    Text(
                        "যেমন: ব্যাকগ্রাউন্ডে সূর্যাস্তের নরম আলো থাকবে, ভিডিওতে কোনো অন-স্ক্রিন টেক্সট থাকবে না...",
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
            .height(52.dp)
            .testTag("generate_master_prompt_button"),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(NeonCyan, ElectricIndigo, NeonPurple)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isGenerating) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "মাস্টার প্রম্পট তৈরি হচ্ছে...",
                        style = MaterialTheme.typography.titleSmall,
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
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "✨ মাস্টার প্রম্পট তৈরি করুন",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 15.sp
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
    context: Context,
    onNavigateToModelCreator: () -> Unit = {}
) {
    var isCopiedAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(isCopiedAnimation) {
        if (isCopiedAnimation) {
            delay(2000)
            isCopiedAnimation = false
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("generated_prompt_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(NeonCyan, NeonPurple, ElectricIndigo)
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
                    title = "✨ মাস্টার প্রম্পট প্রস্তুত!",
                    subtitle = "Google Flow, Sora, Kling ও Runway-তে ব্যবহারের জন্য তৈরি",
                    icon = Icons.Default.AutoAwesome
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Error or info banners
            if (uiState.errorMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
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
                    shape = RoundedCornerShape(10.dp),
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
                    subtitleText = "XKIRO Qwen 3.8 Omni & অফলাইন ইঞ্জিন দ্বারা সিনক্রোনাইজেশন হচ্ছে",
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
                    shape = RoundedCornerShape(12.dp)
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
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("পরিবর্তন সংরক্ষণ করুন")
                    }

                    OutlinedButton(
                        onClick = { viewModel.cancelEditMode() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("cancel_edit_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("বাতিল")
                    }
                }
            } else {
                // View Mode
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(
                            1.dp,
                            NeonCyan.copy(alpha = 0.35f),
                            RoundedCornerShape(14.dp)
                        )
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    SelectionContainer {
                        Text(
                            text = prompt.ifBlank { "প্রম্পট তৈরি হলে এখানে প্রদর্শিত হবে।" },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 19.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Actions: Copy, Regenerate, Edit, Download, Share, Save to Model Creator
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Copy Prompt with Animated Feedback
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Master Prompt", prompt)
                            clipboard.setPrimaryClip(clip)
                            isCopiedAnimation = true
                            Toast.makeText(context, "মাস্টার প্রম্পট কপি হয়েছে!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = if (isCopiedAnimation) ButtonDefaults.buttonColors(containerColor = EmeraldGreen) else ButtonDefaults.buttonColors(),
                        modifier = Modifier.testTag("copy_prompt_button")
                    ) {
                        Icon(
                            imageVector = if (isCopiedAnimation) Icons.Default.CheckCircle else Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isCopiedAnimation) "কপিকৃত! ✓" else "কপি করুন")
                    }

                    // Regenerate
                    OutlinedButton(
                        onClick = { viewModel.regeneratePrompt() },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("regenerate_prompt_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("পুনরায় তৈরি")
                    }

                    // Edit
                    OutlinedButton(
                        onClick = { viewModel.startEditMode() },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("edit_prompt_button")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("এডিট")
                    }

                    // Download / Export
                    OutlinedButton(
                        onClick = {
                            saveOrSharePrompt(context, prompt)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("download_prompt_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("শেয়ার / সেভ")
                    }

                    // Clear
                    OutlinedButton(
                        onClick = { viewModel.clearPrompt() },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("clear_prompt_result_button")
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("মুছুন")
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
        Toast.makeText(context, "এক্সপোর্ট উইন্ডো খোলা হচ্ছে...", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to export: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
