package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.model.AgentChatMessage
import com.example.model.AgentSender
import com.example.ui.PromptFlowViewModel
import com.example.ui.components.AiPulseLoader
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentModeScreen(
    viewModel: PromptFlowViewModel,
    onNavigateToVideoGenerator: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val agentState by viewModel.agentUiState.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var textInput by remember { mutableStateOf("") }
    var isTopPromptCopied by remember { mutableStateOf(false) }
    var showEditPromptDialog by remember { mutableStateOf(false) }
    var tempPromptInput by remember { mutableStateOf("") }

    LaunchedEffect(isTopPromptCopied) {
        if (isTopPromptCopied) {
            delay(2000)
            isTopPromptCopied = false
        }
    }

    // Auto-scroll to latest chat message when new messages arrive
    LaunchedEffect(agentState.messages.size, agentState.isAgentThinking) {
        if (agentState.messages.isNotEmpty()) {
            delay(100)
            listState.animateScrollToItem(agentState.messages.size - 1)
        }
    }

    // Pre-load prompt from generator if Agent state is currently empty
    LaunchedEffect(Unit) {
        if (agentState.currentPrompt.isBlank()) {
            val genPrompt = viewModel.uiState.value.generatedPrompt
            val modelPrompt = viewModel.modelUiState.value.generatedResult?.detailedPrompt
            val candidate = when {
                !genPrompt.isNullOrBlank() -> genPrompt
                !modelPrompt.isNullOrBlank() -> modelPrompt
                else -> null
            }
            if (candidate != null) {
                viewModel.loadPromptIntoAgent(candidate, "পূর্ববর্তী তৈরি করা প্রম্পট")
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .testTag("agent_mode_screen")
    ) {
        // Top Sticky Header & Controls
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(listOf(NeonCyan, NeonPurple))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "এজেন্ট মোড (Agent)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = NeonGreen.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "LIVE REFINER",
                                        color = NeonGreen,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "চ্যাট করে ক্যামেরা, ভিজ্যুয়াল ও লাইটিং ঠিক করুন",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Action buttons in Top Header
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Clear chat button
                        IconButton(
                            onClick = { viewModel.clearAgentChat() },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "নতুন চ্যাট",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Send back to video generator
                        Button(
                            onClick = {
                                viewModel.useAgentPromptInVideoGenerator()
                                Toast.makeText(context, "প্রম্পট ভিডিও জেনারেটরে পাঠানো হয়েছে!", Toast.LENGTH_SHORT).show()
                                onNavigateToVideoGenerator()
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                        ) {
                            Icon(Icons.Default.Movie, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ভিডিওতে নিন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Active Master Prompt Card (Collapsible)
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(NeonCyan.copy(alpha = 0.5f), ElectricIndigo.copy(alpha = 0.3f)))
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                        .testTag("agent_active_prompt_card")
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = NeonCyan.copy(alpha = 0.18f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "ভার্সন v${agentState.promptVersion}",
                                        color = NeonCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "সক্রিয় মাস্টার প্রম্পট",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Undo / Revert if history available
                                if (agentState.promptHistory.size > 1) {
                                    IconButton(
                                        onClick = { viewModel.revertToPreviousPrompt() },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Undo,
                                            contentDescription = "আগের ভার্সন",
                                            tint = ElectricIndigo,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(2.dp))
                                }

                                // 1-tap Copy
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Agent Master Prompt", agentState.currentPrompt))
                                        isTopPromptCopied = true
                                        Toast.makeText(context, "প্রম্পট কপি হয়েছে!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isTopPromptCopied) Icons.Default.CheckCircle else Icons.Default.ContentCopy,
                                        contentDescription = "কপি করুন",
                                        tint = if (isTopPromptCopied) EmeraldGreen else NeonCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // Edit manually
                                IconButton(
                                    onClick = {
                                        tempPromptInput = agentState.currentPrompt
                                        showEditPromptDialog = true
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "ম্যানুয়ালি এডিট",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // Expand/Collapse toggle
                                IconButton(
                                    onClick = { viewModel.togglePromptCardExpanded() },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (agentState.isPromptCardExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Expand/Collapse",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        if (agentState.isPromptCardExpanded) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                                    .padding(8.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                if (agentState.currentPrompt.isNotBlank()) {
                                    SelectionContainer {
                                        Text(
                                            text = agentState.currentPrompt,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 11.sp,
                                                lineHeight = 15.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                } else {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "এখনো কোনো প্রম্পট লোড করা হয়নি।",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        OutlinedButton(
                                            onClick = {
                                                tempPromptInput = ""
                                                showEditPromptDialog = true
                                            },
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("প্রম্পট লিখুন বা পেস্ট করুন", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Error message banner if any
        agentState.errorMessage?.let { err ->
            Surface(
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Chat Message Stream
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            // Welcome Card if no messages yet
            if (agentState.messages.isEmpty()) {
                item {
                    AgentWelcomeCard(
                        onQuickFixSelected = { fixInstruction ->
                            viewModel.sendAgentMessage(fixInstruction)
                        }
                    )
                }
            }

            items(agentState.messages, key = { it.id }) { message ->
                AgentChatMessageItem(
                    message = message,
                    context = context,
                    onUseAsActivePrompt = { prompt ->
                        viewModel.updateAgentActivePrompt(prompt)
                        Toast.makeText(context, "সক্রিয় প্রম্পট আপডেট করা হয়েছে!", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            if (agentState.isAgentThinking) {
                item {
                    AgentThinkingBubble()
                }
            }
        }

        // Quick Correction Chips (এক-ক্লিকে সংশোধন বাটন)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(top = 4.dp, bottom = 2.dp)
        ) {
            Text(
                text = "⚡ দ্রুত সংশোধন ফিল্টার:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                QuickFixChip(
                    label = "🎥 ড্রোন শট",
                    description = "ক্যামেরা অ্যাঙ্গেল হাই-অ্যাঙ্গেল ড্রোন শট করে দাও",
                    enabled = !agentState.isAgentThinking,
                    color = NeonCyan,
                    onClick = { viewModel.sendAgentMessage("ক্যামেরা অ্যাঙ্গেল ক্লোজ-আপ থেকে পরিবর্তন করে সিনেমাটিক হাই-অ্যাঙ্গেল এরিয়াল ড্রোন শট করে দাও।") }
                )
                QuickFixChip(
                    label = "🎥 ক্লোজ-আপ ৮৫মিমি",
                    description = "৮৫মিমি স্টুডিও পোর্ট্রেট ক্লোজ-আপ ফ্রেমিং",
                    enabled = !agentState.isAgentThinking,
                    color = NeonCyan,
                    onClick = { viewModel.sendAgentMessage("ক্যামেরা ফ্রেমিং পরিবর্তন করে ৮৫মিমি পোর্ট্রেট প্রাইম লেন্স দিয়ে ফেসিয়াল ক্লোজ-আপ শট করো।") }
                )
                QuickFixChip(
                    label = "💡 গোল্ডেন আওয়ার",
                    description = "লাইটিং গোল্ডেন আওয়ার সূর্যাস্ত করো",
                    enabled = !agentState.isAgentThinking,
                    color = Color(0xFFF59E0B),
                    onClick = { viewModel.sendAgentMessage("লাইটিং পরিবর্তন করে গোল্ডেন আওয়ারের উষ্ণ প্রাকৃতিক সূর্যাস্তের আলো ও রিচ শ্যাডো যুক্ত করো।") }
                )
                QuickFixChip(
                    label = "💡 নিয়ন সাইবারপাঙ্ক",
                    description = "ভলিউমেট্রিক নিয়ন লাইটিং",
                    enabled = !agentState.isAgentThinking,
                    color = NeonPurple,
                    onClick = { viewModel.sendAgentMessage("লাইটিং সাইবারপাঙ্ক স্টাইলের ব্লু ও ম্যাজেন্টা ভলিউমেট্রিক নিয়ন লাইট করো।") }
                )
                QuickFixChip(
                    label = "👔 ব্লেজার ও স্যুট",
                    description = "পোশাক ফরমাল ব্লেজারে পরিবর্তন করো",
                    enabled = !agentState.isAgentThinking,
                    color = EmeraldGreen,
                    onClick = { viewModel.sendAgentMessage("মডেলের পোশাক পরিবর্তন করে প্রিমিয়াম নেভি ব্লু এক্সিকিউটিভ ব্লেজার এবং ফরমাল লুক দাও।") }
                )
                QuickFixChip(
                    label = "🌆 ঢাকা সিটি ব্যাকগ্রাউন্ড",
                    description = "আধুনিক বাংলাদেশ সিটিস্কেপ",
                    enabled = !agentState.isAgentThinking,
                    color = ElectricIndigo,
                    onClick = { viewModel.sendAgentMessage("ব্যাকগ্রাউন্ড পরিবর্তন করে আধুনিক ঢাকার গুলশান/হাতিরঝিল মনোরম সিটিস্কেপ এবং সুন্দর বোকেহ দাও।") }
                )
                QuickFixChip(
                    label = "🔍 8K আল্ট্রা-রিয়েলিজম",
                    description = "ম্যাক্সিমাম ফটোরিয়ালিজম ও টেক্সচার",
                    enabled = !agentState.isAgentThinking,
                    color = NeonGreen,
                    onClick = { viewModel.sendAgentMessage("ভিজ্যুয়ালের কোয়ালিটি বাড়িয়ে Hasselblad 8K আল্ট্রা-ফটোরিয়েলিস্টিক স্কিন টেক্সচার এবং পারফেক্ট ক্যাচলাইট করো।") }
                )
                QuickFixChip(
                    label = "🚫 গ্লিচ দূর করো",
                    description = "নেগেটিভ প্রম্পট আরও কঠোর করো",
                    enabled = !agentState.isAgentThinking,
                    color = MaterialTheme.colorScheme.error,
                    onClick = { viewModel.sendAgentMessage("সব ধরণের ফেসিয়াল ওয়ার্পিং, অতিরিক্ত আঙুল, বিকৃতি এবং গ্লিচ সম্পূর্ণ দূর করার জন্য নেগেটিভ প্রম্পট কঠোর করো।") }
                )
            }
        }

        // Bottom Input Field Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = {
                        Text(
                            text = "যেমন: ক্যামেরাটা একটু দূর থেকে দেখাও, লাইটিং ডার্ক করো...",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("agent_chat_input"),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Send Button with animated state
                Button(
                    onClick = {
                        val text = textInput
                        if (text.isNotBlank()) {
                            viewModel.sendAgentMessage(text)
                            textInput = ""
                        }
                    },
                    enabled = textInput.isNotBlank() && !agentState.isAgentThinking,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(46.dp)
                        .testTag("agent_send_button"),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (textInput.isNotBlank() && !agentState.isAgentThinking) {
                                    Brush.linearGradient(listOf(NeonCyan, NeonPurple))
                                } else {
                                    Brush.linearGradient(listOf(Color.Gray.copy(alpha = 0.4f), Color.Gray.copy(alpha = 0.4f)))
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (agentState.isAgentThinking) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "পাঠান",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Manual Edit / Set Prompt Dialog
    if (showEditPromptDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showEditPromptDialog = false },
            title = {
                Text(
                    text = "প্রম্পট লিখুন বা পেস্ট করুন",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "এখানে যেকোনো প্রম্পট পেস্ট করতে পারেন। এজেন্ট এটি বিশ্লেষণ করে আপনার কথামতো ঠিক করে দেবে।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = tempPromptInput,
                        onValueChange = { tempPromptInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        placeholder = { Text("এখানে প্রম্পট পেস্ট করুন...") },
                        textStyle = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempPromptInput.isNotBlank()) {
                            viewModel.loadPromptIntoAgent(tempPromptInput, "ম্যানুয়াল প্রম্পট")
                        }
                        showEditPromptDialog = false
                    }
                ) {
                    Text("সংরক্ষণ করুন")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEditPromptDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
fun QuickFixChip(
    label: String,
    description: String,
    enabled: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.12f),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(color.copy(alpha = 0.4f), color.copy(alpha = 0.1f)))
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = enabled) { onClick() }
    ) {
        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun AgentChatMessageItem(
    message: AgentChatMessage,
    context: Context,
    onUseAsActivePrompt: (String) -> Unit
) {
    val isUser = message.sender == AgentSender.USER
    var isCopied by remember { mutableStateOf(false) }

    LaunchedEffect(isCopied) {
        if (isCopied) {
            delay(2000)
            isCopied = false
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            // Agent Avatar
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(NeonCyan, NeonPurple))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Agent",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.fillMaxWidth(if (isUser) 0.85f else 0.92f)
        ) {
            // Chat Bubble
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                color = if (isUser) {
                    NeonPurple.copy(alpha = 0.25f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                },
                border = if (isUser) {
                    CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(ElectricIndigo, NeonPurple))
                    )
                } else {
                    CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(NeonCyan.copy(alpha = 0.4f), ElectricIndigo.copy(alpha = 0.2f)))
                    )
                }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Message text / explanation
                    Text(
                        text = message.messageText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    // If agent updated the prompt, show the prompt box inside bubble
                    if (!isUser && !message.refinedPrompt.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.linearGradient(listOf(NeonCyan, NeonPurple))
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "✨ সংশোধিত প্রম্পট (Updated Prompt):",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonCyan,
                                        fontSize = 10.sp
                                    )

                                    Row {
                                        // Copy button
                                        IconButton(
                                            onClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                clipboard.setPrimaryClip(ClipData.newPlainText("Refined Prompt", message.refinedPrompt))
                                                isCopied = true
                                                Toast.makeText(context, "সংশোধিত প্রম্পট কপি হয়েছে!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isCopied) Icons.Default.CheckCircle else Icons.Default.ContentCopy,
                                                contentDescription = "কপি",
                                                tint = if (isCopied) EmeraldGreen else NeonCyan,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }

                                        // Set as active
                                        IconButton(
                                            onClick = { onUseAsActivePrompt(message.refinedPrompt) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "সক্রিয় করুন",
                                                tint = NeonPurple,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .padding(6.dp)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    SelectionContainer {
                                        Text(
                                            text = message.refinedPrompt,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 10.sp,
                                                lineHeight = 14.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Timestamp
            Text(
                text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontSize = 9.sp,
                modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
            )
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            // User Avatar
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(ElectricIndigo, Color(0xFF6366F1)))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun AgentThinkingBubble() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(NeonCyan, NeonPurple))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "Agent",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(NeonCyan.copy(alpha = 0.5f), NeonPurple.copy(alpha = 0.3f)))
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = NeonCyan,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "এআই এজেন্ট চিন্তা করছে ও প্রম্পট সংশোধন করছে...",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeonCyan,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun AgentWelcomeCard(
    onQuickFixSelected: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(NeonCyan, NeonPurple, ElectricIndigo))
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Brush.linearGradient(listOf(NeonCyan, NeonPurple))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "স্বাগতম এজেন্ট মোডে! 🤖",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "আপনার এআই সিনেমাটোগ্রাফি ও প্রম্পট ডিরেক্টর",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "ভিডিও বা ক্যারেক্টার প্রম্পটে অনেক সময় ক্যামেরা অ্যাঙ্গেল, ভিজ্যুয়াল বা লাইটিং ভুল আসে। এই এজেন্ট মোডে আপনি সরাসরি চ্যাট করে প্রম্পটটি নিখুঁতভাবে সংশোধন করে নিতে পারবেন।",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "💡 আপনি কী কী করতে পারেন:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                BulletPoint(text = "ক্যামেরা শট ক্লোজ-আপ থেকে ড্রোন শট বা FPV ডাইভে বদলানো")
                BulletPoint(text = "লাইটিংকে গোল্ডেন আওয়ার বা সাইবারপাঙ্ক নিয়ন লুক দেওয়া")
                BulletPoint(text = "মডেলের চেহারা, পোশাক বা এক্সপ্রেশন নিখুঁত করা")
                BulletPoint(text = "ব্যাকগ্রাউন্ড লোকেশন ও পরিবেশ পরিবর্তন করা")
                BulletPoint(text = "সব ধরণের গ্লিচ বা ওয়ার্পিং দূর করতে নেগেটিভ প্রম্পট ঠিক করা")
            }
        }
    }
}

@Composable
fun BulletPoint(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text("• ", color = NeonCyan, fontWeight = FontWeight.Bold)
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
    }
}
