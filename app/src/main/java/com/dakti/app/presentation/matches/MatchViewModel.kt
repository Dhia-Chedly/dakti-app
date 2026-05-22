package com.dakti.app.presentation.matches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dakti.app.domain.model.InvitationResponseStatus
import com.dakti.app.domain.model.InvitationWithContext
import com.dakti.app.domain.model.MatchCreatePayload
import com.dakti.app.domain.model.MatchMonitoringResult
import com.dakti.app.domain.model.MatchReadinessStatus
import com.dakti.app.domain.model.MonitoringSuggestedActionType
import com.dakti.app.domain.model.MatchStatus
import com.dakti.app.domain.model.MatchWithContext
import com.dakti.app.domain.model.VenueWithTimeSlots
import com.dakti.app.domain.repository.AuthRepository
import com.dakti.app.domain.usecase.CreateMatchUseCase
import com.dakti.app.domain.usecase.EvaluateMatchReadinessUseCase
import com.dakti.app.domain.usecase.GetMatchDetailsUseCase
import com.dakti.app.domain.usecase.GetMyMatchesUseCase
import com.dakti.app.domain.usecase.GetPlayerInvitationsUseCase
import com.dakti.app.domain.usecase.GetVenuesUseCase
import com.dakti.app.domain.usecase.ObserveNotificationsUseCase
import com.dakti.app.domain.usecase.RespondToInvitationUseCase
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MatchVenueOptionUi(
    val venueId: String,
    val venueName: String,
    val sportType: String,
    val address: String,
    val location: String,
    val availableSlotsCount: Int
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

data class MonitoringSuggestedActionUi(
    val type: MonitoringSuggestedActionType,
    val title: String,
    val description: String?
)

data class ReschedulingSuggestionUi(
    val suggestionId: String,
    val venueName: String,
    val venueAddress: String,
    val timeSlotLabel: String,
    val reason: String
)

data class MatchReadinessUi(
    val status: MatchReadinessStatus,
    val statusLabel: String,
    val reason: String,
    val summary: String,
    val requiredPlayers: Int,
    val confirmedPlayersCount: Int,
    val pendingPlayersCount: Int,
    val declinedPlayersCount: Int,
    val remainingSpots: Int,
    val minutesUntilMatch: Long,
    val shouldAlertOrganizer: Boolean,
    val reminderMessageText: String?,
    val updateMessageText: String?,
    val suggestedActions: List<MonitoringSuggestedActionUi>,
    val reschedulingSuggestions: List<ReschedulingSuggestionUi>
)

data class MatchCreateFormState(
    val selectedVenueId: String? = null,
    val selectedLocation: String = "",
    val sportType: String = "",
    val scheduledAtInput: String = "",
    val requiredPlayersInput: String = "10",
    val description: String = ""
)

data class MatchUiState(
    val isLoading: Boolean = false,
    val matches: List<MatchListItemUi> = emptyList(),
    val openMatchesCount: Int = 0,
    val selectedTab: MatchesDashboardTab = MatchesDashboardTab.UPCOMING,
    val searchQuery: String = "",
    val showNeedsAttentionOnly: Boolean = false,
    val unreadNotificationCount: Int = 0,
    val upcomingMatches: List<MatchDashboardCardUi> = emptyList(),
    val inviteItems: List<InviteDashboardCardUi> = emptyList(),
    val pastMatches: List<MatchDashboardCardUi> = emptyList(),
    val actionMessage: String? = null,
    val venueOptions: List<MatchVenueOptionUi> = emptyList(),
    val isVenueOptionsLoading: Boolean = false,
    val venueOptionsErrorMessage: String? = null,
    val formState: MatchCreateFormState = MatchCreateFormState(),
    val isCreatingMatch: Boolean = false,
    val createSuccessMessage: String? = null,
    val createErrorMessage: String? = null,
    val latestCreatedMatchId: String? = null,
    val isDetailsLoading: Boolean = false,
    val selectedMatchDetails: MatchDetailsUi? = null,
    val isMonitoringLoading: Boolean = false,
    val selectedMatchReadiness: MatchReadinessUi? = null,
    val monitoringErrorMessage: String? = null,
    val detailsErrorMessage: String? = null,
    val errorMessage: String? = null
) {
    val upcomingCount: Int get() = upcomingMatches.size
    val invitesCount: Int get() = inviteItems.count { item -> item.status == InvitationResponseStatus.PENDING }
    val completedCount: Int get() = pastMatches.count { item -> item.statusLabel.equals("Completed", true) }

    val filteredUpcoming: List<MatchDashboardCardUi>
        get() = upcomingMatches
            .filterMatchCardsBySearch(searchQuery)
            .filter { item -> !showNeedsAttentionOnly || item.needsAttention }

    val filteredInvites: List<InviteDashboardCardUi>
        get() = inviteItems.filterInviteCardsBySearch(searchQuery)

    val filteredPast: List<MatchDashboardCardUi>
        get() = pastMatches.filterMatchCardsBySearch(searchQuery)

    val isCreateEnabled: Boolean
        get() {
            val hasSelectedVenue = formState.selectedVenueId != null
            val hasSport = formState.sportType.isNotBlank()
            val hasTime = formState.scheduledAtInput.isNotBlank()
            val players = formState.requiredPlayersInput.toIntOrNull() ?: return false
            return hasSelectedVenue && hasSport && hasTime && players >= 2 && !isCreatingMatch
        }
}

@HiltViewModel
class MatchViewModel @Inject constructor(
    private val getMyMatchesUseCase: GetMyMatchesUseCase,
    private val getPlayerInvitationsUseCase: GetPlayerInvitationsUseCase,
    private val respondToInvitationUseCase: RespondToInvitationUseCase,
    private val observeNotificationsUseCase: ObserveNotificationsUseCase,
    private val authRepository: AuthRepository,
    private val createMatchUseCase: CreateMatchUseCase,
    private val getMatchDetailsUseCase: GetMatchDetailsUseCase,
    private val getVenuesUseCase: GetVenuesUseCase,
    private val scheduleMatchReminderUseCase: ScheduleMatchReminderUseCase,
    private val evaluateMatchReadinessUseCase: EvaluateMatchReadinessUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchUiState())
    val uiState: StateFlow<MatchUiState> = _uiState.asStateFlow()

    private var notificationsJob: Job? = null
    private var respondingInvitationIds: Set<String> = emptySet()

    init {
        observeUnreadNotifications()
        refreshMatchesModule()
    }

    fun refreshMatchesModule() {
        refreshDashboard()
        refreshCreateDependencies()
    }

    fun refreshDashboard() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    actionMessage = null
                )
            }

            val matchesResult = getMyMatchesUseCase()
            val invitesResult = getPlayerInvitationsUseCase()

            val errors = mutableListOf<String>()
            val matches = when (matchesResult) {
                is Resource.Success -> matchesResult.data
                is Resource.Error -> {
                    errors += matchesResult.message
                    emptyList()
                }
                Resource.Loading -> emptyList()
            }
            val invites = when (invitesResult) {
                is Resource.Success -> invitesResult.data
                is Resource.Error -> {
                    errors += invitesResult.message
                    emptyList()
                }
                Resource.Loading -> emptyList()
            }

            val matchItems = matches.map { match -> match.toListItemUi() }
            val partitioned = partitionMatches(matches)
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    matches = matchItems,
                    openMatchesCount = matchItems.count { item ->
                        item.status == MatchStatus.ORGANIZING ||
                            item.status == MatchStatus.OPEN ||
                            item.status == MatchStatus.DRAFT
                    },
                    upcomingMatches = partitioned.upcoming.map { item -> item.toDashboardCardUi() },
                    pastMatches = partitioned.past.map { item -> item.toDashboardCardUi() },
                    inviteItems = invites
                        .sortedBy { item -> item.scheduledStartTime }
                        .map { item ->
                            item.toInviteDashboardCardUi(
                                isResponding = item.invitationId in respondingInvitationIds
                            )
                        },
                    errorMessage = errors.firstOrNull()
                )
            }
        }
    }

    fun refresh() {
        refreshDashboard()
    }

    fun refreshCreateDependencies() {
        loadVenueOptions()
    }

    fun setInitialTab(tab: MatchesDashboardTab) {
        _uiState.update { state -> state.copy(selectedTab = tab) }
    }

    fun onTabSelected(tab: MatchesDashboardTab) {
        _uiState.update { state -> state.copy(selectedTab = tab) }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state -> state.copy(searchQuery = query) }
    }

    fun onNeedsAttentionFilterToggled() {
        _uiState.update { state ->
            state.copy(showNeedsAttentionOnly = !state.showNeedsAttentionOnly)
        }
    }

    fun respondToInvitation(
        invitationId: String,
        accept: Boolean
    ) {
        if (invitationId in respondingInvitationIds) {
            return
        }

        val nextStatus = if (accept) {
            InvitationResponseStatus.ACCEPTED
        } else {
            InvitationResponseStatus.DECLINED
        }

        viewModelScope.launch {
            respondingInvitationIds = respondingInvitationIds + invitationId
            _uiState.update { state ->
                state.copy(
                    inviteItems = state.inviteItems.map { item ->
                        if (item.invitationId == invitationId) {
                            item.copy(isResponding = true)
                        } else {
                            item
                        }
                    },
                    actionMessage = null,
                    errorMessage = null
                )
            }

            when (val result = respondToInvitationUseCase(invitationId, nextStatus)) {
                is Resource.Success -> {
                    respondingInvitationIds = respondingInvitationIds - invitationId
                    _uiState.update { state ->
                        state.copy(
                            actionMessage = if (accept) {
                                "Invitation accepted."
                            } else {
                                "Invitation declined."
                            }
                        )
                    }
                    refreshDashboard()
                }

                is Resource.Error -> {
                    respondingInvitationIds = respondingInvitationIds - invitationId
                    _uiState.update { state ->
                        state.copy(errorMessage = result.message)
                    }
                    refreshDashboard()
                }

                Resource.Loading -> Unit
            }
        }
    }

    fun onCreateScreenOpened() {
        resetCreateForm()
        _uiState.update { state -> state.copy(venueOptionsErrorMessage = null) }
        refreshCreateDependencies()
    }

    fun onSportTypeChanged(value: String) {
        _uiState.update { state ->
            val selectedVenueId = state.formState.selectedVenueId
                ?.takeIf { venueId ->
                    state.venueOptions
                        .firstOrNull { venue -> venue.venueId == venueId }
                        ?.sportType == value
                }
            state.copy(
                formState = state.formState.copy(
                    selectedVenueId = selectedVenueId,
                    selectedLocation = selectedVenueId?.let { venueId ->
                        state.venueOptions.firstOrNull { venue -> venue.venueId == venueId }?.location
                    }.orEmpty(),
                    sportType = value
                ),
                createErrorMessage = null,
                createSuccessMessage = null
            )
        }
    }

    fun onLocationChanged(value: String) {
        _uiState.update { state ->
            val selectedVenueId = state.formState.selectedVenueId
                ?.takeIf { venueId ->
                    state.venueOptions
                        .firstOrNull { venue -> venue.venueId == venueId }
                        ?.location == value
                }
            state.copy(
                formState = state.formState.copy(
                    selectedLocation = value,
                    selectedVenueId = selectedVenueId
                ),
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

    fun onVenueSelected(venueId: String?) {
        _uiState.update { state ->
            val venue = state.venueOptions.firstOrNull { item -> item.venueId == venueId }
            state.copy(
                formState = state.formState.copy(
                    selectedVenueId = venueId,
                    selectedLocation = venue?.location ?: state.formState.selectedLocation,
                    sportType = venue?.sportType ?: state.formState.sportType
                ),
                createErrorMessage = null,
                createSuccessMessage = null
            )
        }
    }

    fun createMatch() {
        if (_uiState.value.isCreatingMatch) {
            return
        }

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

        val venueId = state.formState.selectedVenueId
        if (venueId.isNullOrBlank()) {
            _uiState.update { it.copy(createErrorMessage = MISSING_VENUE_ERROR) }
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
                reservationId = null
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
                    refreshDashboard()
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
        refreshDashboard()
    }

    fun loadMatchDetails(matchId: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isDetailsLoading = true,
                    selectedMatchDetails = null,
                    selectedMatchReadiness = null,
                    isMonitoringLoading = true,
                    monitoringErrorMessage = null,
                    detailsErrorMessage = null
                )
            }

            when (val result = getMatchDetailsUseCase(matchId)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isDetailsLoading = false,
                            selectedMatchDetails = result.data.toDetailsUi(),
                            detailsErrorMessage = null
                        )
                    }
                    evaluateReadiness(matchId)
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isDetailsLoading = false,
                            selectedMatchDetails = null,
                            selectedMatchReadiness = null,
                            isMonitoringLoading = false,
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

    fun refreshMatchReadiness(matchId: String) {
        evaluateReadiness(matchId)
    }

    private fun observeUnreadNotifications() {
        viewModelScope.launch {
            authRepository.observeAuthenticatedUser().collect { user ->
                notificationsJob?.cancel()
                if (user == null) {
                    _uiState.update { state -> state.copy(unreadNotificationCount = 0) }
                } else {
                    notificationsJob = launch {
                        observeNotificationsUseCase(user.id).collect { notifications ->
                            _uiState.update { state ->
                                state.copy(
                                    unreadNotificationCount = notifications.count { notification ->
                                        !notification.isRead
                                    }
                                )
                            }
                        }
                    }
                }
            }
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

    fun buildMonitoringReminderSharePayloadForSelectedMatch(): ShareMessagePayload? {
        val text = _uiState.value.selectedMatchReadiness?.reminderMessageText
            ?.trim()
            ?.takeIf { value -> value.isNotBlank() }
            ?: return null
        return ShareMessagePayload(text = text)
    }

    fun buildMonitoringUpdateSharePayloadForSelectedMatch(): ShareMessagePayload? {
        val text = _uiState.value.selectedMatchReadiness?.updateMessageText
            ?.trim()
            ?.takeIf { value -> value.isNotBlank() }
            ?: return null
        return ShareMessagePayload(text = text)
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

    fun buildMonitoringReminderEmailPayloadForSelectedMatch(): EmailPayload? {
        val details = _uiState.value.selectedMatchDetails ?: return null
        val payload = buildMonitoringReminderSharePayloadForSelectedMatch() ?: return null
        return EmailPayload(
            subject = "Reminder: ${details.title}",
            body = payload.text
        )
    }

    fun buildMonitoringUpdateEmailPayloadForSelectedMatch(): EmailPayload? {
        val details = _uiState.value.selectedMatchDetails ?: return null
        val payload = buildMonitoringUpdateSharePayloadForSelectedMatch() ?: return null
        return EmailPayload(
            subject = "Match Update: ${details.title}",
            body = payload.text
        )
    }

    private fun loadVenueOptions() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isVenueOptionsLoading = true,
                    venueOptionsErrorMessage = null
                )
            }
            when (val result = getVenuesUseCase()) {
                is Resource.Success -> {
                    _uiState.update { state ->
                        val venueOptions = result.data.map { venueWithSlots ->
                            venueWithSlots.toVenueOptionUi()
                        }
                        val selectedVenue = state.formState.selectedVenueId?.let { venueId ->
                            result.data.firstOrNull { item -> item.venue.id == venueId }
                        }?.toVenueOptionUi()
                        val shouldClearVenueDependencyError =
                            state.createErrorMessage == MISSING_VENUE_ERROR && venueOptions.isNotEmpty()
                        state.copy(
                            venueOptions = venueOptions,
                            isVenueOptionsLoading = false,
                            venueOptionsErrorMessage = null,
                            formState = state.formState.copy(
                                selectedLocation = selectedVenue?.location ?: state.formState.selectedLocation
                            ),
                            createErrorMessage = if (shouldClearVenueDependencyError) {
                                null
                            } else {
                                state.createErrorMessage
                            }
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isVenueOptionsLoading = false,
                            venueOptionsErrorMessage = result.message
                        )
                    }
                }

                Resource.Loading -> {
                    _uiState.update { it.copy(isVenueOptionsLoading = true) }
                }
            }
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

    private fun evaluateReadiness(matchId: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isMonitoringLoading = true,
                    monitoringErrorMessage = null
                )
            }

            when (val result = evaluateMatchReadinessUseCase(matchId)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isMonitoringLoading = false,
                            selectedMatchReadiness = result.data.toUi(),
                            monitoringErrorMessage = null
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isMonitoringLoading = false,
                            selectedMatchReadiness = null,
                            monitoringErrorMessage = result.message
                        )
                    }
                }

                Resource.Loading -> {
                    _uiState.update { it.copy(isMonitoringLoading = true) }
                }
            }
        }
    }

    private fun partitionMatches(matches: List<MatchWithContext>): MatchPartition {
        val now = Instant.now()
        val upcoming = mutableListOf<MatchWithContext>()
        val past = mutableListOf<MatchWithContext>()
        matches.forEach { item ->
            val isPastByStatus = item.match.status == MatchStatus.COMPLETED || item.match.status == MatchStatus.CANCELLED
            val isPastByTime = item.match.scheduledStartTime.isBefore(now)
            if (isPastByStatus || isPastByTime) {
                past += item
            } else {
                upcoming += item
            }
        }
        return MatchPartition(
            upcoming = upcoming.sortedBy { item -> item.match.scheduledStartTime },
            past = past.sortedByDescending { item -> item.match.scheduledStartTime }
        )
    }

    private fun MatchWithContext.toDashboardCardUi(): MatchDashboardCardUi {
        val needsAttention = pendingPlayersCount > 0 ||
            remainingSpots > 0 ||
            match.status in setOf(MatchStatus.ORGANIZING, MatchStatus.DRAFT, MatchStatus.OPEN)
        return MatchDashboardCardUi(
            id = match.id,
            title = match.title,
            sportType = match.sportType,
            venueName = venueName,
            scheduledLabel = match.scheduledStartTime
                .atZone(ZoneId.systemDefault())
                .format(displayFormatter),
            statusLabel = match.status.toDashboardStatusLabel(),
            statusTone = match.status.toStatusTone(),
            requiredPlayers = match.requiredPlayers,
            confirmedPlayersCount = confirmedPlayersCount,
            pendingPlayersCount = pendingPlayersCount,
            remainingSpots = remainingSpots,
            needsAttention = needsAttention,
            actionLabel = if (needsAttention) "Manage" else "View Details"
        )
    }

    private fun InvitationWithContext.toInviteDashboardCardUi(
        isResponding: Boolean
    ): InviteDashboardCardUi {
        return InviteDashboardCardUi(
            invitationId = invitationId,
            matchId = matchId,
            title = matchTitle,
            sportType = sportType,
            venueName = venueName,
            scheduledLabel = scheduledStartTime
                .atZone(ZoneId.systemDefault())
                .format(displayFormatter),
            status = status,
            statusLabel = status.toDisplayLabel(),
            canRespond = status == InvitationResponseStatus.PENDING,
            isResponding = isResponding
        )
    }

    private fun MatchWithContext.toListItemUi(): MatchListItemUi =
        MatchListItemUi(
            id = match.id,
            title = match.title.displayOrNotProvided(),
            sportType = match.sportType,
            venueName = venueName.displayOrNotProvided(),
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
            title = match.title.displayOrNotProvided(),
            sportType = match.sportType,
            venueName = venueName.displayOrNotProvided(),
            venueAddress = venueAddress.displayOrNotProvided(),
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

    private fun MatchMonitoringResult.toUi(): MatchReadinessUi =
        MatchReadinessUi(
            status = status,
            statusLabel = status.toDisplayLabel(),
            reason = reason,
            summary = summary,
            requiredPlayers = requiredPlayers,
            confirmedPlayersCount = confirmedPlayersCount,
            pendingPlayersCount = pendingPlayersCount,
            declinedPlayersCount = declinedPlayersCount,
            remainingSpots = remainingSpots,
            minutesUntilMatch = minutesUntilMatch,
            shouldAlertOrganizer = shouldAlertOrganizer,
            reminderMessageText = reminderMessageText,
            updateMessageText = updateMessageText,
            suggestedActions = suggestedActions.map { action ->
                MonitoringSuggestedActionUi(
                    type = action.type,
                    title = action.title,
                    description = action.description
                )
            },
            reschedulingSuggestions = reschedulingSuggestions.map { suggestion ->
                ReschedulingSuggestionUi(
                    suggestionId = suggestion.id,
                    venueName = suggestion.venueName,
                    venueAddress = suggestion.venueAddress,
                    timeSlotLabel = suggestion.timeSlotLabel,
                    reason = suggestion.reason
                )
            }
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

    private fun MatchStatus.toDashboardStatusLabel(): String {
        return when (this) {
            MatchStatus.ORGANIZING,
            MatchStatus.DRAFT,
            MatchStatus.OPEN -> "Upcoming"
            MatchStatus.CONFIRMED,
            MatchStatus.FULL -> "Confirmed"
            MatchStatus.CANCELLED -> "Cancelled"
            MatchStatus.COMPLETED -> "Completed"
        }
    }

    private fun MatchStatus.toStatusTone(): MatchesDashboardStatusTone {
        return when (this) {
            MatchStatus.CONFIRMED,
            MatchStatus.FULL,
            MatchStatus.COMPLETED -> MatchesDashboardStatusTone.POSITIVE
            MatchStatus.CANCELLED -> MatchesDashboardStatusTone.DANGER
            MatchStatus.ORGANIZING,
            MatchStatus.DRAFT,
            MatchStatus.OPEN -> MatchesDashboardStatusTone.WARNING
        }
    }

    private fun InvitationResponseStatus.toDisplayLabel(): String {
        return when (this) {
            InvitationResponseStatus.PENDING -> "Pending"
            InvitationResponseStatus.ACCEPTED -> "Accepted"
            InvitationResponseStatus.DECLINED -> "Declined"
            InvitationResponseStatus.EXPIRED -> "Expired"
        }
    }

    private fun MatchReadinessStatus.toDisplayLabel(): String =
        when (this) {
            MatchReadinessStatus.READY -> "Ready"
            MatchReadinessStatus.AT_RISK -> "At Risk"
            MatchReadinessStatus.INSUFFICIENT_PLAYERS -> "Insufficient Players"
            MatchReadinessStatus.NEEDS_ORGANIZER_ACTION -> "Needs Action"
        }

    private fun VenueWithTimeSlots.toVenueOptionUi(): MatchVenueOptionUi =
        MatchVenueOptionUi(
            venueId = venue.id,
            venueName = venue.name,
            sportType = venue.sportType,
            address = venue.address,
            location = parseLocationFromAddress(venue.address),
            availableSlotsCount = slots.count { slot -> slot.isAvailable }
        )

    private fun formatDisplayDate(instant: Instant): String =
        instant.atZone(ZoneId.systemDefault()).format(displayFormatter)

    private data class MatchPartition(
        val upcoming: List<MatchWithContext>,
        val past: List<MatchWithContext>
    )

    companion object {
        private const val DEFAULT_CALENDAR_DURATION_HOURS: Long = 2
        private const val MISSING_VENUE_ERROR = "Select venue"
        private val createInputFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        private val displayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm")

        private fun parseLocationFromAddress(address: String): String {
            val cleaned = address.trim()
            if (cleaned.isBlank()) return "Not provided"
            val segments = cleaned.split(",")
                .map { segment -> segment.trim() }
                .filter { segment -> segment.isNotBlank() }
            return segments.lastOrNull() ?: cleaned
        }
    }
}

private fun List<MatchDashboardCardUi>.filterMatchCardsBySearch(query: String): List<MatchDashboardCardUi> {
    val normalized = query.trim().lowercase()
    if (normalized.isBlank()) return this
    return filter { item ->
        item.title.lowercase().contains(normalized) ||
            item.sportType.lowercase().contains(normalized) ||
            item.venueName.lowercase().contains(normalized) ||
            item.statusLabel.lowercase().contains(normalized)
    }
}

private fun List<InviteDashboardCardUi>.filterInviteCardsBySearch(query: String): List<InviteDashboardCardUi> {
    val normalized = query.trim().lowercase()
    if (normalized.isBlank()) return this
    return filter { item ->
        item.title.lowercase().contains(normalized) ||
            item.sportType.lowercase().contains(normalized) ||
            item.venueName.lowercase().contains(normalized) ||
            item.statusLabel.lowercase().contains(normalized)
    }
}

private fun String?.displayOrNotProvided(): String =
    this?.takeIf { value -> value.isNotBlank() } ?: "Not provided"
