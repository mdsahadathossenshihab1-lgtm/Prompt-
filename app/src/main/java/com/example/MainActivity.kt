package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.MainAppScreen
import com.example.ui.PromptFlowViewModel
import com.example.ui.theme.PromptFlowTheme

class MainActivity : ComponentActivity() {

    private val viewModel: PromptFlowViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themePreference by viewModel.currentTheme.collectAsState()

            PromptFlowTheme(themePreference = themePreference) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainAppScreen(viewModel = viewModel)
                }
            }
        }
    }
}
