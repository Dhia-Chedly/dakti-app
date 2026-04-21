package com.dakti.app.ai.service

import com.dakti.app.ai.parser.AssistantIntentParser
import com.dakti.app.ai.parser.AssistantResponseParser
import com.dakti.app.ai.prompt.PromptBuilder
import com.dakti.app.ai.suggestion.SuggestionEngine
import com.dakti.app.domain.model.AISuggestionType
import com.dakti.app.domain.model.AssistantActionExecutionResult
import com.dakti.app.domain.model.AssistantActionProposal
import com.dakti.app.domain.model.AssistantActionType
import com.dakti.app.domain.model.AssistantContext
import com.dakti.app.domain.model.AssistantConversationMessage
import com.dakti.app.domain.model.AssistantGeneratedMessage
import com.dakti.app.domain.model.AssistantGeneratedMessageKind
import com.dakti.app.domain.model.AssistantIntent
import com.dakti.app.domain.model.AssistantMessageRole
import com.dakti.app.domain.model.AssistantReply
import com.dakti.app.domain.model.AssistantStructuredRequest
import com.dakti.app.domain.model.AssistantSuggestionItem
import com.dakti.app.domain.model.AssistantVenueSuggestion
import com.dakti.app.domain.model.MatchCreatePayload
import com.dakti.app.domain.model.MatchMonitoringResult
import com.dakti.app.domain.model.MonitoringAlert
import com.dakti.app.domain.repository.MatchRepository
import com.dakti.app.domain.repository.ReservationRepository
import com.dakti.app.domain.repository.VenueRepository
import com.dakti.app.util.Resource
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

