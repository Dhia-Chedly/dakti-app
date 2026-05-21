package com.dakti.app.presentation.invitations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dakti.app.domain.model.AssistantContext
import com.dakti.app.domain.model.AssistantIntent
import com.dakti.app.domain.model.AssistantStructuredRequest
import com.dakti.app.domain.model.InvitationResponseStatus
import com.dakti.app.domain.model.InvitationWithContext
import com.dakti.app.domain.usecase.GenerateInvitationMessageUseCase
import com.dakti.app.domain.usecase.GetInviteCandidatesUseCase
import com.dakti.app.domain.usecase.GetMatchDetailsUseCase
import com.dakti.app.domain.usecase.GetMatchInvitationsUseCase
import com.dakti.app.domain.usecase.GetPlayerInvitationsUseCase
import com.dakti.app.domain.usecase.InvitePlayersUseCase
import com.dakti.app.domain.usecase.RespondToInvitationUseCase
import com.dakti.app.domain.usecase.ScheduleInvitationReminderUseCase
import com.dakti.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InvitationItemUi(
    val invitationId: String,
    val matchId: String,
    val title: String,
    val subtitle: String,
    val scheduledLabel: String,
    val organizerLabel: String,
    val status: InvitationResponseStatus,
    val statusLabel: String,
    val message: String?,
    val sentAtLabel: String,
    val respondedAtLabel: String?,
    val canRespond: Boolean,
    val isResponding: Boolean
)

data class PlayerSelectableItemUi(
    val playerId: String,
    val displayName: String,
    val email: String,
    val preferredSport: String,
    val skillLevel: String?,
    val availabilityNote: String?,
    val isSelected: Boolean,
    val isAlreadyInvited: Boolean,
    val existingStatusLabel: String?
)

data class MatchInvitationItemUi(
    val invitationId: String,
    val playerName: String,
    val status: InvitationResponseStatus,
    val statusLabel: String,
    val sentAtLabel: String
)

data class InvitePlayersUiState(
    val matchId: String? = null,
    val matchTitle: String = "",
    val sportType: String = "",
    val venueName: String = "",
    val scheduledLabel: String = "",
    val requiredPlayers: Int = 0,
    val confirmedPlayersCount: Int = 0,
    val pendingPlayersCount: Int = 0,
    val declinedPlayersCount: Int = 0,
    val remainingSpots: Int = 0,
    val messageInput: String = "",
    val isGeneratingAiMessage: Boolean = false,
    val players: List<PlayerSelectableItemUi> = emptyList(),
    val existingInvitations: List<MatchInvitationItemUi> = emptyList(),
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
) {
    val selectedPlayerIds: List<String>
        get() = players
            .filter { item -> item.isSelected && !item.isAlreadyInvited }
            .map { item -> item.playerId }

    val canSendInvites: Boolean
        get() = !isLoading && !isSending && selectedPlayerIds.isNotEmpty()
}

data class InvitationUiState(
    val isLoading: Boolean = false,
    val invitations: List<InvitationItemUi> = emptyList(),
    val errorMessage: String? = null,
    val actionMessage: String? = null,
    val respondingInvitationIds: Set<String> = emptySet(),
    val invitePlayers: InvitePlayersUiState = InvitePlayersUiState()
)

