package com.dakti.app.ai.service

import com.dakti.app.ai.prompt.MonitoringPromptBuilder
import com.dakti.app.ai.prompt.PromptBuilder
import com.dakti.app.ai.suggestion.MatchReadinessAssessment
import com.dakti.app.ai.suggestion.MatchReadinessEvaluator
import com.dakti.app.ai.suggestion.ReschedulingSuggestionEngine
import com.dakti.app.domain.model.MatchMonitoringResult
import com.dakti.app.domain.model.MatchReadinessStatus
import com.dakti.app.domain.model.MatchWithContext
import com.dakti.app.domain.model.MonitoringAlert
import com.dakti.app.domain.model.MonitoringSuggestedActionType
import com.dakti.app.domain.model.ReschedulingSuggestion
import com.dakti.app.domain.model.SuggestedAction
import com.dakti.app.domain.repository.MatchRepository
import com.dakti.app.util.Resource
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

class MatchMonitoringCoordinator @Inject constructor(
    private val matchRepository: MatchRepository,
    private val readinessEvaluator: MatchReadinessEvaluator,
    private val reschedulingSuggestionEngine: ReschedulingSuggestionEngine,
    private val aiAssistantService: AiAssistantService
) {

    suspend fun evaluateMatchReadiness(matchId: String): Resource<MatchMonitoringResult> {
        val details = when (val matchResult = matchRepository.getMatchDetails(matchId)) {
            is Resource.Success -> matchResult.data
            is Resource.Error -> return Resource.Error(matchResult.message)
            Resource.Loading -> return Resource.Loading
        }
        return Resource.Success(buildMonitoringResult(details))
    }

    suspend fun evaluateMyMatchesReadiness(): Resource<List<MatchMonitoringResult>> {
        val matches = when (val matchesResult = matchRepository.getMyMatches()) {
            is Resource.Success -> matchesResult.data
            is Resource.Error -> return Resource.Error(matchesResult.message)
            Resource.Loading -> return Resource.Loading
        }

        val monitoringResults = matches
            .map { item -> buildMonitoringResult(item) }
            .sortedWith(
                compareByDescending<MatchMonitoringResult> { result ->
                    result.shouldAlertOrganizer
                }.thenByDescending { result ->
                    severityScore(result.status)
                }.thenBy { result ->
                    result.minutesUntilMatch
                }
            )

        return Resource.Success(monitoringResults)
    }

    suspend fun monitorMatchAndBuildAlert(matchId: String): Resource<MonitoringAlert?> {
        val monitoringResult = when (val readinessResult = evaluateMatchReadiness(matchId)) {
            is Resource.Success -> readinessResult.data
            is Resource.Error -> return Resource.Error(readinessResult.message)
            Resource.Loading -> return Resource.Loading
        }

        if (!monitoringResult.shouldAlertOrganizer || monitoringResult.status == MatchReadinessStatus.READY) {
            return Resource.Success(null)
        }

        val alert = MonitoringAlert(
            id = "m-alert-${UUID.randomUUID()}",
            matchId = monitoringResult.matchId,
            title = buildAlertTitle(monitoringResult),
            body = monitoringResult.reason,
            status = monitoringResult.status,
            createdAt = Instant.now(),
            summary = monitoringResult.summary,
            suggestedActions = monitoringResult.suggestedActions,
            reschedulingSuggestions = monitoringResult.reschedulingSuggestions,
            reminderMessageText = monitoringResult.reminderMessageText,
            updateMessageText = monitoringResult.updateMessageText
        )

        return Resource.Success(alert)
    }

    suspend fun generateMonitoringReminderMessage(matchId: String): Resource<String> {
        val result = when (val readinessResult = evaluateMatchReadiness(matchId)) {
            is Resource.Success -> readinessResult.data
            is Resource.Error -> return Resource.Error(readinessResult.message)
            Resource.Loading -> return Resource.Loading
        }

        val message = result.reminderMessageText
            ?: fallbackReminderMessage(result)
        return Resource.Success(message)
    }

    suspend fun generateMonitoringUpdateMessage(matchId: String): Resource<String> {
        val result = when (val readinessResult = evaluateMatchReadiness(matchId)) {
            is Resource.Success -> readinessResult.data
            is Resource.Error -> return Resource.Error(readinessResult.message)
            Resource.Loading -> return Resource.Loading
        }

        val message = result.updateMessageText
            ?: fallbackUpdateMessage(result)
        return Resource.Success(message)
    }

    private suspend fun buildMonitoringResult(
        details: MatchWithContext
    ): MatchMonitoringResult {
        val assessment = readinessEvaluator.evaluate(details)
        val baseResult = MatchMonitoringResult(
            matchId = details.match.id,
            matchTitle = details.match.title,
            sportType = details.match.sportType,
            venueName = details.venueName,
            scheduledStartTime = details.match.scheduledStartTime,
            status = assessment.status,
            reason = assessment.reason,
            summary = fallbackSummary(details, assessment),
            requiredPlayers = details.match.requiredPlayers,
            invitedPlayersCount = details.invitedPlayersCount,
            confirmedPlayersCount = details.confirmedPlayersCount,
            pendingPlayersCount = details.pendingPlayersCount,
            declinedPlayersCount = details.declinedPlayersCount,
            remainingSpots = details.remainingSpots,
            minutesUntilMatch = assessment.minutesUntilMatch,
            shouldAlertOrganizer = assessment.shouldAlertOrganizer,
            suggestedActions = buildSuggestedActions(details, assessment),
            reschedulingSuggestions = emptyList(),
            reminderMessageText = null,
            updateMessageText = null
        )

        val alternatives = if (assessment.status == MatchReadinessStatus.READY) {
            emptyList()
        } else {
            reschedulingSuggestionEngine.suggestAlternatives(details)
        }

        val enrichedSummary = generateSummaryWithFallback(baseResult)
        val reminderText = generateReminderWithFallback(baseResult)
        val updateText = generateUpdateWithFallback(baseResult)

        return baseResult.copy(
            summary = enrichedSummary,
            reschedulingSuggestions = alternatives,
            reminderMessageText = reminderText,
            updateMessageText = updateText
        )
    }

    private fun buildSuggestedActions(
        details: MatchWithContext,
        assessment: MatchReadinessAssessment
    ): List<SuggestedAction> {
        val actions = mutableListOf<SuggestedAction>()

        if (details.pendingPlayersCount > 0) {
            actions += SuggestedAction(
                id = "sa-${UUID.randomUUID()}",
                type = MonitoringSuggestedActionType.REMIND_PENDING_PLAYERS,
                title = "Remind pending players",
                description = "Send a short reminder to players who have not responded."
            )
        }

        if (details.confirmedPlayersCount < details.match.requiredPlayers) {
            actions += SuggestedAction(
                id = "sa-${UUID.randomUUID()}",
                type = MonitoringSuggestedActionType.INVITE_MORE_PLAYERS,
                title = "Invite more players",
                description = "Add more invitees to close the player gap before kickoff."
            )
        }

        if (assessment.status != MatchReadinessStatus.READY) {
            actions += SuggestedAction(
                id = "sa-${UUID.randomUUID()}",
                type = MonitoringSuggestedActionType.REVIEW_RESCHEDULE_OPTIONS,
                title = "Review rescheduling options",
                description = "Check nearby slots and backup venue options."
            )
            actions += SuggestedAction(
                id = "sa-${UUID.randomUUID()}",
                type = MonitoringSuggestedActionType.PREPARE_UPDATE_MESSAGE,
                title = "Prepare update message",
                description = "Generate a status update for invited players."
            )
            actions += SuggestedAction(
                id = "sa-${UUID.randomUUID()}",
                type = MonitoringSuggestedActionType.OPEN_ASSISTANT,
                title = "Open assistant guidance",
                description = "Get AI-backed next-step suggestions for this match."
            )
        }

        return actions
    }

    private suspend fun generateSummaryWithFallback(result: MatchMonitoringResult): String {
        val prompt = MonitoringPromptBuilder.buildSummaryPrompt(result)
        return queryAiSingleLine(prompt) ?: fallbackSummaryText(result)
    }

    private suspend fun generateReminderWithFallback(result: MatchMonitoringResult): String {
        val prompt = MonitoringPromptBuilder.buildReminderMessagePrompt(result)
        return queryAiSingleLine(prompt) ?: fallbackReminderMessage(result)
    }

    private suspend fun generateUpdateWithFallback(result: MatchMonitoringResult): String {
        val prompt = MonitoringPromptBuilder.buildUpdateMessagePrompt(result)
        return queryAiSingleLine(prompt) ?: fallbackUpdateMessage(result)
    }

    private suspend fun queryAiSingleLine(prompt: String): String? {
        val response = runCatching {
            aiAssistantService.generateReply(
                AiAssistantRequest(
                    systemPrompt = PromptBuilder.buildSystemPrompt(),
                    compiledPrompt = prompt,
                    conversation = emptyList(),
                    userMessage = prompt
                )
            )
        }.getOrNull() ?: return null

        return response.rawText
            .lineSequence()
            .map { line -> line.trim() }
            .firstOrNull { line ->
                line.isNotBlank() && !line.startsWith("SUGGESTION|")
            }
            ?.take(220)
    }

    private fun fallbackSummary(
        details: MatchWithContext,
        assessment: MatchReadinessAssessment
    ): String {
        val kickoff = details.match.scheduledStartTime
            .atZone(ZoneId.systemDefault())
            .format(dateTimeFormatter)
        return when (assessment.status) {
            MatchReadinessStatus.READY -> "Match is on track for kickoff at $kickoff."
            MatchReadinessStatus.AT_RISK -> "Match is at risk: pending responses are still high for the $kickoff kickoff."
            MatchReadinessStatus.INSUFFICIENT_PLAYERS -> "Match is short of confirmed players for the $kickoff kickoff."
            MatchReadinessStatus.NEEDS_ORGANIZER_ACTION -> "Match needs organizer action to improve readiness before kickoff."
        }
    }

    private fun fallbackSummaryText(result: MatchMonitoringResult): String =
        when (result.status) {
            MatchReadinessStatus.READY -> "Readiness looks healthy. Keep regular reminders in place."
            MatchReadinessStatus.AT_RISK -> "Readiness is uncertain due to pending responses. Send reminders and prepare a backup slot."
            MatchReadinessStatus.INSUFFICIENT_PLAYERS -> "Confirmed players are below target. Invite more players or review rescheduling options."
            MatchReadinessStatus.NEEDS_ORGANIZER_ACTION -> "Take action now by reminding pending players and reviewing alternatives."
        }

    private fun fallbackReminderMessage(result: MatchMonitoringResult): String =
        "Hi team, quick reminder for ${result.matchTitle} at ${result.venueName}. " +
            "Please reply ACCEPT or DECLINE so we can finalize the lineup."

    private fun fallbackUpdateMessage(result: MatchMonitoringResult): String =
        "Update for ${result.matchTitle}: we currently have ${result.confirmedPlayersCount}/" +
            "${result.requiredPlayers} confirmed players. Please respond soon and watch for possible schedule adjustments."

    private fun buildAlertTitle(result: MatchMonitoringResult): String =
        when (result.status) {
            MatchReadinessStatus.READY -> "Match Ready"
            MatchReadinessStatus.AT_RISK -> "Match At Risk"
            MatchReadinessStatus.INSUFFICIENT_PLAYERS -> "Insufficient Players"
            MatchReadinessStatus.NEEDS_ORGANIZER_ACTION -> "Match Needs Action"
        }

    private fun severityScore(status: MatchReadinessStatus): Int =
        when (status) {
            MatchReadinessStatus.READY -> 0
            MatchReadinessStatus.NEEDS_ORGANIZER_ACTION -> 1
            MatchReadinessStatus.AT_RISK -> 2
            MatchReadinessStatus.INSUFFICIENT_PLAYERS -> 3
        }

    private companion object {
        private val dateTimeFormatter: DateTimeFormatter =
            DateTimeFormatter.ofPattern("EEE, d MMM HH:mm", Locale.getDefault())
    }
}

