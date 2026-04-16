package com.dakti.app.presentation.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
    )
)

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
}

