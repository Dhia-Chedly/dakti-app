package com.dakti.app.presentation.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class HomeUiState(
    val welcomeMessage: String = "Plan your next game, reserve a venue, and organize players with Dakti."
)

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun refreshPlaceholderMessage() {
        _uiState.update {
            it.copy(welcomeMessage = "Navigation shell active. Feature logic comes in later phases.")
        }
    }
}
