package com.dakti.app.presentation.matches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dakti.app.domain.repository.MatchRepository
import com.dakti.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MatchListItemUi(
    val id: String,
    val title: String,
    val status: String,
    val requiredPlayers: Int
)

data class MatchUiState(
    val isLoading: Boolean = false,
    val matches: List<MatchListItemUi> = emptyList(),
    val latestCreatedMatchId: String? = null,
    val openMatchesCount: Int = 0,
    val errorMessage: String? = null
)

@HiltViewModel
class MatchViewModel @Inject constructor(
    private val matchRepository: MatchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchUiState())
    val uiState: StateFlow<MatchUiState> = _uiState.asStateFlow()

    init {
        loadMatches()
    }

    fun loadMatches() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = matchRepository.getMyMatches()) {
                is Resource.Success -> {
                    val mapped = result.data.map { match ->
                        MatchListItemUi(
                            id = match.id,
                            title = match.title,
                            status = match.status.name,
                            requiredPlayers = match.requiredPlayers
                        )
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            matches = mapped,
                            openMatchesCount = mapped.count { item ->
                                item.status.equals("OPEN", ignoreCase = true) ||
                                    item.status.equals("DRAFT", ignoreCase = true)
                            }
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }

                Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun createDemoMatch() {
        viewModelScope.launch {
            when (val result = matchRepository.createMatch("New Friendly Match")) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(latestCreatedMatchId = result.data.id)
                    }
                    loadMatches()
                }

                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }

                Resource.Loading -> Unit
            }
        }
    }
}

