package com.dakti.app.testutil

import com.dakti.app.domain.model.AssistantActionExecutionResult
import com.dakti.app.domain.model.AssistantActionProposal
import com.dakti.app.domain.model.AssistantConversationMessage
import com.dakti.app.domain.model.AssistantContext
import com.dakti.app.domain.model.AssistantGeneratedMessage
import com.dakti.app.domain.model.AssistantQuickAction
import com.dakti.app.domain.model.AssistantReply
import com.dakti.app.domain.model.AssistantStructuredRequest
import com.dakti.app.domain.model.AssistantVenueSuggestion
import com.dakti.app.domain.model.InvitationResponseStatus
import com.dakti.app.domain.model.InvitationWithContext
import com.dakti.app.domain.model.InvitePlayerCandidate
import com.dakti.app.domain.model.Match
import com.dakti.app.domain.model.MatchCreatePayload
import com.dakti.app.domain.model.MatchMonitoringResult
import com.dakti.app.domain.model.MatchReservationContext
import com.dakti.app.domain.model.MatchStatus
import com.dakti.app.domain.model.MatchWithContext
import com.dakti.app.domain.model.MatchWithInvitations
import com.dakti.app.domain.model.MonitoringAlert
import com.dakti.app.domain.model.Notification
import com.dakti.app.domain.model.Organizer
import com.dakti.app.domain.model.Player
import com.dakti.app.domain.model.Reservation
import com.dakti.app.domain.model.ReservationDraft
import com.dakti.app.domain.model.ReservationStatus
import com.dakti.app.domain.model.TimeSlot
import com.dakti.app.domain.model.User
import com.dakti.app.domain.model.UserWithProfiles
import com.dakti.app.domain.model.Venue
import com.dakti.app.domain.model.VenueWithTimeSlots
import com.dakti.app.domain.repository.AssistantRepository
import com.dakti.app.domain.repository.AuthRepository
import com.dakti.app.domain.repository.InvitationRepository
import com.dakti.app.domain.repository.MatchRepository
import com.dakti.app.domain.repository.NotificationRepository
import com.dakti.app.domain.repository.ReservationRepository
import com.dakti.app.domain.repository.VenueRepository
import com.dakti.app.util.Resource
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class FakeAuthRepository : AuthRepository {
    private val authenticatedUser = MutableStateFlow<User?>(null)
    var loginResult: Resource<User> = Resource.Error("Invalid credentials")
    var registerResult: Resource<User> = Resource.Error("Registration failed")
    var updateProfileResult: Resource<User> = Resource.Error("Not implemented")

    override suspend fun login(email: String, password: String): Resource<User> {
        if (loginResult is Resource.Success) {
            authenticatedUser.value = (loginResult as Resource.Success<User>).data
        }
        return loginResult
    }

    override suspend fun register(
        name: String,
        email: String,
        phoneNumber: String,
        password: String
    ): Resource<User> {
        if (registerResult is Resource.Success) {
            authenticatedUser.value = (registerResult as Resource.Success<User>).data
        }
        return registerResult
    }

    override suspend fun logout(): Resource<Unit> {
        authenticatedUser.value = null
        return Resource.Success(Unit)
    }

    override fun observeAuthenticatedUser(): Flow<User?> = authenticatedUser

    override suspend fun getAuthenticatedUser(): User? = authenticatedUser.value

    override suspend fun updateCurrentUserProfile(
        displayName: String,
        phoneNumber: String?,
        avatarUrl: String?
    ): Resource<User> = updateProfileResult

    override suspend fun getUserById(userId: String): User? =
        authenticatedUser.value?.takeIf { user -> user.id == userId }

    override fun observeUser(userId: String): Flow<User?> =
        authenticatedUser.map { user -> user?.takeIf { item -> item.id == userId } }

    override suspend fun getUserWithProfiles(userId: String): UserWithProfiles? =
        authenticatedUser.value
            ?.takeIf { user -> user.id == userId }
            ?.let { user -> UserWithProfiles(user = user, organizer = null, player = null) }

    override suspend fun upsertOrganizerProfile(organizer: Organizer): Resource<Unit> =
        Resource.Success(Unit)

    override suspend fun upsertPlayerProfile(player: Player): Resource<Unit> =
        Resource.Success(Unit)
}

