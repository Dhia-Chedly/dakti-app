package com.dakti.app.testutil

import com.dakti.app.domain.model.AISuggestionType
import com.dakti.app.domain.model.AssistantActionExecutionResult
import com.dakti.app.domain.model.AssistantActionProposal
import com.dakti.app.domain.model.AssistantActionType
import com.dakti.app.domain.model.AssistantGeneratedMessage
import com.dakti.app.domain.model.AssistantGeneratedMessageKind
import com.dakti.app.domain.model.AssistantIntent
import com.dakti.app.domain.model.AssistantQuickAction
import com.dakti.app.domain.model.AssistantReply
import com.dakti.app.domain.model.AssistantStructuredRequest
import com.dakti.app.domain.model.AssistantSuggestionItem
import com.dakti.app.domain.model.AssistantVenueSuggestion
import com.dakti.app.domain.model.InvitationResponseStatus
import com.dakti.app.domain.model.InvitationWithContext
import com.dakti.app.domain.model.InvitePlayerCandidate
import com.dakti.app.domain.model.Match
import com.dakti.app.domain.model.MatchMonitoringResult
import com.dakti.app.domain.model.MatchReadinessStatus
import com.dakti.app.domain.model.MatchReservationContext
import com.dakti.app.domain.model.MatchStatus
import com.dakti.app.domain.model.MatchWithContext
import com.dakti.app.domain.model.MonitoringSuggestedActionType
import com.dakti.app.domain.model.Reservation
import com.dakti.app.domain.model.ReservationDraft
import com.dakti.app.domain.model.ReservationStatus
import com.dakti.app.domain.model.ReschedulingSuggestion
import com.dakti.app.domain.model.SuggestedAction
import com.dakti.app.domain.model.TimeSlot
import com.dakti.app.domain.model.User
import com.dakti.app.domain.model.UserRole
import com.dakti.app.domain.model.Venue
import com.dakti.app.domain.model.VenueWithTimeSlots
import java.time.Instant

object TestData {
    val now: Instant = Instant.parse("2026-04-21T10:00:00Z")
    val plusTwoHours: Instant = now.plusSeconds(2 * 3600)
    val plusOneDay: Instant = now.plusSeconds(24 * 3600)

    fun user(
        id: String = "user-1",
        name: String = "Demo User",
        email: String = "demo@dakti.app",
        role: UserRole = UserRole.BOTH
    ): User = User(
        id = id,
        displayName = name,
        email = email,
        phoneNumber = "+2348011111111",
        avatarUrl = null,
        role = role,
        bio = null,
        createdAt = now,
        updatedAt = now
    )

    fun venueWithSlots(
        venueId: String = "venue-1",
        sportType: String = "Football",
        available: Boolean = true
    ): VenueWithTimeSlots {
        val venue = Venue(
            id = venueId,
            name = "Dakti Arena",
            sportType = sportType,
            description = "Main field",
            address = "1 Stadium Road, Lagos",
            contactPhone = "+2348010000000",
            imageUrl = null,
            city = "Lagos",
            state = "Lagos",
            country = "Nigeria",
            latitude = 6.5244,
            longitude = 3.3792,
            pricePerHour = 12000.0,
            currency = "NGN",
            amenities = listOf("Parking", "Lights"),
            createdAt = now,
            updatedAt = now
        )
        val slots = listOf(
            TimeSlot(
                id = "slot-1",
                venueId = venueId,
                startTime = plusTwoHours,
                endTime = plusTwoHours.plusSeconds(3600),
                isAvailable = available,
                capacity = 14
            )
        )
        return VenueWithTimeSlots(venue = venue, slots = slots)
    }

    fun reservationDraft(
        venueId: String = "venue-1",
        slotId: String = "slot-1",
        available: Boolean = true
    ): ReservationDraft = ReservationDraft(
        organizerId = "organizer-1",
        venueId = venueId,
        venueName = "Dakti Arena",
        venueAddress = "1 Stadium Road, Lagos",
        venueSportType = "Football",
        timeSlotId = slotId,
        timeSlotLabel = "Tue, 21 Apr 18:00 - 19:00",
        totalPrice = 12000.0,
        currency = "NGN",
        isSlotAvailable = available
    )

