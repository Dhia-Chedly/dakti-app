package com.dakti.app.presentation.matches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dakti.app.domain.model.MatchCreatePayload
import com.dakti.app.domain.model.MatchReservationContext
import com.dakti.app.domain.model.MatchStatus
import com.dakti.app.domain.model.MatchWithContext
import com.dakti.app.domain.model.VenueWithTimeSlots
import com.dakti.app.domain.usecase.CreateMatchUseCase
import com.dakti.app.domain.usecase.GetMatchDetailsUseCase
import com.dakti.app.domain.usecase.GetMatchReservationContextsUseCase
import com.dakti.app.domain.usecase.GetMyMatchesUseCase
import com.dakti.app.domain.usecase.GetVenuesUseCase
import com.dakti.app.domain.usecase.ScheduleMatchReminderUseCase
import com.dakti.app.integration.CalendarEventPayload
import com.dakti.app.integration.EmailPayload
import com.dakti.app.integration.ShareMessagePayload
import com.dakti.app.integration.VenueLocationPayload
import com.dakti.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MatchReservationContextUi(
    val reservationId: String,
    val venueId: String,
    val venueName: String,
    val sportType: String,
    val timeSlotLabel: String,
    val scheduledStartTime: Instant,
    val displayLabel: String
)

data class MatchVenueOptionUi(
    val venueId: String,
    val venueName: String,
    val sportType: String,
    val address: String
)

data class MatchListItemUi(
    val id: String,
    val title: String,
    val sportType: String,
    val venueName: String,
    val scheduledLabel: String,
    val status: MatchStatus,
    val statusLabel: String,
    val requiredPlayers: Int,
    val invitedPlayersCount: Int,
    val confirmedPlayersCount: Int,
    val pendingPlayersCount: Int,
    val declinedPlayersCount: Int,
    val remainingSpots: Int
)

data class MatchDetailsUi(
    val id: String,
    val title: String,
    val sportType: String,
    val venueName: String,
    val venueAddress: String,
    val scheduledStartTime: Instant,
    val scheduledLabel: String,
    val status: MatchStatus,
    val statusLabel: String,
    val requiredPlayers: Int,
    val invitedPlayersCount: Int,
    val confirmedPlayersCount: Int,
    val pendingPlayersCount: Int,
    val declinedPlayersCount: Int,
    val remainingSpots: Int,
    val organizerName: String?,
    val reservationReference: String?,
    val description: String?
)

data class MatchCreateFormState(
    val selectedReservationId: String? = null,
    val selectedVenueId: String? = null,
    val sportType: String = "",
    val scheduledAtInput: String = "",
    val requiredPlayersInput: String = "",
    val description: String = ""
)

data class MatchUiState(
    val isLoading: Boolean = false,
    val matches: List<MatchListItemUi> = emptyList(),
    val openMatchesCount: Int = 0,
    val reservationContexts: List<MatchReservationContextUi> = emptyList(),
    val venueOptions: List<MatchVenueOptionUi> = emptyList(),
    val formState: MatchCreateFormState = MatchCreateFormState(),
    val isCreatingMatch: Boolean = false,
    val createSuccessMessage: String? = null,
    val createErrorMessage: String? = null,
    val latestCreatedMatchId: String? = null,
    val isDetailsLoading: Boolean = false,
    val selectedMatchDetails: MatchDetailsUi? = null,
    val detailsErrorMessage: String? = null,
    val errorMessage: String? = null
) {
    val isCreateEnabled: Boolean
        get() {
            val hasContextVenue = selectedVenueIdOrFromReservation() != null
            val hasSport = formState.sportType.isNotBlank()
            val hasTime = formState.scheduledAtInput.isNotBlank()
            val players = formState.requiredPlayersInput.toIntOrNull() ?: return false
            return hasContextVenue && hasSport && hasTime && players >= 2 && !isCreatingMatch
        }

    private fun selectedVenueIdOrFromReservation(): String? {
        return formState.selectedVenueId
    }
}

