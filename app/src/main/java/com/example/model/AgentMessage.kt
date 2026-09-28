package com.example.model

enum class AgentSender {
    USER,
    AGENT
}

data class AgentChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: AgentSender,
    val messageText: String,
    val refinedPrompt: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class AgentModeUiState(
    val currentPrompt: String = "",
    val originalPrompt: String = "",
    val promptVersion: Int = 1,
    val promptHistory: List<String> = emptyList(),
    val messages: List<AgentChatMessage> = emptyList(),
    val isAgentThinking: Boolean = false,
    val errorMessage: String? = null,
    val isPromptCardExpanded: Boolean = true
)
