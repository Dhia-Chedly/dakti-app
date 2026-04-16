package com.dakti.app.presentation.venues

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dakti.app.domain.repository.VenueRepository
import com.dakti.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VenueListItemUi(
    val id: String,
    val name: String,
    val sportType: String,
    val city: String,
    val pricePerHour: Double,
    val currency: String
)

data class VenueUiState(
    val isLoading: Boolean = false,
    val venues: List<VenueListItemUi> = emptyList(),
    val selectedVenueId: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class VenueViewModel @Inject constructor(
    private val venueRepository: VenueRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VenueUiState())
    val uiState: StateFlow<VenueUiState> = _uiState.asStateFlow()

    init {
        loadVenues()
    }

    fun loadVenues() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = venueRepository.getVenues()) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            venues = result.data.map { venue ->
                                VenueListItemUi(
                                    id = venue.id,
                                    name = venue.name,
                                    sportType = venue.sportType,
                                    city = venue.city,
                                    pricePerHour = venue.pricePerHour,
                                    currency = venue.currency
                                )
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

    fun selectVenue(venueId: String) {
        _uiState.update { it.copy(selectedVenueId = venueId) }
    }
}

