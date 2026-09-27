package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.PromptFlowViewModel
import com.example.ui.components.AiPulseLoader
import com.example.ui.components.AppBadge
import com.example.ui.components.EmailVerificationView
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple

@Composable
fun AuthScreen(
    viewModel: PromptFlowViewModel,
    isAuthGate: Boolean = false,
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val authState by viewModel.authUiState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Login, 1: Sign Up
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Firebase Settings accordion
    var showProjectSettings by remember { mutableStateOf(false) }
    var customProjectId by remember { mutableStateOf(viewModel.getActiveFirebaseProjectId()) }
    var customApiKey by remember { mutableStateOf(viewModel.getActiveFirebaseApiKey()) }
    var customAppId by remember { mutableStateOf(viewModel.getActiveFirebaseAppId()) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Futuristic Ambient Glow Background
        Box(
            modifier = Modifier
                .size(340.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.radialGradient(
                        listOf(
                            NeonPurple.copy(alpha = 0.16f),
                            NeonCyan.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Branding Top Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(listOf(NeonCyan, NeonPurple))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Logo",
                        tint = Color.Black,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "PROMPTFLOW AI",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Master Video Prompt Studio",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Security Badge
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(NeonCyan.copy(alpha = 0.4f), NeonPurple.copy(alpha = 0.4f)))
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "FIREBASE 100% REAL SECURE AUTH",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Global Error Banner
            AnimatedVisibility(visible = authState.errorMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = authState.errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearAuthMessages() },
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Global Success Banner
            AnimatedVisibility(visible = authState.successMessage != null) {
                Surface(
                    color = NeonGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(NeonGreen, NeonCyan))
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = authState.successMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = NeonGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(
                            onClick = { viewModel.clearAuthMessages() },
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = NeonGreen, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // 1. EMAIL VERIFICATION STATE (MANDATORY REQUIREMENT)
            if (authState.requiresEmailVerification) {
                EmailVerificationView(
                    email = authState.pendingVerificationEmail.ifBlank { email },
                    onCheckVerification = { viewModel.checkEmailVerificationAndSignIn() },
                    onResendEmail = { viewModel.resendVerificationEmail() },
                    onCancelOrChangeAccount = { viewModel.dismissVerificationPrompt() },
                    isChecking = authState.isCheckingVerification
                )
            }
            // 2. LOADING ANIMATION (ACTIVE AUTH OPERATION)
            else if (authState.isLoading) {
                GlassmorphicCard(
                    modifier = Modifier.padding(vertical = 20.dp),
                    borderGradients = listOf(NeonCyan, NeonPurple)
                ) {
                    AiPulseLoader(
                        statusText = "Firebase একাউন্ট যাচাই করা হচ্ছে...",
                        subtitleText = "অনুগ্রহ করে অপেক্ষা করুন, নিরাপদ সংযোগ স্থাপিত হচ্ছে"
                    )
                }
            }
            // 3. LOGGED IN PROFILE (WHEN AUTHENTICATED)
            else if (authState.currentUser != null) {
                val currentUser = authState.currentUser!!
                GlassmorphicCard(
                    borderGradients = listOf(NeonCyan, NeonPurple, ElectricIndigo)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(NeonCyan, NeonPurple))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (currentUser.displayName?.take(1) ?: currentUser.email?.take(1) ?: "U").uppercase(),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = currentUser.displayName?.ifBlank { "User" } ?: "PromptFlow Creator",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = currentUser.email ?: (if (currentUser.isAnonymous) "Guest Account (Anonymous)" else "Verified User"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        AppBadge(
                            text = if (currentUser.isAnonymous) "GUEST MODE" else "VERIFIED & AUTHENTICATED",
                            backgroundColor = NeonGreen.copy(alpha = 0.2f),
                            textColor = NeonGreen
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("সংযুক্ত Firebase Project:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(viewModel.getActiveFirebaseProjectId(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = NeonCyan)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { viewModel.signOut() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("লগআউট করুন (Sign Out)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            // 4. AUTHENTICATION FORM (LOGIN / SIGN UP TABS)
            else {
                GlassmorphicCard(
                    borderGradients = listOf(NeonCyan.copy(alpha = 0.6f), NeonPurple.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        // High-tech Animated Tab Row
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color.Transparent,
                            contentColor = NeonCyan,
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    color = NeonCyan,
                                    height = 3.dp
                                )
                            }
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = {
                                    selectedTab = 0
                                    viewModel.clearAuthMessages()
                                },
                                text = {
                                    Text(
                                        text = "লগইন (Sign In)",
                                        fontWeight = if (selectedTab == 0) FontWeight.Black else FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                }
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = {
                                    selectedTab = 1
                                    viewModel.clearAuthMessages()
                                },
                                text = {
                                    Text(
                                        text = "নতুন একাউন্ট (Sign Up)",
                                        fontWeight = if (selectedTab == 1) FontWeight.Black else FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(22.dp))

                        AnimatedContent(
                            targetState = selectedTab,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "tab_form_switch"
                        ) { tab ->
                            Column {
                                // SIGN UP ONLY: Display Name
                                if (tab == 1) {
                                    OutlinedTextField(
                                        value = displayName,
                                        onValueChange = { displayName = it },
                                        label = { Text("আপনার পূর্ণ নাম (Full Name)") },
                                        placeholder = { Text("যেমন: শিহাব") },
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = NeonCyan) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("auth_name_input"),
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                }

                                // Email Input
                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    label = { Text("ইমেইল / জিমেইল (Email)") },
                                    placeholder = { Text("shihabno.18@gmail.com") },
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = NeonCyan) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("auth_email_input"),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Email,
                                        imeAction = ImeAction.Next
                                    )
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Password Input
                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    label = { Text("পাসওয়ার্ড (Password)") },
                                    placeholder = { Text("কমপক্ষে ৬ অক্ষর") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = NeonPurple) },
                                    trailingIcon = {
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(
                                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = "Toggle password visibility"
                                            )
                                        }
                                    },
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("auth_password_input"),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            focusManager.clearFocus()
                                            if (tab == 0) {
                                                viewModel.signInWithEmail(email, password)
                                            } else {
                                                viewModel.signUpWithEmail(email, password, displayName)
                                            }
                                        }
                                    )
                                )

                                if (tab == 1) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "🔒 সাইন আপ করার পর আপনার ইমেইলে একটি ভেরিফিকেশন লিঙ্ক পাঠানো হবে। লিঙ্কটিতে ক্লিক করে ভেরিফাই করলেই অ্যাপ ব্যবহার করতে পারবেন।",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(22.dp))

                                // Primary Submit Button with Gradient
                                Button(
                                    onClick = {
                                        focusManager.clearFocus()
                                        if (tab == 0) {
                                            viewModel.signInWithEmail(email, password)
                                        } else {
                                            viewModel.signUpWithEmail(email, password, displayName)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("auth_submit_button"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                    contentPadding = PaddingValues()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(NeonCyan, NeonPurple)
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (tab == 0) "লগইন করুন (SIGN IN)" else "অ্যাকাউন্ট তৈরি ও ভেরিফাই করুন",
                                            fontWeight = FontWeight.Black,
                                            color = Color.Black,
                                            fontSize = 14.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Divider OR
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            Text(
                                text = "  অথবা  ",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Guest Mode (Direct testing)
                        OutlinedButton(
                            onClick = { viewModel.signInAnonymously() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.linearGradient(listOf(NeonCyan.copy(alpha = 0.4f), NeonPurple.copy(alpha = 0.4f)))
                            )
                        ) {
                            Icon(Icons.Default.PersonOutline, contentDescription = null, modifier = Modifier.size(18.dp), tint = NeonCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("গেস্ট হিসেবে চালিয়ে যান (Instant Guest Mode)", fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Real Firebase Project Info Card
            GlassmorphicCard(
                borderGradients = listOf(ElectricIndigo.copy(alpha = 0.4f), NeonPurple.copy(alpha = 0.4f)),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Firebase প্রজেক্ট কনফিগারেশন",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = { showProjectSettings = !showProjectSettings },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (showProjectSettings) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = "Toggle project settings",
                                tint = NeonCyan
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = showProjectSettings,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = NeonPurple.copy(alpha = 0.12f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "আপনার জিমেইল (shihabno.18@gmail.com) এর সাথে প্রজেক্ট ID: project-d28c75fa-a3af-48dc-ac6 সরাসরি যুক্ত আছে।",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = customProjectId,
                                onValueChange = { customProjectId = it },
                                label = { Text("Project ID", fontSize = 12.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = customApiKey,
                                onValueChange = { customApiKey = it },
                                label = { Text("API Key", fontSize = 12.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    viewModel.updateFirebaseCustomProject(
                                        projectId = customProjectId.trim(),
                                        apiKey = customApiKey.trim(),
                                        appId = customAppId.trim()
                                    )
                                    Toast.makeText(context, "Firebase প্রজেক্ট আপডেট করা হয়েছে", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("প্রজেক্ট আপডেট ও সংরক্ষণ", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