    fun reservation(
        id: String = "res-1",
        slotLabel: String = "Tue, 21 Apr 18:00 - 19:00"
    ): Reservation = Reservation(
        id = id,
        organizerId = "organizer-1",
        venueId = "venue-1",
        timeSlotId = "slot-1",
        venueName = "Dakti Arena",
        timeSlot = slotLabel,
        status = ReservationStatus.CONFIRMED,
        totalPrice = 12000.0,
        currency = "NGN",
        note = null,
        createdAt = now,
        updatedAt = now
    )

    fun matchWithContext(
        id: String = "match-1",
        status: MatchStatus = MatchStatus.ORGANIZING,
        requiredPlayers: Int = 10,
        confirmedPlayers: Int = 4,
        pendingPlayers: Int = 3,
        declinedPlayers: Int = 1
    ): MatchWithContext = MatchWithContext(
        match = Match(
            id = id,
            organizerId = "organizer-1",
            venueId = "venue-1",
            reservationId = "res-1",
            title = "Football Match",
            sportType = "Football",
            scheduledStartTime = plusOneDay,
            requiredPlayers = requiredPlayers,
            status = status,
            description = "Friendly game",
            createdAt = now,
            updatedAt = now
        ),
        venueName = "Dakti Arena",
        venueAddress = "1 Stadium Road, Lagos",
        reservationReference = "res-1",
        organizerName = "Organizer",
        invitedPlayersCount = confirmedPlayers + pendingPlayers + declinedPlayers,
        confirmedPlayersCount = confirmedPlayers,
        pendingPlayersCount = pendingPlayers,
        declinedPlayersCount = declinedPlayers
    )

    fun matchReservationContext(
        reservationId: String = "res-1"
    ): MatchReservationContext = MatchReservationContext(
        reservationId = reservationId,
        venueId = "venue-1",
        venueName = "Dakti Arena",
        venueAddress = "1 Stadium Road, Lagos",
        sportType = "Football",
        scheduledStartTime = plusOneDay,
        timeSlotLabel = "Wed, 22 Apr 18:00 - 19:00"
    )

    fun invitationWithContext(
        id: String = "inv-1",
        status: InvitationResponseStatus = InvitationResponseStatus.PENDING
    ): InvitationWithContext = InvitationWithContext(
        invitationId = id,
        matchId = "match-1",
        playerId = "player-1",
        playerName = "Player One",
        organizerId = "organizer-1",
        organizerName = "Organizer",
        matchTitle = "Football Match",
        sportType = "Football",
        venueName = "Dakti Arena",
        venueAddress = "1 Stadium Road, Lagos",
        scheduledStartTime = plusOneDay,
        requiredPlayers = 10,
        status = status,
        message = "Come play",
        sentAt = now,
        respondedAt = if (status == InvitationResponseStatus.PENDING) null else now
    )

    fun inviteCandidate(
        playerId: String = "player-1",
        alreadyInvited: InvitationResponseStatus? = null
    ): InvitePlayerCandidate = InvitePlayerCandidate(
        playerId = playerId,
        displayName = "Player One",
        email = "player1@dakti.app",
        phoneNumber = "+2348012222222",
        preferredSport = "Football",
        availabilityNote = "Evenings",
        skillLevel = "Intermediate",
        invitationStatus = alreadyInvited
    )