@HiltViewModel
class InvitationViewModel @Inject constructor(
    private val getPlayerInvitationsUseCase: GetPlayerInvitationsUseCase,
    private val respondToInvitationUseCase: RespondToInvitationUseCase,
    private val getInviteCandidatesUseCase: GetInviteCandidatesUseCase,
    private val invitePlayersUseCase: InvitePlayersUseCase,
    private val getMatchInvitationsUseCase: GetMatchInvitationsUseCase,
    private val getMatchDetailsUseCase: GetMatchDetailsUseCase,
    private val scheduleInvitationReminderUseCase: ScheduleInvitationReminderUseCase,
    private val generateInvitationMessageUseCase: GenerateInvitationMessageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvitationUiState())
    val uiState: StateFlow<InvitationUiState> = _uiState.asStateFlow()

    fun loadPlayerInvitations() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            when (val result = getPlayerInvitationsUseCase()) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            invitations = result.data.map { invitation ->
                                invitation.toInvitationItemUi(
                                    isResponding = invitation.invitationId in it.respondingInvitationIds
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

    fun respondToInvitation(
        invitationId: String,
        accept: Boolean
    ) {
        if (invitationId in _uiState.value.respondingInvitationIds) {
            return
        }

        val nextStatus = if (accept) {
            InvitationResponseStatus.ACCEPTED
        } else {
            InvitationResponseStatus.DECLINED
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    respondingInvitationIds = it.respondingInvitationIds + invitationId,
                    errorMessage = null,
                    actionMessage = null
                )
            }

            when (val result = respondToInvitationUseCase(invitationId, nextStatus)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            respondingInvitationIds = it.respondingInvitationIds - invitationId,
                            actionMessage = if (accept) {
                                "Invitation accepted."
                            } else {
                                "Invitation declined."
                            }
                        )
                    }
                    loadPlayerInvitations()
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            respondingInvitationIds = it.respondingInvitationIds - invitationId,
                            errorMessage = result.message
                        )
                    }
                }

                Resource.Loading -> Unit
            }
        }
    }

    fun loadInvitePlayers(matchId: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    invitePlayers = state.invitePlayers.copy(
                        matchId = matchId,
                        isLoading = true,
                        errorMessage = null,
                        successMessage = null
                    )
                )
            }

            val detailsResult = getMatchDetailsUseCase(matchId)
            val existingInvitationsResult = getMatchInvitationsUseCase(matchId)
            val candidatesResult = getInviteCandidatesUseCase(matchId)

            val details = (detailsResult as? Resource.Success)?.data
            val existingInvitations = (existingInvitationsResult as? Resource.Success)?.data.orEmpty()
            val inviteCandidates = (candidatesResult as? Resource.Success)?.data.orEmpty()
            val failureMessage = listOfNotNull(
                (detailsResult as? Resource.Error)?.message,
                (existingInvitationsResult as? Resource.Error)?.message,
                (candidatesResult as? Resource.Error)?.message
            ).firstOrNull()

            _uiState.update { state ->
                state.copy(
                    invitePlayers = state.invitePlayers.copy(
                        matchId = matchId,
                        matchTitle = details?.match?.title.displayOrNotProvided(),
                        sportType = details?.match?.sportType.displayOrNotProvided(),
                        venueName = details?.venueName.displayOrNotProvided(),
                        scheduledLabel = details?.match?.scheduledStartTime?.formatAsSchedule().orEmpty(),
                        requiredPlayers = details?.match?.requiredPlayers ?: 0,
                        confirmedPlayersCount = details?.confirmedPlayersCount ?: 0,
                        pendingPlayersCount = details?.pendingPlayersCount ?: 0,
                        declinedPlayersCount = details?.declinedPlayersCount ?: 0,
                        remainingSpots = details?.remainingSpots ?: 0,
                        players = inviteCandidates.map { candidate ->
                            PlayerSelectableItemUi(
                                playerId = candidate.playerId,
                                displayName = candidate.displayName.displayOrNotProvided(),
                                email = candidate.email,
                                preferredSport = candidate.preferredSport,
                                skillLevel = candidate.skillLevel,
                                availabilityNote = candidate.availabilityNote,
                                isSelected = false,
                                isAlreadyInvited = candidate.isAlreadyInvited,
                                existingStatusLabel = candidate.invitationStatus?.toDisplayLabel()
                            )
                        },
                        existingInvitations = existingInvitations.map { invitation ->
                            MatchInvitationItemUi(
                                invitationId = invitation.invitationId,
                                playerName = invitation.playerName.displayOrNotProvided(),
                                status = invitation.status,
                                statusLabel = invitation.status.toDisplayLabel(),
                                sentAtLabel = invitation.sentAt.formatAsDateTime()
                            )
                        },
                        isLoading = false,
                        errorMessage = failureMessage
                    )
                )
            }
        }
    }

    fun onInviteMessageChanged(value: String) {
        _uiState.update { state ->
            state.copy(
                invitePlayers = state.invitePlayers.copy(
                    messageInput = value,
                    successMessage = null,
                    errorMessage = null
                )
            )
        }
    }

    fun togglePlayerSelection(playerId: String) {
        _uiState.update { state ->
            state.copy(
                invitePlayers = state.invitePlayers.copy(
                    players = state.invitePlayers.players.map { player ->
                        if (player.playerId != playerId || player.isAlreadyInvited) {
                            player
                        } else {
                            player.copy(isSelected = !player.isSelected)
                        }
                    },
                    successMessage = null,
                    errorMessage = null
                )
            )
        }
    }

    fun sendInvitations() {
        val inviteState = _uiState.value.invitePlayers
        if (inviteState.isSending || inviteState.isGeneratingAiMessage) {
            return
        }

        val matchId = inviteState.matchId
        if (matchId.isNullOrBlank()) {
            _uiState.update { state ->
                state.copy(
                    invitePlayers = state.invitePlayers.copy(
                        errorMessage = "Match context is missing"
                    )
                )
            }
            return
        }

        if (inviteState.selectedPlayerIds.isEmpty()) {
            _uiState.update { state ->
                state.copy(
                    invitePlayers = state.invitePlayers.copy(
                        errorMessage = "Select at least one player"
                    )
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    invitePlayers = state.invitePlayers.copy(
                        isSending = true,
                        successMessage = null,
                        errorMessage = null
                    )
                )
            }

            when (
                val result = invitePlayersUseCase(
                    matchId = matchId,
                    playerIds = inviteState.selectedPlayerIds,
                    message = inviteState.messageInput
                )
            ) {
                is Resource.Success -> {
                    val sentCount = result.data
                    val reminderStatusMessage = if (sentCount > 0) {
                        when (scheduleInvitationReminderUseCase(matchId)) {
                            is Resource.Success -> " Pending-response reminder scheduled."
                            else -> " Pending-response reminder could not be scheduled."
                        }
                    } else {
                        ""
                    }
                    _uiState.update { state ->
                        state.copy(
                            invitePlayers = state.invitePlayers.copy(
                                isSending = false,
                                messageInput = "",
                                successMessage = if (sentCount == 0) {
                                    "Selected players were already invited."
                                } else {
                                    "Sent $sentCount invitation(s)."
                                }
                            ),
                            actionMessage = if (sentCount == 0) {
                                "No new invitations were created."
                            } else {
                                "Invitations sent successfully.$reminderStatusMessage"
                            }
                        )
                    }
                    loadInvitePlayers(matchId)
                }

                is Resource.Error -> {
                    _uiState.update { state ->
                        state.copy(
                            invitePlayers = state.invitePlayers.copy(
                                isSending = false,
                                errorMessage = result.message
                            )
                        )
                    }
                }

                Resource.Loading -> {
                    _uiState.update { state ->
                        state.copy(
                            invitePlayers = state.invitePlayers.copy(isSending = true)
                        )
                    }
                }
            }
        }
    }

    fun generateAiInvitationMessage() {
        val inviteState = _uiState.value.invitePlayers
        val matchId = inviteState.matchId
        if (matchId.isNullOrBlank()) {
            _uiState.update { state ->
                state.copy(
                    invitePlayers = state.invitePlayers.copy(
                        errorMessage = "Match context is missing"
                    )
                )
            }
            return
        }

        if (inviteState.isGeneratingAiMessage || inviteState.isSending) {
            return
        }

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    invitePlayers = state.invitePlayers.copy(
                        isGeneratingAiMessage = true,
                        successMessage = null,
                        errorMessage = null
                    )
                )
            }

            val request = AssistantStructuredRequest(
                rawText = "Generate invitation message for this match",
                intent = AssistantIntent.GENERATE_INVITATION_MESSAGE,
                sportType = inviteState.sportType.takeIf { value -> value.isNotBlank() },
                preferredDateTime = null,
                desiredPlayers = inviteState.requiredPlayers.takeIf { value -> value > 0 },
                venuePreference = inviteState.venueName.takeIf { value -> value.isNotBlank() },
                targetMatchId = matchId,
                context = AssistantContext(
                    sourceRoute = "invite_players",
                    matchId = matchId,
                    reservationId = null,
                    venueId = null
                )
            )

            when (val result = generateInvitationMessageUseCase(request)) {
                is Resource.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            invitePlayers = state.invitePlayers.copy(
                                isGeneratingAiMessage = false,
                                messageInput = result.data.content,
                                successMessage = "AI draft inserted. You can edit before sending."
                            )
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.update { state ->
                        state.copy(
                            invitePlayers = state.invitePlayers.copy(
                                isGeneratingAiMessage = false,
                                errorMessage = result.message
                            )
                        )
                    }
                }

                Resource.Loading -> {
                    _uiState.update { state ->
                        state.copy(
                            invitePlayers = state.invitePlayers.copy(isGeneratingAiMessage = true)
                        )
                    }
                }
            }
        }
    }

    fun clearInvitePlayersFeedback() {
        _uiState.update { state ->
            state.copy(
                invitePlayers = state.invitePlayers.copy(
                    successMessage = null,
                    errorMessage = null
                )
            )
        }
    }

    private fun InvitationWithContext.toInvitationItemUi(
        isResponding: Boolean
    ): InvitationItemUi =
        InvitationItemUi(
            invitationId = invitationId,
            matchId = matchId,
            title = matchTitle.displayOrNotProvided(),
            subtitle = "${sportType.displayOrNotProvided()} - ${venueName.displayOrNotProvided()}",
            scheduledLabel = scheduledStartTime.formatAsSchedule(),
            organizerLabel = "From ${organizerName.displayOrNotProvided()}",
            status = status,
            statusLabel = status.toDisplayLabel(),
            message = message,
            sentAtLabel = sentAt.formatAsDateTime(),
            respondedAtLabel = respondedAt?.formatAsDateTime(),
            canRespond = status == InvitationResponseStatus.PENDING,
            isResponding = isResponding
        )

    private fun InvitationResponseStatus.toDisplayLabel(): String =
        when (this) {
            InvitationResponseStatus.PENDING -> "Pending"
            InvitationResponseStatus.ACCEPTED -> "Accepted"
            InvitationResponseStatus.DECLINED -> "Declined"
            InvitationResponseStatus.EXPIRED -> "Expired"
        }

    private fun Instant.formatAsSchedule(): String =
        atZone(ZoneId.systemDefault()).format(scheduleFormatter)

    private fun Instant.formatAsDateTime(): String =
        atZone(ZoneId.systemDefault()).format(dateTimeFormatter)

    private fun String?.displayOrNotProvided(): String =
        this?.takeIf { value -> value.isNotBlank() } ?: "Not provided"

    companion object {
        private val scheduleFormatter: DateTimeFormatter =
            DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm")
        private val dateTimeFormatter: DateTimeFormatter =
            DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm")
    }
}
