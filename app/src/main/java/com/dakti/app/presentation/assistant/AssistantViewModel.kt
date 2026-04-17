package com.dakti.app.presentation.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dakti.app.domain.model.AssistantConversationMessage
import com.dakti.app.domain.model.AssistantMessageRole
import com.dakti.app.domain.usecase.GetAssistantQuickActionsUseCase
import com.dakti.app.domain.usecase.GetAssistantSuggestedPromptsUseCase
import com.dakti.app.domain.usecase.SendAssistantMessageUseCase
import com.dakti.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AssistantSuggestionUi(
    val id: String,
    val title: String,
    val description: String?,
    val typeLabel: String
)

data class AssistantQuickActionUi(
    val id: String,
    val title: String,
    val prompt: String
)

data class AssistantMessageUi(
    val id: String,
    val role: AssistantMessageRole,
    val text: String,
    val createdAt: Instant,
    val timestampLabel: String,
    val suggestions: List<AssistantSuggestionUi> = emptyList(),
    val providerLabel: String? = null,
    val isError: Boolean = false
)

data class AssistantUiState(
    val inputText: String = "",
    val isLoading: Boolean = false,
    val messages: List<AssistantMessageUi> = emptyList(),
    val quickActions: List<AssistantQuickActionUi> = emptyList(),
    val suggestedPrompts: List<String> = emptyList(),
    val errorMessage: String? = null,
    val lastFailedPrompt: String? = null
) {
    val canSend: Boolean
        get() = inputText.trim().isNotBlank() && !isLoading

    val showWelcomeState: Boolean
        get() = messages.isEmpty()
}

@HiltViewModel
class AssistantViewModel @Inject constructor(
    private val sendAssistantMessageUseCase: SendAssistantMessageUseCase,
    getAssistantQuickActionsUseCase: GetAssistantQuickActionsUseCase,
    getAssistantSuggestedPromptsUseCase: GetAssistantSuggestedPromptsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AssistantUiState(
            quickActions = getAssistantQuickActionsUseCase().map { action ->
                AssistantQuickActionUi(
                    id = action.id,
                    title = action.title,
                    prompt = action.prompt
                )
            },
            suggestedPrompts = getAssistantSuggestedPromptsUseCase()
        )
    )
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    fun onInputChanged(value: String) {
        _uiState.update { state ->
            state.copy(
                inputText = value,
                errorMessage = null
            )
        }
    }

    fun sendCurrentMessage() {
        sendMessage(_uiState.value.inputText)
    }

    fun sendSuggestedPrompt(prompt: String) {
        sendMessage(prompt)
    }

    fun sendQuickAction(action: AssistantQuickActionUi) {
        sendMessage(action.prompt)
    }

    fun retryLastFailedMessage() {
        val retryPrompt = _uiState.value.lastFailedPrompt ?: return
        sendMessage(retryPrompt)
    }

    fun clearError() {
        _uiState.update { state -> state.copy(errorMessage = null) }
    }

    private fun sendMessage(rawInput: String) {
        val normalized = rawInput.trim()
        if (normalized.isBlank()) {
            _uiState.update { state -> state.copy(errorMessage = "Type a message first.") }
            return
        }

        val userMessage = createMessage(
            role = AssistantMessageRole.USER,
            text = normalized,
            createdAt = Instant.now()
        )

        val updatedMessages = _uiState.value.messages + userMessage
        _uiState.update { state ->
            state.copy(
                inputText = "",
                isLoading = true,
                errorMessage = null,
                lastFailedPrompt = null,
                messages = updatedMessages
            )
        }

        val conversationHistory = updatedMessages
            .filterNot { item -> item.isError }
            .map { item ->
                AssistantConversationMessage(
                    id = item.id,
                    role = item.role,
                    text = item.text,
                    createdAt = item.createdAt
                )
            }

        viewModelScope.launch {
            when (
                val result = sendAssistantMessageUseCase(
                    message = normalized,
                    conversationHistory = conversationHistory
                )
            ) {
                is Resource.Success -> {
                    val now = Instant.now()
                    val assistantMessage = createMessage(
                        role = AssistantMessageRole.ASSISTANT,
                        text = result.data.text,
                        createdAt = now,
                        suggestions = result.data.suggestions.map { item ->
                            AssistantSuggestionUi(
                                id = item.id,
                                title = item.title,
                                description = item.description,
                                typeLabel = item.type.name.replace("_", " ")
                            )
                        },
                        providerLabel = buildProviderLabel(
                            provider = result.data.providerLabel,
                            usedFallback = result.data.usedFallback
                        )
                    )

                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            messages = state.messages + assistantMessage,
                            quickActions = result.data.quickActions.map { action ->
                                AssistantQuickActionUi(
                                    id = action.id,
                                    title = action.title,
                                    prompt = action.prompt
                                )
                            }
                        )
                    }
                }

                is Resource.Error -> {
                    val now = Instant.now()
                    val errorBubble = createMessage(
                        role = AssistantMessageRole.ASSISTANT,
                        text = "Assistant is unavailable right now.",
                        createdAt = now,
                        isError = true
                    )
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            messages = state.messages + errorBubble,
                            errorMessage = result.message,
                            lastFailedPrompt = normalized
                        )
                    }
                }

                Resource.Loading -> {
                    _uiState.update { state -> state.copy(isLoading = true) }
                }
            }
        }
    }

    private fun createMessage(
        role: AssistantMessageRole,
        text: String,
        createdAt: Instant,
        suggestions: List<AssistantSuggestionUi> = emptyList(),
        providerLabel: String? = null,
        isError: Boolean = false
    ): AssistantMessageUi =
        AssistantMessageUi(
            id = "msg-${UUID.randomUUID()}",
            role = role,
            text = text,
            createdAt = createdAt,
            timestampLabel = createdAt.atZone(ZoneId.systemDefault()).format(timeFormatter),
            suggestions = suggestions,
            providerLabel = providerLabel,
            isError = isError
        )

    private fun buildProviderLabel(
        provider: String,
        usedFallback: Boolean
    ): String {
        return if (usedFallback) {
            "$provider (fallback mode)"
        } else {
            provider
        }
    }

    companion object {
        private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}