    fun assistantReply(
        text: String = "I found options for you.",
        withProposal: Boolean = true
    ): AssistantReply = AssistantReply(
        text = text,
        intent = AssistantIntent.ORGANIZE_MATCH,
        parsedRequest = AssistantStructuredRequest(
            rawText = "Organize match",
            intent = AssistantIntent.ORGANIZE_MATCH,
            sportType = "Football",
            preferredDateTime = plusOneDay,
            desiredPlayers = 10,
            venuePreference = null,
            targetMatchId = null,
            context = null
        ),
        suggestions = listOf(
            AssistantSuggestionItem(
                id = "sg-1",
                type = AISuggestionType.VENUE_RECOMMENDATION,
                title = "Dakti Arena",
                description = "Best fit"
            )
        ),
        venueSuggestions = listOf(
            AssistantVenueSuggestion(
                venueId = "venue-1",
                venueName = "Dakti Arena",
                venueAddress = "1 Stadium Road, Lagos",
                sportType = "Football",
                timeSlotId = "slot-1",
                timeSlotLabel = "Wed, 22 Apr 18:00 - 19:00",
                startTime = plusOneDay,
                endTime = plusOneDay.plusSeconds(3600),
                slotCapacity = 14,
                isPreferredTime = true,
                reason = "Good match"
            )
        ),
        generatedMessage = AssistantGeneratedMessage(
            kind = AssistantGeneratedMessageKind.INVITATION,
            title = "Invitation Draft",
            content = "Join our match this Saturday.",
            variants = emptyList()
        ),
        actionProposal = if (withProposal) {
            AssistantActionProposal(
                id = "proposal-1",
                type = AssistantActionType.CREATE_RESERVATION_AND_MATCH,
                title = "Create Reservation and Match",
                summary = "Create both records.",
                requiresConfirmation = true,
                venueId = "venue-1",
                timeSlotId = "slot-1",
                sportType = "Football",
                requiredPlayers = 10,
                scheduledStartTime = plusOneDay,
                reservationId = null,
                description = "From assistant"
            )
        } else {
            null
        },
        quickActions = listOf(
            AssistantQuickAction(
                id = "qa-1",
                title = "Organize Match",
                prompt = "Organize a football match"
            )
        ),
        providerLabel = "Test Assistant",
        usedFallback = true
    )

    fun actionExecutionResult(
        success: Boolean = true
    ): AssistantActionExecutionResult = AssistantActionExecutionResult(
        success = success,
        message = if (success) "Created successfully." else "Failed.",
        createdReservationId = if (success) "res-99" else null,
        createdMatchId = if (success) "match-99" else null
    )

    fun readinessResult(
        status: MatchReadinessStatus = MatchReadinessStatus.AT_RISK
    ): MatchMonitoringResult = MatchMonitoringResult(
        matchId = "match-1",
        matchTitle = "Football Match",
        sportType = "Football",
        venueName = "Dakti Arena",
        scheduledStartTime = plusOneDay,
        status = status,
        reason = "Players are below required count.",
        summary = "4 confirmed, 3 pending, 10 required.",
        requiredPlayers = 10,
        invitedPlayersCount = 7,
        confirmedPlayersCount = 4,
        pendingPlayersCount = 3,
        declinedPlayersCount = 0,
        remainingSpots = 6,
        minutesUntilMatch = 120,
        shouldAlertOrganizer = status != MatchReadinessStatus.READY,
        suggestedActions = listOf(
            SuggestedAction(
                id = "action-1",
                type = MonitoringSuggestedActionType.REMIND_PENDING_PLAYERS,
                title = "Remind Pending Players",
                description = "Send quick reminders."
            )
        ),
        reschedulingSuggestions = listOf(
            ReschedulingSuggestion(
                id = "alt-1",
                venueId = "venue-1",
                venueName = "Dakti Arena",
                venueAddress = "1 Stadium Road, Lagos",
                timeSlotId = "slot-2",
                timeSlotLabel = "Thu, 23 Apr 18:00 - 19:00",
                startTime = plusOneDay.plusSeconds(86400),
                endTime = plusOneDay.plusSeconds(90000),
                reason = "More likely to fill."
            )
        ),
        reminderMessageText = "Please confirm your attendance.",
        updateMessageText = "Match may be rescheduled."
    )
}