class FakeVenueRepository : VenueRepository {
    var venuesWithSlots: List<VenueWithTimeSlots> = listOf(TestData.venueWithSlots())

    override suspend fun getVenues(): Resource<List<Venue>> =
        Resource.Success(venuesWithSlots.map { item -> item.venue })

    override suspend fun getVenueDetails(venueId: String): Resource<Venue> =
        venuesWithSlots.firstOrNull { item -> item.venue.id == venueId }
            ?.venue
            ?.let { venue -> Resource.Success(venue) }
            ?: Resource.Error("Venue not found")

    override suspend fun searchVenues(
        query: String,
        sportType: String?
    ): Resource<List<VenueWithTimeSlots>> {
        val normalizedQuery = query.trim().lowercase()
        val filtered = venuesWithSlots.filter { item ->
            val queryMatches = normalizedQuery.isBlank() ||
                item.venue.name.lowercase().contains(normalizedQuery) ||
                item.venue.address.lowercase().contains(normalizedQuery)
            val sportMatches = sportType.isNullOrBlank() ||
                item.venue.sportType.equals(sportType, ignoreCase = true)
            queryMatches && sportMatches
        }
        return Resource.Success(filtered)
    }

    override suspend fun getVenueWithTimeSlots(venueId: String): Resource<VenueWithTimeSlots> =
        venuesWithSlots.firstOrNull { item -> item.venue.id == venueId }
            ?.let { venue -> Resource.Success(venue) }
            ?: Resource.Error("Venue not found")

    override suspend fun getSportTypes(): Resource<List<String>> =
        Resource.Success(
            venuesWithSlots.map { item -> item.venue.sportType }.distinct().sorted()
        )

    override fun observeVenues(): Flow<List<Venue>> = flowOf(venuesWithSlots.map { item -> item.venue })

    override fun observeVenueWithSlots(venueId: String): Flow<VenueWithTimeSlots?> =
        flowOf(venuesWithSlots.firstOrNull { item -> item.venue.id == venueId })

    override suspend fun upsertVenue(venue: Venue): Resource<Unit> = Resource.Success(Unit)

    override suspend fun upsertTimeSlots(slots: List<TimeSlot>): Resource<Unit> = Resource.Success(Unit)
}

class FakeReservationRepository : ReservationRepository {
    var draftResult: Resource<ReservationDraft> = Resource.Success(TestData.reservationDraft())
    var createResult: Resource<Reservation> = Resource.Success(TestData.reservation())
    var myReservationsResult: Resource<List<Reservation>> = Resource.Success(emptyList())
    var byIdResult: Resource<Reservation> = Resource.Success(TestData.reservation())
    var createCallCount: Int = 0

    override suspend fun getReservationDraft(
        venueId: String,
        timeSlotId: String
    ): Resource<ReservationDraft> = draftResult

    override suspend fun getMyReservations(): Resource<List<Reservation>> = myReservationsResult

    override suspend fun getReservationById(reservationId: String): Resource<Reservation> = byIdResult

    override suspend fun createReservation(
        venueId: String,
        timeSlotId: String,
        note: String?
    ): Resource<Reservation> {
        createCallCount += 1
        return createResult
    }

    override fun observeReservationsByOrganizer(organizerId: String): Flow<List<Reservation>> =
        emptyFlow()

    override suspend fun updateReservationStatus(
        reservationId: String,
        status: ReservationStatus
    ): Resource<Unit> = Resource.Success(Unit)
}

class FakeMatchRepository : MatchRepository {
    var myMatchesResult: Resource<List<MatchWithContext>> =
        Resource.Success(listOf(TestData.matchWithContext()))
    var createMatchResult: Resource<MatchWithContext> =
        Resource.Success(TestData.matchWithContext(id = "match-created"))
    var detailsById: MutableMap<String, MatchWithContext> =
        mutableMapOf("match-1" to TestData.matchWithContext())
    var contextsResult: Resource<List<MatchReservationContext>> =
        Resource.Success(listOf(TestData.matchReservationContext()))
    var createCallCount: Int = 0

