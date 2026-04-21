package com.dakti.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dakti.app.domain.model.MatchReadinessStatus
import com.dakti.app.domain.usecase.EvaluateMyMatchReadinessUseCase
import com.dakti.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val greetingTitle: String = "Welcome back",
    val summaryText: String = "Manage venues, matches, and team coordination from one dashboard.",
    val upcomingActions: List<String> = listOf(
        "Review available venue slots for this weekend",
        "Confirm player attendance for Friday night match",
        "Ask assistant for balanced team suggestions"
    ),
    val recentActivity: List<String> = listOf(
        "No completed reservations yet",
        "No match reports yet"
    ),
    val readinessAlertCount: Int = 0,
    val readinessHighlights: List<String> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val evaluateMyMatchReadinessUseCase: EvaluateMyMatchReadinessUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        refreshMonitoringHighlights()
    }

    fun refreshMonitoringHighlights() {
        viewModelScope.launch {
            when (val result = evaluateMyMatchReadinessUseCase()) {
                is Resource.Success -> {
                    val riskyMatches = result.data
                        .filter { item ->
                            item.status != MatchReadinessStatus.READY && item.shouldAlertOrganizer
                        }
                    _uiState.update { state ->
                        state.copy(
                            readinessAlertCount = riskyMatches.size,
                            readinessHighlights = riskyMatches.take(MAX_HIGHLIGHTS).map { item ->
                                "${item.matchTitle}: ${item.reason}"
                            }
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.update { state ->
                        state.copy(
                            readinessAlertCount = 0,
                            readinessHighlights = emptyList()
                        )
                    }
                }

                Resource.Loading -> Unit
            }
        }
    }

    private companion object {
        private const val MAX_HIGHLIGHTS: Int = 3
    }
}