@HiltViewModel
class MatchViewModel @Inject constructor(
    private val getMyMatchesUseCase: GetMyMatchesUseCase,
    private val createMatchUseCase: CreateMatchUseCase,
    private val getMatchDetailsUseCase: GetMatchDetailsUseCase,
    private val getMatchReservationContextsUseCase: GetMatchReservationContextsUseCase,
    private val getVenuesUseCase: GetVenuesUseCase,
    private val scheduleMatchReminderUseCase: ScheduleMatchReminderUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchUiState())
    val uiState: StateFlow<MatchUiState> = _uiState.asStateFlow()

    private var pendingPrefillReservationId: String? = null

    init {
        refreshMatchesModule()
    }

    fun refreshMatchesModule() {
        loadMatches()
        loadReservationContexts()
        loadVenueOptions()
    }

    fun onCreateScreenOpened(prefilledReservationId: String?) {
        pendingPrefillReservationId = prefilledReservationId
        resetCreateForm()
        prefilledReservationId?.let { reservationId ->
            applyReservationContext(reservationId)
        }
    }

    fun onSportTypeChanged(value: String) {
        _uiState.update { state ->
            state.copy(
                formState = state.formState.copy(sportType = value),
                createErrorMessage = null,
                createSuccessMessage = null
            )
        }
    }

    fun onScheduledAtChanged(value: String) {
        _uiState.update { state ->
            state.copy(
                formState = state.formState.copy(scheduledAtInput = value),
                createErrorMessage = null,
                createSuccessMessage = null
            )
        }
    }

    fun onRequiredPlayersChanged(value: String) {
        _uiState.update { state ->
            state.copy(
                formState = state.formState.copy(requiredPlayersInput = value),
                createErrorMessage = null,
                createSuccessMessage = null
            )
        }
    }

    fun onDescriptionChanged(value: String) {
        _uiState.update { state ->
            state.copy(
                formState = state.formState.copy(description = value),
                createErrorMessage = null,
                createSuccessMessage = null
            )
        }
    }

    fun onReservationContextSelected(reservationId: String?) {
        _uiState.update { state ->
            val updated = if (reservationId == null) {
                state.formState.copy(
                    selectedReservationId = null
                )
            } else {
                state.formState.copy(
                    selectedReservationId = reservationId
                )
            }
            state.copy(formState = updated)
        }
        reservationId?.let { applyReservationContext(it) }
    }

    fun onVenueSelected(venueId: String?) {
        _uiState.update { state ->
            state.copy(
                formState = state.formState.copy(
                    selectedVenueId = venueId
                )
            )
        }
    }

    fun createMatch() {
        val state = _uiState.value
        val scheduledStartTime = parseScheduledInput(state.formState.scheduledAtInput)
            ?: run {
                _uiState.update {
                    it.copy(createErrorMessage = "Use schedule format yyyy-MM-dd HH:mm")
                }
                return
            }

        val requiredPlayers = state.formState.requiredPlayersInput.toIntOrNull()
            ?: run {
                _uiState.update { it.copy(createErrorMessage = "Required players must be a number") }
                return
            }

        val selectedReservation = state.formState.selectedReservationId?.let { reservationId ->
            state.reservationContexts.firstOrNull { context -> context.reservationId == reservationId }
        }

        val venueId = selectedReservation?.venueId ?: state.formState.selectedVenueId
        if (venueId.isNullOrBlank()) {
            _uiState.update { it.copy(createErrorMessage = "Select reservation context or venue") }
            return
        }

        if (requiredPlayers < 2) {
            _uiState.update { it.copy(createErrorMessage = "Required players must be at least 2") }
            return
        }

        val sportType = state.formState.sportType.trim()
        if (sportType.isBlank()) {
            _uiState.update { it.copy(createErrorMessage = "Sport type is required") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isCreatingMatch = true,
                    createErrorMessage = null,
                    createSuccessMessage = null
                )
            }

            val payload = MatchCreatePayload(
                sportType = sportType,
                scheduledStartTime = scheduledStartTime,
                requiredPlayers = requiredPlayers,
                description = state.formState.description,
                venueId = venueId,
                reservationId = selectedReservation?.reservationId
            )

            when (val result = createMatchUseCase(payload)) {
                is Resource.Success -> {
                    val reminderResult = scheduleMatchReminderUseCase(
                        matchId = result.data.match.id,
                        scheduledStartTime = result.data.match.scheduledStartTime
                    )
                    val reminderHint = if (reminderResult is Resource.Success) {
                        " Match reminder scheduled."
                    } else {
                        " Match reminder could not be scheduled."
                    }
                    _uiState.update {
                        it.copy(
                            isCreatingMatch = false,
                            latestCreatedMatchId = result.data.match.id,
                            createSuccessMessage = "Match created successfully.$reminderHint",
                            createErrorMessage = null
                        )
                    }
                    loadMatches()
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isCreatingMatch = false,
                            createErrorMessage = result.message
                        )
                    }
                }

                Resource.Loading -> {
                    _uiState.update { it.copy(isCreatingMatch = true) }
                }
            }
        }
    }

    fun loadMatches() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getMyMatchesUseCase()) {
                is Resource.Success -> {
                    val items = result.data.map { match -> match.toListItemUi() }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            matches = items,
                            openMatchesCount = items.count { item ->
                                item.status == MatchStatus.ORGANIZING ||
                                    item.status == MatchStatus.OPEN ||
                                    item.status == MatchStatus.DRAFT
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

    fun loadMatchDetails(matchId: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isDetailsLoading = true,
                    selectedMatchDetails = null,
                    detailsErrorMessage = null
                )
            }

            when (val result = getMatchDetailsUseCase(matchId)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isDetailsLoading = false,
                            selectedMatchDetails = result.data.toDetailsUi()
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isDetailsLoading = false,
                            selectedMatchDetails = null,
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

    fun resetCreateFeedback() {
        _uiState.update {
            it.copy(
                createSuccessMessage = null,
                createErrorMessage = null
            )
        }
    }

    fun buildSelectedMatchVenueLocationPayload(): VenueLocationPayload? {
        val details = _uiState.value.selectedMatchDetails ?: return null
        if (details.venueAddress.isBlank()) {
            return null
        }
        return VenueLocationPayload(
            venueName = details.venueName,
            address = details.venueAddress
        )
    }

    fun buildSelectedMatchCalendarPayload(): CalendarEventPayload? {
        val details = _uiState.value.selectedMatchDetails ?: return null
        val start = details.scheduledStartTime
        val end = start.plus(Duration.ofHours(DEFAULT_CALENDAR_DURATION_HOURS))
        val description = buildString {
            append("Sport: ${details.sportType}\n")
            append("Required players: ${details.requiredPlayers}\n")
            details.description?.takeIf { value -> value.isNotBlank() }?.let { notes ->
                append("Notes: $notes")
            }
        }

        return CalendarEventPayload(
            title = details.title,
            description = description,
            location = details.venueAddress,
            startTime = start,
            endTime = end
        )
    }

    fun buildInvitationSharePayloadForSelectedMatch(): ShareMessagePayload? {
        val details = _uiState.value.selectedMatchDetails ?: return null
        val message = buildString {
            append("Hi team, you are invited to ${details.title} ")
            append("(${details.sportType}) at ${details.venueName} ")
            append("on ${details.scheduledLabel}. ")
            append("Please reply ACCEPT or DECLINE.")
        }
        return ShareMessagePayload(text = message)
    }

    fun buildReminderSharePayloadForSelectedMatch(): ShareMessagePayload? {
        val details = _uiState.value.selectedMatchDetails ?: return null
        val message = buildString {
            append("Reminder: ${details.title} at ${details.venueName}, ${details.scheduledLabel}. ")
            append("Please arrive 20 minutes early and confirm attendance.")
        }
        return ShareMessagePayload(text = message)
    }

    fun buildInvitationEmailPayloadForSelectedMatch(): EmailPayload? {
        val details = _uiState.value.selectedMatchDetails ?: return null
        return EmailPayload(
            subject = "Invitation: ${details.title}",
            body = buildInvitationSharePayloadForSelectedMatch()?.text.orEmpty()
        )
    }

    fun buildReminderEmailPayloadForSelectedMatch(): EmailPayload? {
        val details = _uiState.value.selectedMatchDetails ?: return null
        return EmailPayload(
            subject = "Reminder: ${details.title}",
            body = buildReminderSharePayloadForSelectedMatch()?.text.orEmpty()
        )
    }

    private fun loadReservationContexts() {
        viewModelScope.launch {
            when (val result = getMatchReservationContextsUseCase()) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            reservationContexts = result.data.map { context -> context.toUi() }
                        )
                    }
                    pendingPrefillReservationId?.let { reservationId ->
                        applyReservationContext(reservationId)
                    }
                }

                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }

                Resource.Loading -> Unit
            }
        }
    }

    private fun loadVenueOptions() {
        viewModelScope.launch {
            when (val result = getVenuesUseCase()) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            venueOptions = result.data.map { venueWithSlots ->
                                venueWithSlots.toVenueOptionUi()
                            }
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }

                Resource.Loading -> Unit
            }
        }
    }

    private fun applyReservationContext(reservationId: String) {
        val context = _uiState.value.reservationContexts
            .firstOrNull { item -> item.reservationId == reservationId }
            ?: return

        _uiState.update { state ->
            state.copy(
                formState = state.formState.copy(
                    selectedReservationId = context.reservationId,
                    selectedVenueId = context.venueId,
                    sportType = context.sportType,
                    scheduledAtInput = context.scheduledStartTime
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
                        .format(createInputFormatter)
                )
            )
        }
    }

    private fun resetCreateForm() {
        _uiState.update {
            it.copy(
                formState = MatchCreateFormState(),
                createSuccessMessage = null,
                createErrorMessage = null,
                latestCreatedMatchId = null
            )
        }
    }

    private fun parseScheduledInput(input: String): Instant? {
        val localDateTime = runCatching {
            LocalDateTime.parse(input.trim(), createInputFormatter)
        }.getOrNull() ?: return null
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant()
    }

    private fun MatchWithContext.toListItemUi(): MatchListItemUi =
        MatchListItemUi(
            id = match.id,
            title = match.title,
            sportType = match.sportType,
            venueName = venueName,
            scheduledLabel = formatDisplayDate(match.scheduledStartTime),
            status = match.status,
            statusLabel = match.status.toDisplayLabel(),
            requiredPlayers = match.requiredPlayers,
            invitedPlayersCount = invitedPlayersCount,
            confirmedPlayersCount = confirmedPlayersCount,
            pendingPlayersCount = pendingPlayersCount,
            declinedPlayersCount = declinedPlayersCount,
            remainingSpots = remainingSpots
        )

    private fun MatchWithContext.toDetailsUi(): MatchDetailsUi =
        MatchDetailsUi(
            id = match.id,
            title = match.title,
            sportType = match.sportType,
            venueName = venueName,
            venueAddress = venueAddress,
            scheduledStartTime = match.scheduledStartTime,
            scheduledLabel = formatDisplayDate(match.scheduledStartTime),
            status = match.status,
            statusLabel = match.status.toDisplayLabel(),
            requiredPlayers = match.requiredPlayers,
            invitedPlayersCount = invitedPlayersCount,
            confirmedPlayersCount = confirmedPlayersCount,
            pendingPlayersCount = pendingPlayersCount,
            declinedPlayersCount = declinedPlayersCount,
            remainingSpots = remainingSpots,
            organizerName = organizerName,
            reservationReference = reservationReference,
            description = match.description
        )

    private fun MatchStatus.toDisplayLabel(): String =
        when (this) {
            MatchStatus.ORGANIZING,
            MatchStatus.DRAFT,
            MatchStatus.OPEN -> "Organizing"
            MatchStatus.FULL,
            MatchStatus.CONFIRMED -> "Confirmed"
            MatchStatus.CANCELLED -> "Cancelled"
            MatchStatus.COMPLETED -> "Completed"
        }

    private fun MatchReservationContext.toUi(): MatchReservationContextUi =
        MatchReservationContextUi(
            reservationId = reservationId,
            venueId = venueId,
            venueName = venueName,
            sportType = sportType,
            timeSlotLabel = timeSlotLabel,
            scheduledStartTime = scheduledStartTime,
            displayLabel = "$venueName - $timeSlotLabel"
        )

    private fun VenueWithTimeSlots.toVenueOptionUi(): MatchVenueOptionUi =
        MatchVenueOptionUi(
            venueId = venue.id,
            venueName = venue.name,
            sportType = venue.sportType,
            address = venue.address
        )

    private fun formatDisplayDate(instant: Instant): String =
        instant.atZone(ZoneId.systemDefault()).format(displayFormatter)

    companion object {
        private const val DEFAULT_CALENDAR_DURATION_HOURS: Long = 2
        private val createInputFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        private val displayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm")
    }
}
