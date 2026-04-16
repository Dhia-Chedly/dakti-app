package com.dakti.app.presentation.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dakti.app.domain.repository.AssistantRepository
import com.dakti.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AssistantMessageUi(
    val isFromUser: Boolean,
    val text: String
)

data class AssistantUiState(
    val isLoading: Boolean = false,
    val suggestedPrompts: List<String> = listOf(
        "Suggest a balanced football match format for 10 players",
        "Create a simple invitation message for weekend match",
        "Help decide player rotation for a 5v5 game"
    ),
    val messages: List<AssistantMessageUi> = listOf(
        AssistantMessageUi(
            isFromUser = false,
            text = "Hi, I'm Dakti Assistant. Ask me for help organizing your next match."
        )
    ),
    val lastPrompt: String = "",
    val lastResponse: String = ""
)

@HiltViewModel
class AssistantViewModel @Inject constructor(
    private val assistantRepository: AssistantRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssistantUiState())
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    fun askSuggestion(prompt: String = DEFAULT_PROMPT) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    lastPrompt = prompt,
                    messages = it.messages + AssistantMessageUi(isFromUser = true, text = prompt)
                )
            }
            when (val result = assistantRepository.askAssistant(prompt)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            lastResponse = result.data,
                            messages = it.messages + AssistantMessageUi(
                                isFromUser = false,
                                text = result.data
                            )
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            lastResponse = result.message,
                            messages = it.messages + AssistantMessageUi(
                                isFromUser = false,
                                text = result.message
                            )
                        )
                    }
                }

                Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    private companion object {
        private const val DEFAULT_PROMPT: String =
            "Suggest a balanced football match format for 10 players"
    }
}

