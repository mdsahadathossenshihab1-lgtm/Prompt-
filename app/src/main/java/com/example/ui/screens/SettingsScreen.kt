package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.PromptFlowViewModel
import com.example.ui.components.AppBadge
import com.example.ui.components.SectionHeader
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SunsetAmber

@Composable
fun SettingsScreen(
    viewModel: PromptFlowViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val currentTheme by viewModel.currentTheme.collectAsState()
    val prefs = viewModel.preferencesManager

    var customKeyInput by remember { mutableStateOf("") }
    var showKeyEditor by remember { mutableStateOf(false) }

    var showFirebaseConfigEditor by remember { mutableStateOf(false) }
    var fbProjectId by remember { mutableStateOf(prefs.getFirebaseProjectId()) }
    var fbApiKey by remember { mutableStateOf(prefs.getFirebaseApiKey()) }
    var fbAppId by remember { mutableStateOf(prefs.getFirebaseAppId()) }

    var defaultRatio by remember { mutableStateOf(prefs.getDefaultAspectRatio()) }
    var defaultDuration by remember { mutableStateOf(prefs.getDefaultDuration()) }
    var defaultStyle by remember { mutableStateOf(prefs.getDefaultStyle()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(2.dp))

        SectionHeader(
            title = "App Settings",
            subtitle = "AI configuration, Firebase database, and theme preferences",
            icon = Icons.Default.Settings
        )

        // Inbuilt AI Model & Provider Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("api_status_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SectionHeader(
                    title = "Inbuilt AI Video Engine",
                    subtitle = "XKIRO Cloud • Qwen 3.8 Omni Flash (High-Speed Engine)",
                    icon = Icons.Default.Speed
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI Model Engine",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AppBadge(
                        text = "qwen/qwen3.8-omni-flash:free",
                        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                        textColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "API Endpoint",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "https://api.xkiro.com/v1",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Inbuilt API Status",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Inbuilt & Active (${prefs.getMaskedApiKey()})",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen
                        )
                    }
                }

                if (uiState.apiTestResult != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.apiTestResult ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.testApiConnection("") },
                        enabled = !uiState.isTestingApi,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("test_api_button")
                    ) {
                        if (uiState.isTestingApi) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text("Test API Connection", style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedButton(
                        onClick = { showKeyEditor = !showKeyEditor },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("edit_key_button")
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (showKeyEditor) "Close" else "Custom Key", style = MaterialTheme.typography.labelMedium)
                    }
                }

                if (showKeyEditor) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customKeyInput,
                            onValueChange = { customKeyInput = it },
                            label = { Text("Custom XKIRO API Key (or override)") },
                            placeholder = { Text("sk-...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    if (customKeyInput.isNotBlank()) {
                                        prefs.setCustomApiKey(customKeyInput)
                                        Toast.makeText(context, "Custom API key saved", Toast.LENGTH_SHORT).show()
                                        customKeyInput = ""
                                        showKeyEditor = false
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Save Custom Key")
                            }
                            OutlinedButton(
                                onClick = {
                                    prefs.clearCustomApiKey()
                                    Toast.makeText(context, "Reverted to inbuilt key", Toast.LENGTH_SHORT).show()
                                    showKeyEditor = false
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Reset to Inbuilt")
                            }
                        }
                    }
                }
            }
        }

        // Firebase Cloud Database Card
        val historyList by viewModel.historyList.collectAsState()
        val isFirebaseReady = viewModel.isFirebaseAvailable()
        val syncedCount = historyList.count { it.isSyncedWithCloud }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("firebase_database_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SectionHeader(
                    title = "Firebase Cloud Database",
                    subtitle = "Cloud Firestore prompt backup & real-time sync",
                    icon = Icons.Default.Cloud
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Database Engine",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Cloud Firestore",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Firebase Status",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isFirebaseReady) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isFirebaseReady) EmeraldGreen else SunsetAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isFirebaseReady) "Active & Connected" else "Inbuilt Engine Ready",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isFirebaseReady) EmeraldGreen else SunsetAmber
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cloud Synced Prompts",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AppBadge(
                        text = "$syncedCount / ${historyList.size} Prompts",
                        backgroundColor = if (syncedCount > 0) EmeraldGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                        textColor = if (syncedCount > 0) EmeraldGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (uiState.cloudMessage != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.cloudMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.syncAllHistoryToCloud() },
                        enabled = !uiState.isSyncingCloud,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sync_history_to_firebase_button")
                    ) {
                        if (uiState.isSyncingCloud) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sync All", style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedButton(
                        onClick = { viewModel.testFirebaseDatabase() },
                        enabled = !uiState.isSyncingCloud,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("test_firebase_button")
                    ) {
                        Text("Test DB", style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedButton(
                        onClick = { showFirebaseConfigEditor = !showFirebaseConfigEditor },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (showFirebaseConfigEditor) "Hide" else "Config", style = MaterialTheme.typography.labelMedium)
                    }
                }

                if (showFirebaseConfigEditor) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = fbProjectId,
                            onValueChange = { fbProjectId = it },
                            label = { Text("Firebase Project ID") },
                            placeholder = { Text("e.g. my-promptflow-app") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = fbApiKey,
                            onValueChange = { fbApiKey = it },
                            label = { Text("Web API Key") },
                            placeholder = { Text("AIzaSy...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = fbAppId,
                            onValueChange = { fbAppId = it },
                            label = { Text("Mobile App ID") },
                            placeholder = { Text("1:123456789:android:...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                viewModel.saveFirebaseConfig(fbProjectId, fbApiKey, fbAppId)
                                Toast.makeText(context, "Firebase configuration saved", Toast.LENGTH_SHORT).show()
                                showFirebaseConfigEditor = false
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Connect Firebase Project")
                        }
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "ফায়ারবেস সংযোগ নির্দেশিকা (Firebase Setup):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "আপনার ফায়ারবেস প্রজেক্টের সাথে যুক্ত করতে Firebase Console থেকে google-services.json ফাইলটি ডাউনলোড করে app/ ফোল্ডারে রাখুন অথবা উপরের Config বাটনে চাপ দিয়ে প্রজেক্ট আইডি যুক্ত করুন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Default Generation Preferences Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SectionHeader(
                    title = "Default Prompt Preferences",
                    subtitle = "Set presets for newly created prompts",
                    icon = Icons.Default.Tune
                )

                // Aspect ratio
                Text(
                    text = "Default Aspect Ratio",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("9:16", "16:9", "1:1").forEach { ratio ->
                        FilterChip(
                            selected = defaultRatio == ratio,
                            onClick = {
                                defaultRatio = ratio
                                prefs.setDefaultAspectRatio(ratio)
                            },
                            label = { Text(ratio) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                // Duration
                Text(
                    text = "Default Duration",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("8 seconds", "10 seconds", "15 seconds", "20 seconds").forEach { dur ->
                        FilterChip(
                            selected = defaultDuration == dur,
                            onClick = {
                                defaultDuration = dur
                                prefs.setDefaultDuration(dur)
                            },
                            label = { Text(dur) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                // Style
                Text(
                    text = "Default Style",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Cinematic", "Realistic", "Commercial").forEach { style ->
                        FilterChip(
                            selected = defaultStyle == style,
                            onClick = {
                                defaultStyle = style
                                prefs.setDefaultStyle(style)
                            },
                            label = { Text(style) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        // Appearance Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SectionHeader(
                    title = "Appearance & Theme",
                    subtitle = "Switch visual styling mode",
                    icon = Icons.Default.Palette
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("DARK", "LIGHT", "SYSTEM").forEach { theme ->
                        FilterChip(
                            selected = currentTheme == theme,
                            onClick = {
                                prefs.setTheme(theme)
                            },
                            label = {
                                Text(
                                    when (theme) {
                                        "DARK" -> "Dark Mode"
                                        "LIGHT" -> "Light Mode"
                                        else -> "System"
                                    }
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        // About & Guidelines Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
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
                    title = "PromptFlow Master Engine Spec",
                    subtitle = "Strict adherence to 22-part video directives",
                    icon = Icons.Default.DisplaySettings
                )

                Text(
                    text = "• Preserves original dialogue completely (Bengali, English, mixed)\n" +
                            "• Mandates one continuous voice take, no repetition, clean stop\n" +
                            "• Synchronizes cutaway B-roll semantically with spoken words\n" +
                            "• Ensures 100% character face and identity consistency with reference image\n" +
                            "• Includes strict negative prompts eliminating common video AI artifacts\n" +
                            "• Inbuilt persistence: Room Database + Firebase Cloud Firestore",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
