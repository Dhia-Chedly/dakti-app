package com.dakti.app.presentation.venues

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dakti.app.domain.model.TimeSlot
import com.dakti.app.domain.model.VenueWithTimeSlots
import com.dakti.app.domain.usecase.GetVenueDetailsUseCase
import com.dakti.app.domain.usecase.GetVenueSportTypesUseCase
import com.dakti.app.domain.usecase.SearchVenuesUseCase
import com.dakti.app.integration.DialerPayload
import com.dakti.app.integration.VenueLocationPayload
import com.dakti.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val ALL_SPORT_FILTER: String = "All"

data class VenueListItemUi(
    val id: String,
    val name: String,
    val sportType: String,
    val locationLabel: String,
    val address: String,
    val priceLabel: String,
    val availabilityLabel: String,
    val nextAvailableSlotLabel: String?
)

data class VenueTimeSlotUi(
    val id: String,
    val timeLabel: String,
    val isAvailable: Boolean,
    val capacityLabel: String?
)

data class VenueDetailsUi(
    val id: String,
    val name: String,
    val sportType: String,
    val locationLabel: String,
    val address: String,
    val contactPhone: String?,
    val latitude: Double?,
    val longitude: Double?,
    val description: String,
    val imageUrl: String?,
    val priceLabel: String,
    val amenities: List<String>,
    val timeSlots: List<VenueTimeSlotUi>
)

data class VenueUiState(
    val isLoading: Boolean = false,
    val isDetailsLoading: Boolean = false,
    val searchQuery: String = "",
    val sportFilters: List<String> = listOf(ALL_SPORT_FILTER),
    val selectedSportFilter: String = ALL_SPORT_FILTER,
    val filteredVenues: List<VenueListItemUi> = emptyList(),
    val selectedVenueDetails: VenueDetailsUi? = null,
    val selectedSlotId: String? = null,
    val errorMessage: String? = null,
    val detailsErrorMessage: String? = null
)

@HiltViewModel
class VenueViewModel @Inject constructor(
    private val searchVenuesUseCase: SearchVenuesUseCase,
    private val getVenueDetailsUseCase: GetVenueDetailsUseCase,
    private val getVenueSportTypesUseCase: GetVenueSportTypesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VenueUiState())
    val uiState: StateFlow<VenueUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        refreshVenues()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state -> state.copy(searchQuery = query) }
        refreshVenues()
    }

    fun onSportFilterSelected(filter: String) {
        _uiState.update { state -> state.copy(selectedSportFilter = filter) }
        refreshVenues()
    }

    fun refreshVenues() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            val sportFilters = when (val sportTypesResult = getVenueSportTypesUseCase()) {
                is Resource.Success -> listOf(ALL_SPORT_FILTER) + sportTypesResult.data
                is Resource.Error -> listOf(ALL_SPORT_FILTER)
                Resource.Loading -> listOf(ALL_SPORT_FILTER)
            }

            val selectedSport = _uiState.value.selectedSportFilter.takeUnless { value ->
                value == ALL_SPORT_FILTER
            }

            when (
                val venuesResult = searchVenuesUseCase(
                    query = _uiState.value.searchQuery,
                    sportType = selectedSport
                )
            ) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            sportFilters = sportFilters,
                            filteredVenues = venuesResult.data.map { venueWithSlots ->
                                venueWithSlots.toListItemUi()
                            }
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            sportFilters = sportFilters,
                            filteredVenues = emptyList(),
                            errorMessage = venuesResult.message
                        )
                    }
                }

                Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun loadVenueDetails(venueId: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isDetailsLoading = true,
                    detailsErrorMessage = null,
                    selectedVenueDetails = null,
                    selectedSlotId = null
                )
            }

            when (val result = getVenueDetailsUseCase(venueId)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isDetailsLoading = false,
                            selectedSlotId = null,
                            selectedVenueDetails = result.data.toDetailsUi()
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isDetailsLoading = false,
                            selectedVenueDetails = null,
                            selectedSlotId = null,
                            detailsErrorMessage = result.message
                        )
                    }
                }

                Resource.Loading -> {
                    _uiState.update { it.copy(isDetailsLoading = true) }
                }
            }
        }
    }

    fun selectTimeSlot(slotId: String) {
        val details = _uiState.value.selectedVenueDetails ?: return
        val isAvailable = details.timeSlots
            .firstOrNull { slot -> slot.id == slotId }
            ?.isAvailable == true
        if (!isAvailable) {
            return
        }

        _uiState.update { state ->
            state.copy(selectedSlotId = slotId)
        }
    }

    fun buildSelectedVenueLocationPayload(): VenueLocationPayload? {
        val details = _uiState.value.selectedVenueDetails ?: return null
        val hasAddress = details.address.isNotBlank()
        val hasCoordinates = details.latitude != null && details.longitude != null
        if (!hasAddress && !hasCoordinates) {
            return null
        }

        return VenueLocationPayload(
            venueName = details.name,
            address = details.address,
            latitude = details.latitude,
            longitude = details.longitude
        )
    }

    fun buildSelectedVenueDialerPayload(): DialerPayload? {
        val number = _uiState.value.selectedVenueDetails
            ?.contactPhone
            ?.takeIf { value -> value.isNotBlank() }
            ?: return null
        return DialerPayload(phoneNumber = number)
    }

    private fun VenueWithTimeSlots.toListItemUi(): VenueListItemUi {
        val availableSlots = slots.filter { slot -> slot.isAvailable }
        val nextAvailable = availableSlots.minByOrNull { slot -> slot.startTime }

        return VenueListItemUi(
            id = venue.id,
            name = venue.name,
            sportType = venue.sportType,
            locationLabel = venue.locationLabel(),
            address = venue.address,
            priceLabel = "${venue.currency} ${venue.pricePerHour.toInt()} / hour",
            availabilityLabel = "${availableSlots.size} available slot(s)",
            nextAvailableSlotLabel = nextAvailable?.toSlotLabel()
        )
    }

    private fun VenueWithTimeSlots.toDetailsUi(): VenueDetailsUi =
        VenueDetailsUi(
            id = venue.id,
            name = venue.name,
            sportType = venue.sportType,
            locationLabel = venue.locationLabel(),
            address = venue.address,
            contactPhone = venue.contactPhone,
            latitude = venue.latitude,
            longitude = venue.longitude,
            description = venue.description ?: "No venue description provided yet.",
            imageUrl = venue.imageUrl,
            priceLabel = "${venue.currency} ${venue.pricePerHour.toInt()} / hour",
            amenities = venue.amenities,
            timeSlots = slots
                .sortedBy { slot -> slot.startTime }
                .map { slot ->
                    VenueTimeSlotUi(
                        id = slot.id,
                        timeLabel = slot.toSlotLabel(),
                        isAvailable = slot.isAvailable,
                        capacityLabel = slot.capacity?.let { capacity -> "Capacity $capacity" }
                    )
                }
        )

    private fun TimeSlot.toSlotLabel(): String {
        val zoneId = ZoneId.systemDefault()
        val startDateTime = startTime.atZone(zoneId)
        val endDateTime = endTime.atZone(zoneId)
        return "${startDateTime.format(slotStartFormatter)} - ${endDateTime.format(slotEndFormatter)}"
    }

    private fun com.dakti.app.domain.model.Venue.locationLabel(): String {
        val statePart = state?.takeIf { it.isNotBlank() }?.let { "$it, " }.orEmpty()
        return "$city, ${statePart}$country"
    }

    companion object {
        private val slotStartFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, d MMM HH:mm")
        private val slotEndFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}
