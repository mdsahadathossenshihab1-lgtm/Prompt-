package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.firebase.FirestoreAiModel
import com.example.ui.PromptFlowViewModel
import com.example.ui.components.AiPulseLoader
import com.example.ui.components.AppBadge
import com.example.ui.components.FilterChipGroup
import com.example.ui.components.SectionHeader
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelCreatorScreen(
    viewModel: PromptFlowViewModel,
    onNavigateToVideoGenerator: () -> Unit,
    onNavigateToAgentMode: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val modelConfig by viewModel.modelConfig.collectAsState()
    val modelUiState by viewModel.modelUiState.collectAsState()
    val savedModels by viewModel.savedModels.collectAsState()
    val context = LocalContext.current

    var selectedSubTab by remember { mutableIntStateOf(0) } // 0: Prompt Creator, 1: Library
    var voiceScriptInput by remember { mutableStateOf(modelConfig.description) }
    var selectedGender by remember { mutableStateOf(if (modelConfig.gender.isNotBlank()) modelConfig.gender else "Male") }

    var selectedAge by remember { mutableStateOf("তরুণ প্রফেশনাল (২৫-৩০)") }
    var selectedEthnicity by remember { mutableStateOf("বাঙালি / সাউথ এশিয়ান (Bangladeshi)") }
    var selectedFraming by remember { mutableStateOf("৮৫মিমি স্টুডিও ক্লোজ-আপ") }

    var showSaveDialog by remember { mutableStateOf(false) }
    var saveModelName by remember { mutableStateOf("") }
    var isCopiedAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(isCopiedAnimation) {
        if (isCopiedAnimation) {
            delay(2000)
            isCopiedAnimation = false
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Header Banner
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
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
                                text = "AI CHARACTER STUDIO",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            AppBadge(
                                text = "PROMPT ONLY • NO IMAGE",
                                backgroundColor = NeonPurple.copy(alpha = 0.2f),
                                textColor = NeonPurple
                            )
                        }
                        Text(
                            text = "ভয়েস স্ক্রিপ্ট ও জেন্ডার অনুযায়ী Midjourney, Flux ও Sora-র ক্যারেক্টার প্রম্পট তৈরি করুন",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sub-tabs: Creator & Library
                TabRow(
                    selectedTabIndex = selectedSubTab,
                    containerColor = Color.Transparent,
                    contentColor = NeonCyan,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]),
                            color = NeonCyan,
                            height = 3.dp
                        )
                    }
                ) {
                    Tab(
                        selected = selectedSubTab == 0,
                        onClick = { selectedSubTab = 0 },
                        text = {
                            Text(
                                "✨ ক্যারেক্টার প্রম্পট তৈরি",
                                fontWeight = if (selectedSubTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedSubTab == 0) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                    Tab(
                        selected = selectedSubTab == 1,
                        onClick = { selectedSubTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "📁 সংরক্ষিত ক্যারেক্টার",
                                    fontWeight = if (selectedSubTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedSubTab == 1) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (savedModels.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    AppBadge(text = "${savedModels.size}")
                                }
                            }
                        }
                    )
                }
            }
        }

        // SubTab 0: Character Prompt Creator (No Image - Pure Prompt Generation)
        if (selectedSubTab == 0) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Card 1: Full Voice Script
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(NeonCyan.copy(alpha = 0.5f), ElectricIndigo.copy(alpha = 0.3f)))
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        SectionHeader(
                            title = "১. ভিডিওর ভয়েস স্ক্রিপ্ট (Voice Script)",
                            subtitle = "স্ক্রিপ্টের বিষয়বস্তু অনুযায়ী পোশাক, বয়স ও এক্সপ্রেশন নির্ধারিত হবে",
                            icon = Icons.Default.Movie
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick Sample Script Buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "স্যাম্পল:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = NeonCyan.copy(alpha = 0.12f),
                                modifier = Modifier.clickable {
                                    voiceScriptInput = "আমাদের নতুন স্মার্টফোন কালেকশন এখন লাইভ! সেরা ক্যামেরা এবং প্রিমিয়াম ফিনিশিং নিয়ে চলে এলো ভবিষ্যতের অভিজ্ঞতা। আজই অর্ডার করুন বিশেষ ছাড়ে।"
                                }
                            ) {
                                Text(
                                    text = "📱 প্রোডাক্ট রিভিউ",
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
                                    voiceScriptInput = "আজকের প্রধান সংবাদ: প্রযুক্তি ও কৃত্রিম বুদ্ধিমত্তার ব্যবহারের মাধ্যমে দেশের অর্থনীতিতে তৈরি হচ্ছে নতুন দিগন্ত। তরুণ প্রজন্ম গড়ে তুলছে ভবিষ্যতের সমাধান।"
                                }
                            ) {
                                Text(
                                    text = "🎙️ নিউজ উপস্থাপক",
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
                                    voiceScriptInput = "সফলতা কোনো কাকতালীয় ঘটনা নয়; এটি প্রতিদিনের পরিশ্রম ও ত্যাগের ফল। নিজের স্বপ্নের ওপর বিশ্বাস রাখুন, জয় আপনার হবেই।"
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
                            value = voiceScriptInput,
                            onValueChange = { voiceScriptInput = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .testTag("model_voice_script_input"),
                            placeholder = {
                                Text(
                                    "এখানে ভিডিওর সম্পূর্ণ ডায়লগ বা ভয়েস স্ক্রিপ্ট লিখুন বা পেস্ট করুন...",
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
                                            voiceScriptInput = clip.getItemAt(0).coerceToText(context).toString()
                                            Toast.makeText(context, "স্ক্রিপ্ট পেস্ট করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("পেস্ট", style = MaterialTheme.typography.labelMedium)
                                }

                                if (voiceScriptInput.isNotEmpty()) {
                                    OutlinedButton(
                                        onClick = { voiceScriptInput = "" },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("মুছুন", style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }

                            Text(
                                text = "${voiceScriptInput.length} অক্ষর",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Card 2: Gender Selection (Male vs Female)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(NeonPurple.copy(alpha = 0.5f), NeonCyan.copy(alpha = 0.3f)))
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        SectionHeader(
                            title = "২. উপস্থাপকের জেন্ডার / লিঙ্গ নির্বাচন",
                            subtitle = "পুরুষ নাকি নারী ক্যারেক্টার তৈরি করতে চান?",
                            icon = Icons.Default.Person
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Male Card
                            val isMale = selectedGender == "Male"
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isMale) NeonCyan.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = if (isMale) CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.linearGradient(listOf(NeonCyan, ElectricIndigo))
                                ) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { selectedGender = "Male" }
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("👨", fontSize = 28.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Male (পুরুষ)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = if (isMale) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isMale) NeonCyan else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "পুরুষ উপস্থাপক লুক",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            // Female Card
                            val isFemale = selectedGender == "Female"
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isFemale) NeonPurple.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = if (isFemale) CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.linearGradient(listOf(NeonPurple, ElectricIndigo))
                                ) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { selectedGender = "Female" }
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("👩", fontSize = 28.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Female (নারী)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = if (isFemale) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isFemale) NeonPurple else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "নারী উপস্থাপক লুক",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Card 3: Character Demographics & Framing (Optional fine-tuning)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SectionHeader(
                            title = "৩. ক্যারেক্টার লুক ও স্টাইল (ঐচ্ছিক)",
                            subtitle = "বয়স, ঐতিহ্য ও ক্যামেরা পোর্ট্রেট ফ্রেমিং",
                            icon = Icons.Default.Tune
                        )

                        Text("বয়স ও ব্যক্তিত্ব:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        FilterChipGroup(
                            options = listOf("তরুণ প্রফেশনাল (২৫-৩০)", "কর্পোরেট এক্সিকিউটিভ (৩৫-৪০)", "সিনিয়র এক্সপার্ট (৪৫+)"),
                            selectedOption = selectedAge,
                            onOptionSelected = { selectedAge = it }
                        )

                        Text("অঞ্চল ও ঐতিহ্য (Ethnicity):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        FilterChipGroup(
                            options = listOf("বাঙালি / সাউথ এশিয়ান (Bangladeshi)", "গ্লোবাল / ইন্টারন্যাশনাল", "মিডল ইস্টার্ন"),
                            selectedOption = selectedEthnicity,
                            onOptionSelected = { selectedEthnicity = it }
                        )

                        Text("ক্যামেরা পোর্ট্রেট ফ্রেমিং:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        FilterChipGroup(
                            options = listOf("৮৫মিমি স্টুডিও ক্লোজ-আপ", "৫০মিমি মিডিয়াম শট", "ওয়াইড প্রেজেন্টার শট"),
                            selectedOption = selectedFraming,
                            onOptionSelected = { selectedFraming = it }
                        )
                    }
                }

                // Primary Action Button: Generate Character Master Prompt
                Button(
                    onClick = {
                        viewModel.generateCharacterPromptFromScript(
                            voiceScript = voiceScriptInput,
                            gender = selectedGender
                        )
                    },
                    enabled = !modelUiState.isGenerating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("generate_character_prompt_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(listOf(NeonPurple, ElectricIndigo, NeonCyan)),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (modelUiState.isGenerating) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("ক্যারেক্টার প্রম্পট তৈরি হচ্ছে...", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "✨ ক্যারেক্টার মাস্টার প্রম্পট তৈরি করুন",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }

                // Error Banner Display (Displays AI service errors clearly)
                modelUiState.errorMessage?.let { errorMsg ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("character_prompt_error_banner"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(listOf(MaterialTheme.colorScheme.error, Color(0xFFF43F5E)))
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "AI প্রম্পট তৈরিতে সমস্যা হয়েছে",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = errorMsg,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.generateCharacterPromptFromScript(
                                                voiceScript = voiceScriptInput,
                                                gender = selectedGender
                                            )
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("পুনরায় চেষ্টা করুন", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }

                // Info Banner
                if (modelUiState.infoMessage != null && modelUiState.errorMessage == null && !modelUiState.isGenerating && modelUiState.generatedResult == null) {
                    Surface(
                        color = NeonCyan.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(listOf(NeonCyan.copy(alpha = 0.4f), ElectricIndigo.copy(alpha = 0.2f)))
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = modelUiState.infoMessage ?: "",
                            color = NeonCyan,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Output Result Card: Character Master Prompt (No picture generated!)
                val generatedResult = modelUiState.generatedResult
                val resultPrompt = generatedResult?.detailedPrompt ?: ""

                if (modelUiState.isGenerating) {
                    AiPulseLoader(
                        statusText = "AI ক্যারেক্টার প্রম্পট তৈরি করছে...",
                        subtitleText = "ভয়েস স্ক্রিপ্ট অনুযায়ী চেহারা, পোশাক ও লাইটিং সিনক্রোনাইজেশন হচ্ছে",
                        size = 85.dp
                    )
                }

                if (resultPrompt.isNotBlank()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("character_prompt_result_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(listOf(NeonCyan, NeonPurple))
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SectionHeader(
                                    title = "✨ মাস্টার ক্যারেক্টার প্রম্পট প্রস্তুত!",
                                    subtitle = "${generatedResult?.suggestedName ?: "$selectedGender Presenter"} • Midjourney & Flux রেডি",
                                    icon = Icons.Default.AutoAwesome
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Selectable Monospace Character Prompt Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                SelectionContainer {
                                    Text(
                                        text = resultPrompt,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            lineHeight = 18.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Action Toolbar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 1-Tap Copy Button with animated feedback
                                Button(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Character Prompt", resultPrompt))
                                        isCopiedAnimation = true
                                        Toast.makeText(context, "ক্যারেক্টার প্রম্পট কপি হয়েছে!", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = if (isCopiedAnimation) ButtonDefaults.buttonColors(containerColor = EmeraldGreen) else ButtonDefaults.buttonColors()
                                ) {
                                    Icon(
                                        imageVector = if (isCopiedAnimation) Icons.Default.CheckCircle else Icons.Default.ContentCopy,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isCopiedAnimation) "কপিকৃত! ✓" else "কপি করুন")
                                }

                                // 🤖 Refine in Agent Mode Button
                                Button(
                                    onClick = {
                                        viewModel.loadPromptIntoAgent(resultPrompt, "মডেল ক্যারেক্টার প্রম্পট")
                                        Toast.makeText(context, "প্রম্পটটি এজেন্ট মোডে নেওয়া হয়েছে!", Toast.LENGTH_SHORT).show()
                                        onNavigateToAgentMode()
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("🤖 এজেন্ট মোডে ঠিক করুন", color = Color.Black, fontWeight = FontWeight.Bold)
                                }

                                // Attach & Lock to Video Prompts (Module 1)
                                Button(
                                    onClick = {
                                        viewModel.useThisModelInPromptGenerator(
                                            imageUrl = null,
                                            modelName = generatedResult?.suggestedName ?: "$selectedGender Presenter",
                                            promptDesc = resultPrompt
                                        )
                                        Toast.makeText(context, "ক্যারেক্টার ভিডিও প্রম্পটে লক করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                        onNavigateToVideoGenerator()
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ভিডিওতে লক করুন", color = Color.White)
                                }

                                // Save to Library
                                OutlinedButton(
                                    onClick = {
                                        saveModelName = generatedResult?.suggestedName ?: "$selectedGender Model"
                                        showSaveDialog = true
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("লাইব্রেরিতে সেভ")
                                }

                                // Share
                                OutlinedButton(
                                    onClick = {
                                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, "PromptFlow AI - Character Master Prompt")
                                            putExtra(Intent.EXTRA_TEXT, resultPrompt)
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Share Character Prompt"))
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("শেয়ার")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        } else {
            // SubTab 1: Saved Character Library
            if (savedModels.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "কোনো সংরক্ষিত ক্যারেক্টার নেই",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "প্রথম ট্যাবে গিয়ে আপনার ভয়েস স্ক্রিপ্ট দিয়ে নতুন ক্যারেক্টার তৈরি ও সেভ করুন।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(savedModels, key = { it.id }) { model ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
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
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(NeonPurple.copy(alpha = 0.18f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(if (model.gender == "Female") "👩" else "👨", fontSize = 18.sp)
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = model.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${model.gender} • Seed #${model.seed}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    IconButton(onClick = { viewModel.deleteSavedModel(model) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Prompt preview
                                Text(
                                    text = model.promptText.take(160) + "...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Character Prompt", model.promptText))
                                            Toast.makeText(context, "প্রম্পট কপি হয়েছে!", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("কপি")
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.useThisModelInPromptGenerator(
                                                imageUrl = null,
                                                modelName = model.name,
                                                promptDesc = model.promptText
                                            )
                                            Toast.makeText(context, "${model.name} ভিডিও প্রম্পটে লক করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                            onNavigateToVideoGenerator()
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                                        modifier = Modifier.weight(1.3f)
                                    ) {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("ভিডিওতে লক করুন", color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Save Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("ক্যারেক্টার সংরক্ষণ করুন") },
            text = {
                OutlinedTextField(
                    value = saveModelName,
                    onValueChange = { saveModelName = it },
                    label = { Text("ক্যারেক্টারের নাম লিখুন") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveCurrentModelToLibrary(saveModelName)
                        showSaveDialog = false
                        Toast.makeText(context, "ক্যারেক্টার লাইব্রেরিতে সংরক্ষিত হয়েছে!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("সংরক্ষণ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}
