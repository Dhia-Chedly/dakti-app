package com.dakti.app.data.repository

import android.util.Log
import com.dakti.app.ai.parser.AssistantIntentParser
import com.dakti.app.ai.service.AssistantOrchestrator
import com.dakti.app.data.local.session.SessionLocalDataSource
import com.dakti.app.data.remote.supabase.SupabaseRemoteDataSource
import com.dakti.app.domain.model.AISuggestionType
import com.dakti.app.domain.model.AssistantActionExecutionResult
import com.dakti.app.domain.model.AssistantActionProposal
import com.dakti.app.domain.model.AssistantActionType
import com.dakti.app.domain.model.AssistantContext
import com.dakti.app.domain.model.AssistantConversationMessage
import com.dakti.app.domain.model.AssistantGeneratedMessage
import com.dakti.app.domain.model.AssistantGeneratedMessageKind
import com.dakti.app.domain.model.AssistantIntent
import com.dakti.app.domain.model.AssistantQuickAction
import com.dakti.app.domain.model.AssistantReply
import com.dakti.app.domain.model.AssistantStructuredRequest
import com.dakti.app.domain.model.AssistantSuggestionItem
import com.dakti.app.domain.model.AssistantVenueSuggestion
import com.dakti.app.domain.model.MatchMonitoringResult
import com.dakti.app.domain.model.MatchReadinessStatus
import com.dakti.app.domain.model.MonitoringAlert
import com.dakti.app.domain.model.MonitoringSuggestedActionType
import com.dakti.app.domain.model.ReschedulingSuggestion
import com.dakti.app.domain.model.SuggestedAction
import com.dakti.app.domain.repository.AssistantRepository
import com.dakti.app.util.Resource
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssistantRepositoryImpl @Inject constructor(
    private val orchestrator: AssistantOrchestrator,
    private val supabaseRemoteDataSource: SupabaseRemoteDataSource,
    private val sessionLocalDataSource: SessionLocalDataSource,
    private val assistantIntentParser: AssistantIntentParser
) : AssistantRepository {

    private val slotStartFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("EEE, d MMM HH:mm", Locale.getDefault())
    private val slotEndFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())

    override suspend fun interpretAssistantRequest(
        message: String,
        conversationHistory: List<AssistantConversationMessage>,
        context: AssistantContext?
    ): Resource<AssistantReply> {
        val parsedRequest = assistantIntentParser.parse(
            rawText = message,
            context = context
        )

        if (
            parsedRequest.intent == AssistantIntent.GENERATE_INVITATION_MESSAGE ||
            parsedRequest.intent == AssistantIntent.GENERATE_REMINDER_MESSAGE
        ) {
            return Resource.Success(
                AssistantReply(
                    text = "Message generation is available in the Invite Players and Match Details flows. Open those screens and use the AI Help button there.",
                    intent = AssistantIntent.GENERAL_CHAT,
                    parsedRequest = parsedRequest,
                    suggestions = listOf(
                        AssistantSuggestionItem(
                            id = "sg-${UUID.randomUUID()}",
                            type = AISuggestionType.GENERAL,
                            title = "Go to Invite Players",
                            description = "Use AI Help there to draft invitation or reminder content."
                        )
                    ),
                    venueSuggestions = emptyList(),
                    generatedMessage = null,
                    actionProposal = null,
                    quickActions = getQuickActions(),
                    providerLabel = "Dakti Assistant",
                    usedFallback = true
                )
            )
        }

        val response = interpretGeneralChat(
            message = message,
            conversationHistory = conversationHistory,
            context = context
        )

        val reply = when (response) {
            is Resource.Success -> response.data
            is Resource.Error -> return response
            Resource.Loading -> return Resource.Loading
        }

        runCatching {
            logAssistantRequestAndSuggestions(
                userMessage = message,
                suggestions = reply.suggestions
            )
        }

        return Resource.Success(reply)
    }

    override suspend fun suggestVenues(
        request: AssistantStructuredRequest
    ): Resource<List<AssistantVenueSuggestion>> =
        orchestrator.suggestVenues(request)

    override suspend fun suggestAlternativeSlots(
        request: AssistantStructuredRequest
    ): Resource<List<AssistantVenueSuggestion>> =
        orchestrator.suggestAlternativeSlots(request)

    override suspend fun organizeMatchFromRequest(
        request: AssistantStructuredRequest
    ): Resource<AssistantReply> {
        return runCatching {
            val response = supabaseRemoteDataSource.invokeFunction(
                functionName = "organize-match",
                payload = mapOf(
                    "sport" to (request.sportType ?: "Football"),
                    "preferredDateTime" to request.preferredDateTime?.toString(),
                    "desiredPlayerCount" to request.desiredPlayers,
                    "venuePreference" to request.venuePreference,
                    "confirmAction" to false
                )
            )

            val explanation = response.get("explanation")?.asString
                ?: response.get("message")?.asString
                ?: "Review the suggested options and confirm one."

            val venueSuggestions = response.getAsJsonArray("suggestions")
                ?.mapNotNull { item ->
                    val objectValue = item.asJsonObject
                    val venueId = objectValue.get("venueId")?.asString ?: return@mapNotNull null
                    val slotId = objectValue.get("timeSlotId")?.asString ?: return@mapNotNull null
                    val startTime = objectValue.get("startTime")?.asString?.toInstantOrNow() ?: return@mapNotNull null
                    val endTime = objectValue.get("endTime")?.asString?.toInstantOrNow() ?: startTime
                    val venueName = objectValue.get("venueName")?.asString ?: "Venue option"
                    val venueAddress = objectValue.get("venueAddress")?.asString.orEmpty()
                    val reason = objectValue.get("recommendedReason")?.asString
                        ?: "Good option based on your requested criteria."

                    AssistantVenueSuggestion(
                        venueId = venueId,
                        venueName = venueName,
                        venueAddress = venueAddress,
                        sportType = request.sportType ?: "Football",
                        timeSlotId = slotId,
                        timeSlotLabel = formatRemoteSlotLabel(startTime, endTime),
                        startTime = startTime,
                        endTime = endTime,
                        slotCapacity = objectValue.get("capacity")?.asInt,
                        isPreferredTime = reason.contains("preferred", ignoreCase = true),
                        reason = reason
                    )
                }
                .orEmpty()

            val suggestions = venueSuggestions.take(3).map { suggestion ->
                AssistantSuggestionItem(
                    id = suggestion.timeSlotId,
                    type = AISuggestionType.VENUE_RECOMMENDATION,
                    title = suggestion.venueName,
                    description = suggestion.reason
                )
            }

            val requiresConfirmation = response.get("requiresConfirmation")?.asBoolean ?: true
            val actionProposal = if (requiresConfirmation && venueSuggestions.isNotEmpty()) {
                val topChoice = venueSuggestions.first()
                AssistantActionProposal(
                    id = "proposal-${UUID.randomUUID()}",
                    type = AssistantActionType.CREATE_RESERVATION_AND_MATCH,
                    title = "Create Reservation and Match",
                    summary = "Reserve ${topChoice.venueName} at ${topChoice.timeSlotLabel} and create a ${request.sportType ?: "Football"} match.",
                    requiresConfirmation = true,
                    venueId = topChoice.venueId,
                    timeSlotId = topChoice.timeSlotId,
                    sportType = request.sportType ?: "Football",
                    requiredPlayers = request.desiredPlayers ?: 10,
                    scheduledStartTime = topChoice.startTime,
                    reservationId = null,
                    description = "Created from assistant proposal"
                )
            } else {
                null
            }

            Resource.Success(
                AssistantReply(
                    text = explanation,
                    intent = AssistantIntent.ORGANIZE_MATCH,
                    parsedRequest = request,
                    suggestions = suggestions,
                    venueSuggestions = venueSuggestions,
                    generatedMessage = null,
                    actionProposal = actionProposal,
                    quickActions = getQuickActions(),
                    providerLabel = "Supabase Edge Function",
                    usedFallback = false
                )
            )
        }.getOrElse {
            orchestrator.organizeMatchFromRequest(request)
        }
    }

    override suspend fun generateInvitationMessage(
        request: AssistantStructuredRequest
    ): Resource<AssistantGeneratedMessage> {
        val matchId = request.targetMatchId ?: request.context?.matchId
        if (matchId.isNullOrBlank()) {
            return orchestrator.generateInvitationMessage(request)
        }

        return runCatching {
            val response = supabaseRemoteDataSource.invokeFunction(
                functionName = "generate-invitation",
                payload = mapOf(
                    "matchId" to matchId,
                    "style" to "friendly"
                )
            )

            val text = response.get("invitationText")?.asString
                ?.takeIf { value -> value.isNotBlank() }
                ?: throw IllegalStateException("Invitation function returned empty text")

            val variants = response.getAsJsonArray("variants")
                ?.mapNotNull { item -> item?.asString }
                .orEmpty()

            Resource.Success(
                AssistantGeneratedMessage(
                    kind = AssistantGeneratedMessageKind.INVITATION,
                    title = "Invitation Message",
                    content = text,
                    variants = variants
                )
            )
        }.getOrElse {
            orchestrator.generateInvitationMessage(request)
        }
    }

    override suspend fun generateReminderMessage(
        request: AssistantStructuredRequest
    ): Resource<AssistantGeneratedMessage> {
        val matchId = request.targetMatchId ?: request.context?.matchId
        if (matchId.isNullOrBlank()) {
            return orchestrator.generateReminderMessage(request)
        }

        return runCatching {
            val response = supabaseRemoteDataSource.invokeFunction(
                functionName = "generate-reminder",
                payload = mapOf(
                    "matchId" to matchId,
                    "audienceType" to "players"
                )
            )

            val text = response.get("reminderText")?.asString
                ?.takeIf { value -> value.isNotBlank() }
                ?: throw IllegalStateException("Reminder function returned empty text")

            val variants = response.getAsJsonArray("variants")
                ?.mapNotNull { item -> item?.asString }
                .orEmpty()

            Resource.Success(
                AssistantGeneratedMessage(
                    kind = AssistantGeneratedMessageKind.REMINDER,
                    title = "Reminder Message",
                    content = text,
                    variants = variants
                )
            )
        }.getOrElse {
            orchestrator.generateReminderMessage(request)
        }
    }

    override suspend fun executeAssistantAction(
        proposal: AssistantActionProposal
    ): Resource<AssistantActionExecutionResult> {
        return runCatching {
            Resource.Success(orchestrator.executeAction(proposal))
        }.getOrElse { exception ->
            Resource.Error(exception.message ?: "Could not execute assistant action.")
        }
    }

    override suspend fun evaluateMatchReadiness(
        matchId: String
    ): Resource<MatchMonitoringResult> {
        val monitored = runCatching {
            supabaseRemoteDataSource.invokeFunction(
                functionName = "monitor-match",
                payload = mapOf("matchId" to matchId)
            )
        }.getOrNull()

        if (monitored != null) {
            val status = monitored.get("readinessStatus")?.asString.orEmpty().toMatchReadinessStatus()
            val participation = monitored.getAsJsonObject("participation")
            val requiredPlayers = participation?.get("requiredPlayers")?.asInt ?: 0
            val confirmedPlayers = participation?.get("confirmedPlayers")?.asInt ?: 0
            val pendingPlayers = participation?.get("pendingPlayers")?.asInt ?: 0
            val declinedPlayers = participation?.get("declinedPlayers")?.asInt ?: 0
            val remainingSpots = participation?.get("remainingSpots")?.asInt
                ?: (requiredPlayers - confirmedPlayers).coerceAtLeast(0)

            val suggestions = monitored.getAsJsonArray("suggestedNextActions")
                ?.mapIndexed { index, item ->
                    SuggestedAction(
                        id = "sa-${UUID.randomUUID()}",
                        type = when (index) {
                            0 -> MonitoringSuggestedActionType.REMIND_PENDING_PLAYERS
                            1 -> MonitoringSuggestedActionType.INVITE_MORE_PLAYERS
                            else -> MonitoringSuggestedActionType.REVIEW_RESCHEDULE_OPTIONS
                        },
                        title = item?.asString ?: "Take action",
                        description = null
                    )
                }
                .orEmpty()

            val alternatives = monitored.getAsJsonArray("alternativeSlots")
                ?.map { item ->
                    val slot = item.asJsonObject
                    ReschedulingSuggestion(
                        id = "alt-${UUID.randomUUID()}",
                        venueId = slot.get("venueId")?.asString.orEmpty(),
                        venueName = "Alternative Venue",
                        venueAddress = "",
                        timeSlotId = slot.get("timeSlotId")?.asString.orEmpty(),
                        timeSlotLabel = slot.get("startTime")?.asString.orEmpty(),
                        startTime = slot.get("startTime")?.asString.orEmpty().toInstantOrNow(),
                        endTime = slot.get("endTime")?.asString.orEmpty().toInstantOrNow(),
                        reason = "Suggested by monitoring"
                    )
                }
                .orEmpty()

            return Resource.Success(
                MatchMonitoringResult(
                    matchId = matchId,
                    matchTitle = "Monitored Match",
                    sportType = "Sports",
                    venueName = "Venue",
                    scheduledStartTime = Instant.now(),
                    status = status,
                    reason = monitored.get("issueSummary")?.asString ?: "No issue summary",
                    summary = monitored.get("issueSummary")?.asString ?: "No summary",
                    requiredPlayers = requiredPlayers,
                    invitedPlayersCount = confirmedPlayers + pendingPlayers + declinedPlayers,
                    confirmedPlayersCount = confirmedPlayers,
                    pendingPlayersCount = pendingPlayers,
                    declinedPlayersCount = declinedPlayers,
                    remainingSpots = remainingSpots,
                    minutesUntilMatch = 0,
                    shouldAlertOrganizer = status != MatchReadinessStatus.READY,
                    suggestedActions = suggestions,
                    reschedulingSuggestions = alternatives,
                    reminderMessageText = monitored.get("reminderText")?.asString,
                    updateMessageText = monitored.get("updateText")?.asString
                )
            )
        }

        return orchestrator.evaluateMatchReadiness(matchId)
    }

    override suspend fun evaluateMyMatchReadiness(): Resource<List<MatchMonitoringResult>> =
        orchestrator.evaluateMyMatchReadiness()

    override suspend fun generateMonitoringReminderMessage(matchId: String): Resource<String> {
        return runCatching {
            val response = supabaseRemoteDataSource.invokeFunction(
                functionName = "monitor-match",
                payload = mapOf("matchId" to matchId)
            )
            val text = response.get("reminderText")?.asString
                ?.takeIf { value -> value.isNotBlank() }
                ?: throw IllegalStateException("No reminder text available")
            Resource.Success(text)
        }.getOrElse {
            orchestrator.generateMonitoringReminderMessage(matchId)
        }
    }

    override suspend fun generateMonitoringUpdateMessage(matchId: String): Resource<String> {
        return runCatching {
            val response = supabaseRemoteDataSource.invokeFunction(
                functionName = "monitor-match",
                payload = mapOf("matchId" to matchId)
            )
            val text = response.get("updateText")?.asString
                ?.takeIf { value -> value.isNotBlank() }
                ?: throw IllegalStateException("No update text available")
            Resource.Success(text)
        }.getOrElse {
            orchestrator.generateMonitoringUpdateMessage(matchId)
        }
    }

    override suspend fun monitorMatchAndBuildAlert(matchId: String): Resource<MonitoringAlert?> {
        val readinessResult = evaluateMatchReadiness(matchId)
        val readiness = (readinessResult as? Resource.Success)?.data ?: return Resource.Success(null)

        if (!readiness.shouldAlertOrganizer) {
            return Resource.Success(null)
        }

        return Resource.Success(
            MonitoringAlert(
                id = "m-alert-${UUID.randomUUID()}",
                matchId = matchId,
                title = readiness.status.name.replace('_', ' '),
                body = readiness.reason,
                status = readiness.status,
                createdAt = Instant.now(),
                summary = readiness.summary,
                suggestedActions = readiness.suggestedActions,
                reschedulingSuggestions = readiness.reschedulingSuggestions,
                reminderMessageText = readiness.reminderMessageText,
                updateMessageText = readiness.updateMessageText
            )
        )
    }

    override fun getQuickActions(): List<AssistantQuickAction> =
        listOf(
            AssistantQuickAction(
                id = "venue_evening",
                title = "Evening Venues",
                prompt = "What football venues are available tomorrow evening around 6 PM?"
            ),
            AssistantQuickAction(
                id = "basketball_slots",
                title = "Basketball Slots",
                prompt = "Show me available basketball venues around 7 PM."
            ),
            AssistantQuickAction(
                id = "wearing_tips",
                title = "What to Wear",
                prompt = "What should I wear for an outdoor football match at night?"
            ),
            AssistantQuickAction(
                id = "hydration_tips",
                title = "Hydration Tips",
                prompt = "How should players hydrate before and during a 90-minute match?"
            )
        )

    override fun getSuggestedPrompts(): List<String> =
        listOf(
            "What football venues are available tomorrow evening?",
            "Any basketball courts available around 7 PM?",
            "Give me three tennis venue options this weekend.",
            "What should I wear for a turf football game?",
            "Best warm-up routine before a basketball match?",
            "How much water should players drink before kickoff?"
        )

    private suspend fun interpretGeneralChat(
        message: String,
        conversationHistory: List<AssistantConversationMessage>,
        context: AssistantContext?
    ): Resource<AssistantReply> {
        val remoteReply = runCatching {
            buildRemoteAssistantReply(message = message, context = context)
        }.onFailure { throwable ->
            Log.e(TAG, "Remote assistant call failed, using fallback.", throwable)
        }.getOrNull()

        val reply = remoteReply ?: AssistantReply(
            text = "I’m temporarily in fallback mode. I can still help with venue availability questions and practical sports tips.",
            intent = AssistantIntent.GENERAL_CHAT,
            parsedRequest = assistantIntentParser.parse(rawText = message, context = context),
            suggestions = listOf(
                AssistantSuggestionItem(
                    id = "sg-${UUID.randomUUID()}",
                    type = AISuggestionType.VENUE_RECOMMENDATION,
                    title = "Ask for available venues by sport/time",
                    description = "Example: football venues available tomorrow 6 PM."
                ),
                AssistantSuggestionItem(
                    id = "sg-${UUID.randomUUID()}",
                    type = AISuggestionType.GENERAL,
                    title = "Ask match-day sports questions",
                    description = "Example: what to wear for an evening football match."
                )
            ),
            venueSuggestions = emptyList(),
            generatedMessage = null,
            actionProposal = null,
            quickActions = getQuickActions(),
            providerLabel = "Dakti Assistant (fallback)",
            usedFallback = true
        )

        return Resource.Success(reply)
    }

    private suspend fun buildVenueSuggestionReply(
        request: AssistantStructuredRequest,
        preferAlternatives: Boolean
    ): Resource<AssistantReply> {
        val suggestionsResult = if (preferAlternatives) {
            suggestAlternativeSlots(request)
        } else {
            suggestVenues(request)
        }

        return when (suggestionsResult) {
            is Resource.Success -> {
                val venueSuggestions = suggestionsResult.data
                val replyText = when {
                    venueSuggestions.isEmpty() && preferAlternatives ->
                        "I could not find nearby alternatives right now. Try a wider time window."
                    venueSuggestions.isEmpty() ->
                        "I could not find venue matches. Try adding a sport type or preferred time."
                    preferAlternatives ->
                        "Here are alternative available slots close to your preferred time."
                    else ->
                        "Here are venue suggestions based on your request."
                }

                Resource.Success(
                    AssistantReply(
                        text = replyText,
                        intent = if (preferAlternatives) {
                            AssistantIntent.SUGGEST_ALTERNATIVE_SLOT
                        } else {
                            AssistantIntent.SUGGEST_VENUE
                        },
                        parsedRequest = request,
                        suggestions = venueSuggestions.take(3).map { suggestion ->
                            AssistantSuggestionItem(
                                id = suggestion.timeSlotId,
                                type = if (preferAlternatives) {
                                    AISuggestionType.SCHEDULE
                                } else {
                                    AISuggestionType.VENUE_RECOMMENDATION
                                },
                                title = suggestion.venueName,
                                description = "${suggestion.timeSlotLabel} - ${suggestion.reason}"
                            )
                        },
                        venueSuggestions = venueSuggestions,
                        generatedMessage = null,
                        actionProposal = null,
                        quickActions = getQuickActions(),
                        providerLabel = "Dakti Assistant Orchestrator",
                        usedFallback = true
                    )
                )
            }

            is Resource.Error -> Resource.Error(suggestionsResult.message)
            Resource.Loading -> Resource.Loading
        }
    }

    private suspend fun buildGeneratedMessageReply(
        request: AssistantStructuredRequest,
        kind: AssistantGeneratedMessageKind
    ): Resource<AssistantReply> {
        val generatedResult = when (kind) {
            AssistantGeneratedMessageKind.INVITATION -> generateInvitationMessage(request)
            AssistantGeneratedMessageKind.REMINDER -> generateReminderMessage(request)
        }

        return when (generatedResult) {
            is Resource.Success -> {
                val generatedMessage = generatedResult.data
                val replyText = when (kind) {
                    AssistantGeneratedMessageKind.INVITATION -> "Here is an invitation draft you can reuse."
                    AssistantGeneratedMessageKind.REMINDER -> "Here is a reminder draft you can reuse."
                }
                val suggestionType = when (kind) {
                    AssistantGeneratedMessageKind.INVITATION -> AISuggestionType.INVITATION_MESSAGE
                    AssistantGeneratedMessageKind.REMINDER -> AISuggestionType.REMINDER_MESSAGE
                }
                val intent = when (kind) {
                    AssistantGeneratedMessageKind.INVITATION -> AssistantIntent.GENERATE_INVITATION_MESSAGE
                    AssistantGeneratedMessageKind.REMINDER -> AssistantIntent.GENERATE_REMINDER_MESSAGE
                }

                Resource.Success(
                    AssistantReply(
                        text = replyText,
                        intent = intent,
                        parsedRequest = request,
                        suggestions = listOf(
                            AssistantSuggestionItem(
                                id = "sg-${UUID.randomUUID()}",
                                type = suggestionType,
                                title = generatedMessage.title,
                                description = "Use copy, WhatsApp, or Email actions from the card."
                            )
                        ),
                        venueSuggestions = emptyList(),
                        generatedMessage = generatedMessage,
                        actionProposal = null,
                        quickActions = getQuickActions(),
                        providerLabel = "Dakti Message Generator",
                        usedFallback = true
                    )
                )
            }

            is Resource.Error -> Resource.Error(generatedResult.message)
            Resource.Loading -> Resource.Loading
        }
    }

    private suspend fun buildRemoteAssistantReply(
        message: String,
        context: AssistantContext?
    ): AssistantReply {
        val response = supabaseRemoteDataSource.invokeFunction(
            functionName = "gemini-assistant",
            payload = mapOf(
                "prompt" to message,
                "context" to context,
                "relatedIds" to mapOf(
                    "matchId" to context?.matchId,
                    "reservationId" to context?.reservationId,
                    "venueId" to context?.venueId
                )
            )
        )

        val text = response.get("assistantText")?.asString
            ?.takeIf { value -> value.isNotBlank() }
            ?: "Assistant is available but no response text was returned."

        val suggestions = response.getAsJsonArray("suggestions")
            ?.map { item ->
                val objectValue = item.asJsonObject
                AssistantSuggestionItem(
                    id = objectValue.get("id")?.asString ?: "sg-${UUID.randomUUID()}",
                    type = objectValue.get("type")?.asString.orEmpty().toSuggestionType(),
                    title = objectValue.get("title")?.asString ?: "Suggestion",
                    description = objectValue.get("description")?.asString
                )
            }
            .orEmpty()

        return AssistantReply(
            text = text,
            intent = AssistantIntent.GENERAL_CHAT,
            parsedRequest = null,
            suggestions = suggestions,
            venueSuggestions = emptyList(),
            generatedMessage = null,
            actionProposal = null,
            quickActions = getQuickActions(),
            providerLabel = response.get("provider")?.asString ?: "Supabase Gemini",
            usedFallback = response.get("provider")?.asString != "gemini"
        )
    }

    private suspend fun logAssistantRequestAndSuggestions(
        userMessage: String,
        suggestions: List<AssistantSuggestionItem>
    ) {
        val userId = sessionLocalDataSource.authenticatedUserId.value?.takeIf { it.isNotBlank() } ?: return
        val request = supabaseRemoteDataSource.createAiRequest(
            payload = mapOf(
                "user_id" to userId,
                "request_text" to userMessage,
                "request_type" to "assistant_chat"
            )
        ) ?: return

        if (suggestions.isNotEmpty()) {
            supabaseRemoteDataSource.createAiSuggestions(
                payload = suggestions.map { suggestion ->
                    mapOf(
                        "request_id" to request.id,
                        "suggestion_type" to suggestion.type.name.lowercase(),
                        "suggestion_text" to suggestion.title,
                        "payload" to mapOf("description" to suggestion.description)
                    )
                }
            )
        }
    }

    private fun formatRemoteSlotLabel(
        start: Instant,
        end: Instant
    ): String {
        val zone = ZoneId.systemDefault()
        val startText = start.atZone(zone).format(slotStartFormatter)
        val endText = end.atZone(zone).format(slotEndFormatter)
        return "$startText - $endText"
    }

    private fun String.toSuggestionType(): AISuggestionType =
        when (lowercase()) {
            "venue_recommendation" -> AISuggestionType.VENUE_RECOMMENDATION
            "invitation_message" -> AISuggestionType.INVITATION_MESSAGE
            "reminder_message" -> AISuggestionType.REMINDER_MESSAGE
            "reschedule_plan" -> AISuggestionType.RESCHEDULE_PLAN
            "schedule" -> AISuggestionType.SCHEDULE
            else -> AISuggestionType.GENERAL
        }

    private fun String.toMatchReadinessStatus(): MatchReadinessStatus =
        when (lowercase()) {
            "ready" -> MatchReadinessStatus.READY
            "at_risk" -> MatchReadinessStatus.AT_RISK
            "insufficient_players" -> MatchReadinessStatus.INSUFFICIENT_PLAYERS
            else -> MatchReadinessStatus.NEEDS_ORGANIZER_ACTION
        }

    private fun String.toInstantOrNow(): Instant =
        runCatching { Instant.parse(this) }.getOrElse { Instant.now() }

    private companion object {
        private const val TAG: String = "AssistantRepository"
    }
}
