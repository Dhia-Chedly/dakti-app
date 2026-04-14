package com.dakti.app.presentation.invitations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dakti.app.domain.repository.InvitationRepository
import com.dakti.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InvitationUiState(
    val invitations: List<String> = emptyList(),
    val lastActionMessage: String = "No invitation action yet."
)

@HiltViewModel
class InvitationViewModel @Inject constructor(
    private val invitationRepository: InvitationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvitationUiState())
    val uiState: StateFlow<InvitationUiState> = _uiState.asStateFlow()

    init {
        loadInvitations()
    }

    fun loadInvitations() {
        viewModelScope.launch {
            when (val result = invitationRepository.getInvitations()) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            invitations = result.data.map { invitation ->
                                "${invitation.matchTitle} - from ${invitation.fromUser}"
                            }
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.update { it.copy(lastActionMessage = result.message) }
                }

                Resource.Loading -> Unit
            }
        }
    }

    fun acceptAllPlaceholders() {
        viewModelScope.launch {
            for (index in _uiState.value.invitations.indices) {
                invitationRepository.respondToInvitation(invitationId = "inv-$index", accepted = true)
            }
            _uiState.update { it.copy(lastActionMessage = "Accepted placeholder invitations.") }
        }
    }
}
