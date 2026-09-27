package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppBadge
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.GeneratorScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.ModelCreatorScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple

enum class AppTab(val title: String) {
    VIDEO_PROMPTS("Video Prompts"),
    MODEL_CREATOR("Model Creator"),
    HISTORY("History"),
    AUTH("Account"),
    SETTINGS("Settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: PromptFlowViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(AppTab.VIDEO_PROMPTS) }
    val config by viewModel.config.collectAsState()
    val authState by viewModel.authUiState.collectAsState()

    // MANDATORY AUTH GATE:
    // When the user opens the app, the Login / Sign Up screen must appear first.
    // The user cannot use or navigate the app until authenticated with Firebase.
    if (authState.currentUser == null) {
        AuthScreen(
            viewModel = viewModel,
            isAuthGate = true,
            modifier = modifier
        )
        return
    }

    // Android back button handling: return to Video Prompts if on sub-screens
    BackHandler(enabled = selectedTab != AppTab.VIDEO_PROMPTS) {
        selectedTab = AppTab.VIDEO_PROMPTS
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(NeonCyan, NeonPurple)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "PromptFlow AI",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Right side items in TopBar
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Locked Model indicator in TopBar
                            if (config.isModelLocked) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = NeonGreen.copy(alpha = 0.15f),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { selectedTab = AppTab.MODEL_CREATOR }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = NeonGreen,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "LOCKED",
                                            color = NeonGreen,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            }

                            // Firebase Profile / Login Button in TopBar
                            val currentUser = authState.currentUser
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (currentUser != null) NeonCyan.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedTab = AppTab.AUTH }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = "User Account",
                                        tint = if (currentUser != null) NeonCyan else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (currentUser != null) (currentUser.displayName?.take(8) ?: currentUser.email?.substringBefore("@")?.take(8) ?: "User") else "লগইন",
                                        color = if (currentUser != null) NeonCyan else MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("main_navigation_bar")
            ) {
                // Module 1: AI Video Master Prompt Generator
                NavigationBarItem(
                    selected = selectedTab == AppTab.VIDEO_PROMPTS,
                    onClick = { selectedTab = AppTab.VIDEO_PROMPTS },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == AppTab.VIDEO_PROMPTS) Icons.Filled.Movie else Icons.Outlined.Movie,
                            contentDescription = "Video Prompts"
                        )
                    },
                    label = { Text("Prompts", fontSize = 11.sp, maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_generator_tab")
                )

                // Module 2: AI Model Creator
                NavigationBarItem(
                    selected = selectedTab == AppTab.MODEL_CREATOR,
                    onClick = { selectedTab = AppTab.MODEL_CREATOR },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == AppTab.MODEL_CREATOR) Icons.Filled.Person else Icons.Outlined.Person,
                            contentDescription = "Model Creator"
                        )
                    },
                    label = { Text("Models", fontSize = 11.sp, maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_model_creator_tab")
                )

                // History
                NavigationBarItem(
                    selected = selectedTab == AppTab.HISTORY,
                    onClick = { selectedTab = AppTab.HISTORY },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == AppTab.HISTORY) Icons.Filled.History else Icons.Outlined.History,
                            contentDescription = "History"
                        )
                    },
                    label = { Text("History", fontSize = 11.sp, maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_history_tab")
                )

                // Account / Auth
                NavigationBarItem(
                    selected = selectedTab == AppTab.AUTH,
                    onClick = { selectedTab = AppTab.AUTH },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == AppTab.AUTH) Icons.Filled.AccountCircle else Icons.Outlined.AccountCircle,
                            contentDescription = "Account"
                        )
                    },
                    label = { Text("লগইন", fontSize = 11.sp, maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_auth_tab")
                )

                // Settings
                NavigationBarItem(
                    selected = selectedTab == AppTab.SETTINGS,
                    onClick = { selectedTab = AppTab.SETTINGS },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == AppTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                            contentDescription = "Settings"
                        )
                    },
                    label = { Text("Settings", fontSize = 11.sp, maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_settings_tab")
                )
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            label = "ScreenTransition"
        ) { tab ->
            when (tab) {
                AppTab.VIDEO_PROMPTS -> GeneratorScreen(
                    viewModel = viewModel,
                    onNavigateToModelCreator = { selectedTab = AppTab.MODEL_CREATOR }
                )
                AppTab.MODEL_CREATOR -> ModelCreatorScreen(
                    viewModel = viewModel,
                    onNavigateToVideoGenerator = { selectedTab = AppTab.VIDEO_PROMPTS }
                )
                AppTab.HISTORY -> HistoryScreen(
                    viewModel = viewModel,
                    onNavigateToGenerator = { selectedTab = AppTab.VIDEO_PROMPTS }
                )
                AppTab.AUTH -> AuthScreen(
                    viewModel = viewModel,
                    onNavigateBack = { selectedTab = AppTab.VIDEO_PROMPTS }
                )
                AppTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