    override suspend fun getMyMatches(): Resource<List<MatchWithContext>> = myMatchesResult

    override suspend fun createMatch(payload: MatchCreatePayload): Resource<MatchWithContext> {
        createCallCount += 1
        return createMatchResult
    }

    override suspend fun getMatchDetails(matchId: String): Resource<MatchWithContext> =
        detailsById[matchId]
            ?.let { details -> Resource.Success(details) }
            ?: Resource.Error("Match not found")

    override suspend fun getReservationContextsForCurrentOrganizer(): Resource<List<MatchReservationContext>> =
        contextsResult

    override suspend fun updateMatchStatus(matchId: String, status: MatchStatus): Resource<Unit> =
        Resource.Success(Unit)

    override fun observeMatchesByOrganizer(organizerId: String): Flow<List<Match>> = emptyFlow()

    override fun observeMatchWithInvitations(matchId: String): Flow<MatchWithInvitations?> =
        emptyFlow()

    override suspend fun saveMatch(match: Match): Resource<Match> = Resource.Success(match)
}

class FakeInvitationRepository : InvitationRepository {
    var invitationsForPlayer: List<InvitationWithContext> = listOf(TestData.invitationWithContext())
    var invitationsForMatch: List<InvitationWithContext> = emptyList()
    var inviteCandidates: List<InvitePlayerCandidate> = listOf(TestData.inviteCandidate())
    var inviteResult: Resource<Int> = Resource.Success(1)
    var respondResult: Resource<Unit> = Resource.Success(Unit)
    var invitedMatchIds: MutableList<String> = mutableListOf()

    override suspend fun getInvitationsForCurrentPlayer(): Resource<List<InvitationWithContext>> =
        Resource.Success(invitationsForPlayer)

    override suspend fun getInvitationsForMatch(matchId: String): Resource<List<InvitationWithContext>> =
        Resource.Success(invitationsForMatch)

    override suspend fun getInviteCandidates(matchId: String): Resource<List<InvitePlayerCandidate>> =
        Resource.Success(inviteCandidates)

    override suspend fun invitePlayers(
        matchId: String,
        playerIds: List<String>,
        message: String?
    ): Resource<Int> {
        invitedMatchIds += matchId
        return inviteResult
    }

    override suspend fun respondToInvitation(
        invitationId: String,
        status: InvitationResponseStatus
    ): Resource<Unit> = respondResult
}

class FakeNotificationRepository : NotificationRepository {
    var reservationNotificationResult: Resource<Unit> = Resource.Success(Unit)
    var matchReminderScheduleResult: Resource<Unit> = Resource.Success(Unit)
    var invitationReminderScheduleResult: Resource<Unit> = Resource.Success(Unit)
    var monitoringScheduleResult: Resource<Unit> = Resource.Success(Unit)
    var scheduledMatchReminderIds: MutableList<String> = mutableListOf()
    var scheduledInvitationReminderIds: MutableList<String> = mutableListOf()

    override suspend fun syncNotificationPreferences(): Resource<Unit> = Resource.Success(Unit)

    override fun observeNotifications(userId: String): Flow<List<Notification>> = flowOf(emptyList())

    override suspend fun saveNotification(notification: Notification): Resource<Unit> = Resource.Success(Unit)

    override suspend fun markAsRead(notificationId: String): Resource<Unit> = Resource.Success(Unit)

    override suspend fun sendReservationConfirmationNotification(
        reservation: Reservation
    ): Resource<Unit> = reservationNotificationResult

    override suspend fun sendMatchReminderNotification(matchId: String): Resource<Unit> = Resource.Success(Unit)

    override suspend fun sendInvitationReminderNotification(matchId: String): Resource<Unit> =
        Resource.Success(Unit)

    override suspend fun sendMatchUpdatedNotification(
        matchId: String,
        updateMessage: String?
    ): Resource<Unit> = Resource.Success(Unit)

    override suspend fun sendMatchMonitoringAlertNotification(
        alert: MonitoringAlert
    ): Resource<Unit> = Resource.Success(Unit)