class AssistantOrchestrator @Inject constructor(
    private val intentParser: AssistantIntentParser,
    private val aiAssistantService: AiAssistantService,
    private val suggestionEngine: SuggestionEngine,
    private val venueRepository: VenueRepository,
    private val reservationRepository: ReservationRepository,
    private val matchRepository: MatchRepository,
    private val matchMonitoringCoordinator: MatchMonitoringCoordinator
) {

    suspend fun interpretRequest(
        message: String,
        conversationHistory: List<AssistantConversationMessage>,
        context: AssistantContext?
    ): AssistantReply {
        val parsedRequest = intentParser.parse(
            rawText = message,
            context = context
        )

        return when (parsedRequest.intent) {
            AssistantIntent.ORGANIZE_MATCH -> buildOrganizeMatchReply(parsedRequest)
            AssistantIntent.SUGGEST_VENUE -> buildVenueSuggestionReply(parsedRequest)
            AssistantIntent.SUGGEST_ALTERNATIVE_SLOT -> buildAlternativeSlotReply(parsedRequest)
            AssistantIntent.GENERATE_INVITATION_MESSAGE -> buildInvitationMessageReply(parsedRequest)
            AssistantIntent.GENERATE_REMINDER_MESSAGE -> buildReminderMessageReply(parsedRequest)
            AssistantIntent.RESCHEDULE_HELP -> buildRescheduleReply(parsedRequest)
            AssistantIntent.GENERAL_CHAT -> buildGenericReply(parsedRequest, conversationHistory)
        }
    }

    suspend fun suggestVenues(
        request: AssistantStructuredRequest
    ): Resource<List<AssistantVenueSuggestion>> =
        Resource.Success(buildVenueSuggestions(request = request, preferAlternatives = false))

    suspend fun suggestAlternativeSlots(
        request: AssistantStructuredRequest
    ): Resource<List<AssistantVenueSuggestion>> =
        Resource.Success(buildVenueSuggestions(request = request, preferAlternatives = true))

    suspend fun organizeMatchFromRequest(
        request: AssistantStructuredRequest
    ): Resource<AssistantReply> =
        Resource.Success(buildOrganizeMatchReply(request))

    suspend fun generateInvitationMessage(
        request: AssistantStructuredRequest
    ): Resource<AssistantGeneratedMessage> =
        Resource.Success(buildInvitationMessage(request))

    suspend fun generateReminderMessage(
        request: AssistantStructuredRequest
    ): Resource<AssistantGeneratedMessage> =
        Resource.Success(buildReminderMessage(request))

    suspend fun executeAction(
        proposal: AssistantActionProposal
    ): AssistantActionExecutionResult {
        return when (proposal.type) {
            AssistantActionType.NONE -> AssistantActionExecutionResult(
                success = false,
                message = "No actionable proposal to execute.",
                createdReservationId = null,
                createdMatchId = null
            )

            AssistantActionType.CREATE_RESERVATION_ONLY -> executeReservationOnly(proposal)
            AssistantActionType.CREATE_MATCH_FROM_RESERVATION -> executeMatchFromReservation(proposal)
            AssistantActionType.CREATE_RESERVATION_AND_MATCH -> executeReservationAndMatch(proposal)
        }
    }

    suspend fun evaluateMatchReadiness(
        matchId: String
    ): Resource<MatchMonitoringResult> =
        matchMonitoringCoordinator.evaluateMatchReadiness(matchId)

    suspend fun evaluateMyMatchReadiness(): Resource<List<MatchMonitoringResult>> =
        matchMonitoringCoordinator.evaluateMyMatchesReadiness()

    suspend fun generateMonitoringReminderMessage(matchId: String): Resource<String> =
        matchMonitoringCoordinator.generateMonitoringReminderMessage(matchId)

    suspend fun generateMonitoringUpdateMessage(matchId: String): Resource<String> =
        matchMonitoringCoordinator.generateMonitoringUpdateMessage(matchId)

    suspend fun monitorMatchAndBuildAlert(matchId: String): Resource<MonitoringAlert?> =
        matchMonitoringCoordinator.monitorMatchAndBuildAlert(matchId)

    private suspend fun buildOrganizeMatchReply(
        request: AssistantStructuredRequest
    ): AssistantReply {
        val sportType = request.sportType
        val players = request.desiredPlayers
        if (sportType.isNullOrBlank()) {
            return baseReply(
                text = "I can organize this for you. Tell me the sport type first (for example Football or Basketball).",
                intent = request.intent,
                parsedRequest = request
            )
        }

        if (players == null) {
            return baseReply(
                text = "How many players should I plan for?",
                intent = request.intent,
                parsedRequest = request
            )
        }

        val venueSuggestions = buildVenueSuggestions(
            request = request,
            preferAlternatives = false
        )

        if (venueSuggestions.isEmpty()) {
            return baseReply(
                text = "I could not find available slots for $sportType right now. Ask for alternative times and I will suggest the closest available options.",
                intent = request.intent,
                parsedRequest = request,
                suggestions = listOf(
                    AssistantSuggestionItem(
                        id = "sg-${UUID.randomUUID()}",
                        type = AISuggestionType.SCHEDULE,
                        title = "Ask for alternative slots",
                        description = "Example: Suggest another time near my preferred slot."
                    )
                )
            )
        }

        val topChoice = venueSuggestions.first()
        val proposal = AssistantActionProposal(
            id = "proposal-${UUID.randomUUID()}",
            type = AssistantActionType.CREATE_RESERVATION_AND_MATCH,
            title = "Create Reservation and Match",
            summary = "Reserve ${topChoice.venueName} at ${topChoice.timeSlotLabel} and create a $sportType match for $players players.",
            requiresConfirmation = true,
            venueId = topChoice.venueId,
            timeSlotId = topChoice.timeSlotId,
            sportType = sportType,
            requiredPlayers = players,
            scheduledStartTime = topChoice.startTime,
            reservationId = null,
            description = "Created from assistant workflow"
        )

        val explanation = buildString {
            append("I found ${venueSuggestions.size} suitable option")
            if (venueSuggestions.size > 1) append("s")
            append(" for organizing your $sportType match.")
            append(" Review the options below, then confirm when you are ready.")
        }

        return baseReply(
            text = explanation,
            intent = request.intent,
            parsedRequest = request,
            suggestions = venueSuggestions.take(3).map { suggestion ->
                AssistantSuggestionItem(
                    id = "sg-${UUID.randomUUID()}",
                    type = AISuggestionType.VENUE_RECOMMENDATION,
                    title = suggestion.venueName,
                    description = suggestion.reason
                )
            },
            venueSuggestions = venueSuggestions,
            actionProposal = proposal
        )
    }

    private suspend fun buildVenueSuggestionReply(
        request: AssistantStructuredRequest
    ): AssistantReply {
        val venueSuggestions = buildVenueSuggestions(
            request = request,
            preferAlternatives = false
        )
        if (venueSuggestions.isEmpty()) {
            return baseReply(
                text = "I could not find venue suggestions with the current filters. Try adding sport type or a preferred time.",
                intent = request.intent,
                parsedRequest = request
            )
        }

        return baseReply(
            text = "Here are venue suggestions ranked by sport match and time proximity.",
            intent = request.intent,
            parsedRequest = request,
            suggestions = venueSuggestions.take(3).map { suggestion ->
                AssistantSuggestionItem(
                    id = "sg-${UUID.randomUUID()}",
                    type = AISuggestionType.VENUE_RECOMMENDATION,
                    title = suggestion.venueName,
                    description = "${suggestion.timeSlotLabel} - ${suggestion.reason}"
                )
            },
            venueSuggestions = venueSuggestions
        )
    }

    private suspend fun buildAlternativeSlotReply(
        request: AssistantStructuredRequest
    ): AssistantReply {
        val alternatives = buildVenueSuggestions(
            request = request,
            preferAlternatives = true
        )
        if (alternatives.isEmpty()) {
            return baseReply(
                text = "I could not find alternative slots nearby. Try widening the time window or changing sport/venue preference.",
                intent = request.intent,
                parsedRequest = request
            )
        }

        return baseReply(
            text = "I found alternative available slots close to your preferred time.",
            intent = request.intent,
            parsedRequest = request,
            suggestions = alternatives.take(3).map { suggestion ->
                AssistantSuggestionItem(
                    id = "sg-${UUID.randomUUID()}",
                    type = AISuggestionType.SCHEDULE,
                    title = "${suggestion.venueName} - ${suggestion.timeSlotLabel}",
                    description = suggestion.reason
                )
            },
            venueSuggestions = alternatives
        )
    }

    private suspend fun buildInvitationMessageReply(
        request: AssistantStructuredRequest
    ): AssistantReply {
        val generatedMessage = buildInvitationMessage(request)
        return baseReply(
            text = "Here is a reusable invitation message draft.",
            intent = request.intent,
            parsedRequest = request,
            suggestions = listOf(
                AssistantSuggestionItem(
                    id = "sg-${UUID.randomUUID()}",
                    type = AISuggestionType.INVITATION_MESSAGE,
                    title = "Invitation Draft Ready",
                    description = "You can copy this content for chat or email in later phases."
                )
            ),
            generatedMessage = generatedMessage
        )
    }

    private suspend fun buildReminderMessageReply(
        request: AssistantStructuredRequest
    ): AssistantReply {
        val generatedMessage = buildReminderMessage(request)
        return baseReply(
            text = "Here is a reminder message draft you can reuse.",
            intent = request.intent,
            parsedRequest = request,
            suggestions = listOf(
                AssistantSuggestionItem(
                    id = "sg-${UUID.randomUUID()}",
                    type = AISuggestionType.REMINDER_MESSAGE,
                    title = "Reminder Draft Ready",
                    description = "Use this as your pre-match reminder template."
                )
            ),
            generatedMessage = generatedMessage
        )
    }

    private fun buildRescheduleReply(
        request: AssistantStructuredRequest
    ): AssistantReply {
        return baseReply(
            text = "For rescheduling, propose two nearby kickoff options and prioritize players who already accepted.",
            intent = request.intent,
            parsedRequest = request,
            suggestions = listOf(
                AssistantSuggestionItem(
                    id = "sg-${UUID.randomUUID()}",
                    type = AISuggestionType.RESCHEDULE_PLAN,
                    title = "Offer two alternative kickoff times",
                    description = "Keep at least one option on the same day if possible."
                ),
                AssistantSuggestionItem(
                    id = "sg-${UUID.randomUUID()}",
                    type = AISuggestionType.GENERAL,
                    title = "Reconfirm accepted players first",
                    description = "Secure enough confirmations before publishing the new schedule."
                )
            )
        )
    }

    private suspend fun buildGenericReply(
        request: AssistantStructuredRequest,
        conversationHistory: List<AssistantConversationMessage>
    ): AssistantReply {
        val turns = conversationHistory.map { message ->
            AiAssistantTurn(
                role = if (message.role == AssistantMessageRole.USER) "user" else "assistant",
                text = message.text
            )
        }
        return try {
            val response = aiAssistantService.generateReply(
                AiAssistantRequest(
                    systemPrompt = PromptBuilder.buildSystemPrompt(),
                    compiledPrompt = PromptBuilder.buildChatPrompt(
                        userMessage = request.rawText,
                        conversation = turns
                    ),
                    conversation = turns,
                    userMessage = request.rawText
                )
            )
            val parsed = AssistantResponseParser.parse(response.rawText)
            baseReply(
                text = parsed.replyText,
                intent = request.intent,
                parsedRequest = request,
                suggestions = suggestionEngine.buildSuggestionItems(
                    parsedSuggestions = parsed.suggestions,
                    userMessage = request.rawText
                ),
                providerLabel = response.providerLabel,
                usedFallback = response.usedFallback
            )
        } catch (_: Exception) {
            baseReply(
                text = "I can help with organizing a match, suggesting venues, finding alternative slots, and generating invitation or reminder messages.",
                intent = request.intent,
                parsedRequest = request
            )
        }
    }

    private suspend fun buildVenueSuggestions(
        request: AssistantStructuredRequest,
        preferAlternatives: Boolean
    ): List<AssistantVenueSuggestion> {
        val sportFilter = request.sportType?.trim()?.takeIf { it.isNotBlank() }
        val query = request.venuePreference?.trim().orEmpty()
        val searchResult = venueRepository.searchVenues(
            query = query,
            sportType = sportFilter
        )
        val venuesWithSlots = (searchResult as? Resource.Success)?.data.orEmpty()
        val requestedPlayers = request.desiredPlayers
        val preferredTime = request.preferredDateTime

        val availableSuggestions = mutableListOf<AssistantVenueSuggestion>()
        var hasBlockedPreferredSlot = false

        venuesWithSlots.forEach { venueWithSlots ->
            val venue = venueWithSlots.venue
            val compatibleSlots = venueWithSlots.slots
                .filter { slot ->
                    val capacityOk = requestedPlayers == null || slot.capacity == null || slot.capacity >= requestedPlayers
                    capacityOk
                }
                .sortedBy { slot ->
                    slotScore(
                        slotStart = slot.startTime,
                        preferredTime = preferredTime
                    )
                }

            val blockedNearPreferred = preferredTime != null &&
                compatibleSlots.any { slot ->
                    !slot.isAvailable && isWithinPreferredWindow(
                        slotStart = slot.startTime,
                        preferredTime = preferredTime
                    )
                }
            if (blockedNearPreferred) {
                hasBlockedPreferredSlot = true
            }

            compatibleSlots
                .filter { slot -> slot.isAvailable }
                .take(MAX_SLOTS_PER_VENUE)
                .forEach { slot ->
                    val preferredMatch = preferredTime?.let { preferred ->
                        isWithinPreferredWindow(slot.startTime, preferred)
                    } ?: false

                    if (preferAlternatives && preferredMatch) {
                        return@forEach
                    }

                    availableSuggestions += AssistantVenueSuggestion(
                        venueId = venue.id,
                        venueName = venue.name,
                        venueAddress = venue.address,
                        sportType = venue.sportType,
                        timeSlotId = slot.id,
                        timeSlotLabel = formatSlotLabel(slot.startTime, slot.endTime),
                        startTime = slot.startTime,
                        endTime = slot.endTime,
                        slotCapacity = slot.capacity,
                        isPreferredTime = preferredMatch,
                        reason = buildSuggestionReason(
                            preferredMatch = preferredMatch,
                            preferAlternatives = preferAlternatives,
                            hasBlockedPreferredSlot = blockedNearPreferred || hasBlockedPreferredSlot,
                            requestedPlayers = requestedPlayers,
                            slotCapacity = slot.capacity
                        )
                    )
                }
        }

        return availableSuggestions
            .sortedBy { suggestion ->
                slotScore(
                    slotStart = suggestion.startTime,
                    preferredTime = preferredTime
                )
            }
            .take(MAX_TOTAL_SUGGESTIONS)
    }

    private fun buildInvitationMessage(
        request: AssistantStructuredRequest
    ): AssistantGeneratedMessage {
        val sport = request.sportType ?: "sports"
        val schedule = request.preferredDateTime?.let { instant ->
            instant.atZone(ZoneId.systemDefault()).format(longDateTimeFormatter)
        } ?: "the scheduled time"
        val players = request.desiredPlayers?.toString() ?: "all invited players"

        val base = "Hi team, you are invited to a $sport match on $schedule. " +
            "We are targeting $players participants. Please reply with ACCEPT or DECLINE as soon as possible."

        return AssistantGeneratedMessage(
            kind = AssistantGeneratedMessageKind.INVITATION,
            title = "Invitation Message",
            content = base,
            variants = listOf(
                "Friendly: Hey everyone, join us for a $sport match at $schedule. Please confirm if you can make it.",
                "Concise: $sport match - $schedule. Confirm attendance (Accept/Decline).",
                "Formal: You are invited to a $sport match scheduled for $schedule. Kindly confirm your availability."
            )
        )
    }

    private fun buildReminderMessage(
        request: AssistantStructuredRequest
    ): AssistantGeneratedMessage {
        val sport = request.sportType ?: "sports"
        val schedule = request.preferredDateTime?.let { instant ->
            instant.atZone(ZoneId.systemDefault()).format(longDateTimeFormatter)
        } ?: "the scheduled kickoff"

        val base = "Reminder: Our $sport match is set for $schedule. " +
            "Please arrive 20 minutes early and confirm if anything has changed."

        return AssistantGeneratedMessage(
            kind = AssistantGeneratedMessageKind.REMINDER,
            title = "Reminder Message",
            content = base,
            variants = listOf(
                "Friendly: Quick reminder about our $sport game at $schedule. See you there!",
                "Concise: Reminder - $sport match at $schedule. Arrive early.",
                "Formal: This is a reminder that the $sport match is scheduled for $schedule."
            )
        )
    }

    private suspend fun executeReservationOnly(
        proposal: AssistantActionProposal
    ): AssistantActionExecutionResult {
        val venueId = proposal.venueId
            ?: return executionFailure("Missing venue selection for reservation.")
        val slotId = proposal.timeSlotId
            ?: return executionFailure("Missing time slot selection for reservation.")

        return when (val result = reservationRepository.createReservation(venueId, slotId, proposal.description)) {
            is Resource.Success -> AssistantActionExecutionResult(
                success = true,
                message = "Reservation created successfully at ${result.data.venueName}.",
                createdReservationId = result.data.id,
                createdMatchId = null
            )

            is Resource.Error -> executionFailure(result.message)
            Resource.Loading -> executionFailure("Reservation is still processing.")
        }
    }

    private suspend fun executeMatchFromReservation(
        proposal: AssistantActionProposal
    ): AssistantActionExecutionResult {
        val reservationId = proposal.reservationId
            ?: return executionFailure("Missing reservation reference for match creation.")

        val contexts = when (val contextsResult = matchRepository.getReservationContextsForCurrentOrganizer()) {
            is Resource.Success -> contextsResult.data
            is Resource.Error -> return executionFailure(contextsResult.message)
            Resource.Loading -> return executionFailure("Reservation contexts are still loading.")
        }

        val context = contexts.firstOrNull { item -> item.reservationId == reservationId }
            ?: return executionFailure("Could not find reservation context.")

        val payload = MatchCreatePayload(
            sportType = proposal.sportType ?: context.sportType,
            scheduledStartTime = proposal.scheduledStartTime ?: context.scheduledStartTime,
            requiredPlayers = proposal.requiredPlayers ?: DEFAULT_REQUIRED_PLAYERS,
            description = proposal.description,
            venueId = proposal.venueId ?: context.venueId,
            reservationId = reservationId
        )

        return when (val result = matchRepository.createMatch(payload)) {
            is Resource.Success -> AssistantActionExecutionResult(
                success = true,
                message = "Match created successfully from reservation.",
                createdReservationId = reservationId,
                createdMatchId = result.data.match.id
            )

            is Resource.Error -> executionFailure(result.message)
            Resource.Loading -> executionFailure("Match creation is still processing.")
        }
    }

    private suspend fun executeReservationAndMatch(
        proposal: AssistantActionProposal
    ): AssistantActionExecutionResult {
        val venueId = proposal.venueId
            ?: return executionFailure("Missing venue selection for this workflow.")
        val timeSlotId = proposal.timeSlotId
            ?: return executionFailure("Missing time slot selection for this workflow.")
        val sportType = proposal.sportType
            ?: return executionFailure("Missing sport type for match setup.")
        val requiredPlayers = proposal.requiredPlayers ?: DEFAULT_REQUIRED_PLAYERS
        val startTime = proposal.scheduledStartTime ?: Instant.now().plus(Duration.ofHours(DEFAULT_START_DELAY_HOURS))

        val reservationResult = reservationRepository.createReservation(
            venueId = venueId,
            timeSlotId = timeSlotId,
            note = proposal.description
        )

        val createdReservation = when (reservationResult) {
            is Resource.Success -> reservationResult.data
            is Resource.Error -> return executionFailure(reservationResult.message)
            Resource.Loading -> return executionFailure("Reservation is still processing.")
        }

        val matchResult = matchRepository.createMatch(
            MatchCreatePayload(
                sportType = sportType,
                scheduledStartTime = startTime,
                requiredPlayers = requiredPlayers,
                description = proposal.description,
                venueId = venueId,
                reservationId = createdReservation.id
            )
        )

        return when (matchResult) {
            is Resource.Success -> AssistantActionExecutionResult(
                success = true,
                message = "Reservation and match created successfully.",
                createdReservationId = createdReservation.id,
                createdMatchId = matchResult.data.match.id
            )

            is Resource.Error -> AssistantActionExecutionResult(
                success = false,
                message = "Reservation created, but match creation failed: ${matchResult.message}",
                createdReservationId = createdReservation.id,
                createdMatchId = null
            )

            Resource.Loading -> executionFailure("Match creation is still processing.")
        }
    }

    private fun baseReply(
        text: String,
        intent: AssistantIntent,
        parsedRequest: AssistantStructuredRequest?,
        suggestions: List<AssistantSuggestionItem> = emptyList(),
        venueSuggestions: List<AssistantVenueSuggestion> = emptyList(),
        generatedMessage: AssistantGeneratedMessage? = null,
        actionProposal: AssistantActionProposal? = null,
        providerLabel: String = ORCHESTRATOR_LABEL,
        usedFallback: Boolean = true
    ): AssistantReply =
        AssistantReply(
            text = text,
            intent = intent,
            parsedRequest = parsedRequest,
            suggestions = suggestions,
            venueSuggestions = venueSuggestions,
            generatedMessage = generatedMessage,
            actionProposal = actionProposal,
            quickActions = suggestionEngine.defaultQuickActions(),
            providerLabel = providerLabel,
            usedFallback = usedFallback
        )

    private fun slotScore(
        slotStart: Instant,
        preferredTime: Instant?
    ): Long {
        if (preferredTime == null) {
            return slotStart.toEpochMilli()
        }
        return kotlin.math.abs(Duration.between(preferredTime, slotStart).toMinutes())
    }

    private fun isWithinPreferredWindow(
        slotStart: Instant,
        preferredTime: Instant
    ): Boolean {
        return kotlin.math.abs(Duration.between(preferredTime, slotStart).toMinutes()) <= PREFERRED_WINDOW_MINUTES
    }

    private fun buildSuggestionReason(
        preferredMatch: Boolean,
        preferAlternatives: Boolean,
        hasBlockedPreferredSlot: Boolean,
        requestedPlayers: Int?,
        slotCapacity: Int?
    ): String {
        val capacityText = if (requestedPlayers != null && slotCapacity != null) {
            "Capacity $slotCapacity for $requestedPlayers players."
        } else {
            "Available slot."
        }

        return when {
            preferAlternatives || hasBlockedPreferredSlot -> {
                if (preferredMatch) {
                    "Closest alternative near your preferred time. $capacityText"
                } else {
                    "Alternative option when preferred slot is unavailable. $capacityText"
                }
            }
            preferredMatch -> "Matches your preferred kickoff window. $capacityText"
            else -> "Good available option for your selected sport. $capacityText"
        }
    }

    private fun formatSlotLabel(
        start: Instant,
        end: Instant
    ): String {
        val zoneId = ZoneId.systemDefault()
        val startText = start.atZone(zoneId).format(slotStartFormatter)
        val endText = end.atZone(zoneId).format(slotEndFormatter)
        return "$startText - $endText"
    }

    private fun executionFailure(message: String): AssistantActionExecutionResult =
        AssistantActionExecutionResult(
            success = false,
            message = message,
            createdReservationId = null,
            createdMatchId = null
        )

    private companion object {
        private const val ORCHESTRATOR_LABEL: String = "Dakti Assistant Orchestrator"
        private const val MAX_SLOTS_PER_VENUE: Int = 3
        private const val MAX_TOTAL_SUGGESTIONS: Int = 5
        private const val PREFERRED_WINDOW_MINUTES: Long = 90
        private const val DEFAULT_REQUIRED_PLAYERS: Int = 10
        private const val DEFAULT_START_DELAY_HOURS: Long = 24
        private val slotStartFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, d MMM HH:mm", Locale.getDefault())
        private val slotEndFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
        private val longDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm", Locale.getDefault())
    }
}
