package com.dakti.app.presentation.reservations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dakti.app.domain.repository.ReservationRepository
import com.dakti.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReservationUiState(
    val reservations: List<String> = emptyList(),
    val confirmationMessage: String = "Reservation flow will be completed in the next phase."
)

@HiltViewModel
class ReservationViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReservationUiState())
    val uiState: StateFlow<ReservationUiState> = _uiState.asStateFlow()

    init {
        loadMyReservations()
    }

    fun loadMyReservations() {
        viewModelScope.launch {
            when (val result = reservationRepository.getMyReservations()) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            reservations = result.data.map { reservation ->
                                "${reservation.venueName} - ${reservation.timeSlot}"
                            }
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.update { it.copy(confirmationMessage = result.message) }
                }

                Resource.Loading -> Unit
            }
        }
    }

    fun confirmReservationForVenue(venueId: String) {
        viewModelScope.launch {
            when (val result = reservationRepository.confirmReservation(venueId)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            confirmationMessage = "Reservation confirmed for ${result.data.venueName}"
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.update { it.copy(confirmationMessage = result.message) }
                }

                Resource.Loading -> Unit
            }
        }
    }
}