    override suspend fun scheduleMatchReminder(
        matchId: String,
        scheduledStartTime: Instant
    ): Resource<Unit> {
        scheduledMatchReminderIds += matchId
        return matchReminderScheduleResult
    }

    override suspend fun cancelMatchReminder(matchId: String): Resource<Unit> = Resource.Success(Unit)

    override suspend fun scheduleInvitationReminder(matchId: String): Resource<Unit> {
        scheduledInvitationReminderIds += matchId
        return invitationReminderScheduleResult
    }

    override suspend fun cancelInvitationReminder(matchId: String): Resource<Unit> = Resource.Success(Unit)

    override suspend fun scheduleMatchReadinessMonitoring(
        matchId: String,
        scheduledStartTime: Instant
    ): Resource<Unit> = monitoringScheduleResult

    override suspend fun cancelMatchReadinessMonitoring(matchId: String): Resource<Unit> =
        Resource.Success(Unit)
}

class FakeAssistantRepository : AssistantRepository {
    var interpretResult: Resource<AssistantReply> = Resource.Success(TestData.assistantReply())
    var executeResult: Resource<AssistantActionExecutionResult> =
        Resource.Success(TestData.actionExecutionResult())
    var readinessByMatchId: MutableMap<String, MatchMonitoringResult> =
        mutableMapOf("match-1" to TestData.readinessResult())
    var quickActionsData: List<AssistantQuickAction> = listOf(
        AssistantQuickAction(
            id = "quick-organize",
            title = "Organize Match",
            prompt = "Organize a football match for Saturday at 6 PM for 10 players"
        )
    )
    var promptsData: List<String> = listOf("Suggest venues for football")
    var executeCallCount: Int = 0

    override suspend fun interpretAssistantRequest(
        message: String,
        conversationHistory: List<AssistantConversationMessage>,
        context: AssistantContext?
    ): Resource<AssistantReply> = interpretResult

    override suspend fun suggestVenues(
        request: AssistantStructuredRequest
    ): Resource<List<AssistantVenueSuggestion>> =
        Resource.Success(TestData.assistantReply().venueSuggestions)

    override suspend fun suggestAlternativeSlots(
        request: AssistantStructuredRequest
    ): Resource<List<AssistantVenueSuggestion>> =
        Resource.Success(TestData.assistantReply().venueSuggestions)

    override suspend fun organizeMatchFromRequest(
        request: AssistantStructuredRequest
    ): Resource<AssistantReply> = interpretResult

    override suspend fun generateInvitationMessage(
        request: AssistantStructuredRequest
    ): Resource<AssistantGeneratedMessage> =
        Resource.Success(TestData.assistantReply().generatedMessage!!)

    override suspend fun generateReminderMessage(
        request: AssistantStructuredRequest
    ): Resource<AssistantGeneratedMessage> =
        Resource.Success(
            TestData.assistantReply().generatedMessage!!.copy(
                kind = com.dakti.app.domain.model.AssistantGeneratedMessageKind.REMINDER
            )
        )

    override suspend fun executeAssistantAction(
        proposal: AssistantActionProposal
    ): Resource<AssistantActionExecutionResult> {
        executeCallCount += 1
        return executeResult
    }

    override suspend fun evaluateMatchReadiness(
        matchId: String
    ): Resource<MatchMonitoringResult> =
        readinessByMatchId[matchId]
            ?.let { result -> Resource.Success(result) }
            ?: Resource.Error("Match not found")

    override suspend fun evaluateMyMatchReadiness(): Resource<List<MatchMonitoringResult>> =
        Resource.Success(readinessByMatchId.values.toList())

    override suspend fun generateMonitoringReminderMessage(matchId: String): Resource<String> =
        Resource.Success("Please confirm availability.")

    override suspend fun generateMonitoringUpdateMessage(matchId: String): Resource<String> =
        Resource.Success("Match may be rescheduled.")

    override suspend fun monitorMatchAndBuildAlert(matchId: String): Resource<MonitoringAlert?> =
        Resource.Success(null)

    override fun getQuickActions(): List<AssistantQuickAction> = quickActionsData

    override fun getSuggestedPrompts(): List<String> = promptsData
}
