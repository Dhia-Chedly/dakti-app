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

data class MatchUiState(
    val matches: List<String> = emptyList(),
    val latestCreatedMatchId: String? = null
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
            when (val result = matchRepository.getMyMatches()) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(matches = result.data.map { match -> "${match.title} (${match.status})" })
                    }
                }

                is Resource.Error -> Unit
                Resource.Loading -> Unit
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

                is Resource.Error -> Unit
                Resource.Loading -> Unit
            }
        }
    }
}
