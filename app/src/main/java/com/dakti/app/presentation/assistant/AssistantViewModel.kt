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

data class AssistantUiState(
    val lastPrompt: String = "",
    val lastResponse: String = "Ask the assistant for a placeholder suggestion."
)

@HiltViewModel
class AssistantViewModel @Inject constructor(
    private val assistantRepository: AssistantRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssistantUiState())
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    fun askSuggestion() {
        viewModelScope.launch {
            val prompt = "Suggest a balanced football match format for 10 players"
            _uiState.update { it.copy(lastPrompt = prompt) }
            when (val result = assistantRepository.askAssistant(prompt)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(lastResponse = result.data) }
                }

                is Resource.Error -> {
                    _uiState.update { it.copy(lastResponse = result.message) }
                }

                Resource.Loading -> Unit
            }
        }
    }
}
