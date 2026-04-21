package com.dakti.app.data.repository

import com.dakti.app.ai.service.AssistantOrchestrator
import com.dakti.app.data.local.dao.AssistantDao
import com.dakti.app.data.local.dao.UserDao
import com.dakti.app.data.local.session.SessionLocalDataSource
import com.dakti.app.data.mapper.toEntity
import com.dakti.app.domain.model.AIRequest
import com.dakti.app.domain.model.AISuggestion
import com.dakti.app.domain.model.AssistantActionExecutionResult
import com.dakti.app.domain.model.AssistantActionProposal
import com.dakti.app.domain.model.AssistantContext
import com.dakti.app.domain.model.AssistantConversationMessage
import com.dakti.app.domain.model.AssistantGeneratedMessage
import com.dakti.app.domain.model.MatchMonitoringResult
import com.dakti.app.domain.model.MonitoringAlert
import com.dakti.app.domain.model.AssistantQuickAction
import com.dakti.app.domain.model.AssistantReply
import com.dakti.app.domain.model.AssistantStructuredRequest
import com.dakti.app.domain.model.AssistantVenueSuggestion
import com.dakti.app.domain.model.User
import com.dakti.app.domain.model.UserRole
import com.dakti.app.domain.repository.AssistantRepository
import com.dakti.app.util.Resource
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class AssistantRepositoryImpl @Inject constructor(
    private val orchestrator: AssistantOrchestrator,
    private val assistantDao: AssistantDao,
    private val userDao: UserDao,
    private val sessionLocalDataSource: SessionLocalDataSource
) : AssistantRepository {

    override suspend fun interpretAssistantRequest(
        message: String,
        conversationHistory: List<AssistantConversationMessage>,
        context: AssistantContext?
    ): Resource<AssistantReply> {
        return runCatching {
            val reply = orchestrator.interpretRequest(
                message = message,
                conversationHistory = conversationHistory,
                context = context
            )
            logAssistantRequestAndSuggestions(
                userMessage = message,
                suggestions = reply.suggestions.map { item ->
                    AISuggestion(
                        id = "ai-sg-${UUID.randomUUID()}",
                        requestId = "",
                        type = item.type,
                        suggestionText = if (item.description.isNullOrBlank()) {
                            item.title
                        } else {
                            "${item.title}: ${item.description}"
                        },
                        confidenceScore = null,
                        createdAt = Instant.now()
                    )
                }
            )
            Resource.Success(reply)
        }.getOrElse { exception ->
            Resource.Error(exception.message ?: "Assistant is unavailable right now.")
        }
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
    ): Resource<AssistantReply> =
        orchestrator.organizeMatchFromRequest(request)

    override suspend fun generateInvitationMessage(
        request: AssistantStructuredRequest
    ): Resource<AssistantGeneratedMessage> =
        orchestrator.generateInvitationMessage(request)

    override suspend fun generateReminderMessage(
        request: AssistantStructuredRequest
    ): Resource<AssistantGeneratedMessage> =
        orchestrator.generateReminderMessage(request)

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
    ): Resource<MatchMonitoringResult> =
        orchestrator.evaluateMatchReadiness(matchId)

    override suspend fun evaluateMyMatchReadiness(): Resource<List<MatchMonitoringResult>> =
        orchestrator.evaluateMyMatchReadiness()

    override suspend fun generateMonitoringReminderMessage(matchId: String): Resource<String> =
        orchestrator.generateMonitoringReminderMessage(matchId)

    override suspend fun generateMonitoringUpdateMessage(matchId: String): Resource<String> =
        orchestrator.generateMonitoringUpdateMessage(matchId)

    override suspend fun monitorMatchAndBuildAlert(matchId: String): Resource<MonitoringAlert?> =
        orchestrator.monitorMatchAndBuildAlert(matchId)

    override fun getQuickActions(): List<AssistantQuickAction> =
        listOf(
            AssistantQuickAction(
                id = "organize_match",
                title = "Organize Match",
                prompt = "Organize a football match for Saturday at 6 PM for 10 players"
            ),
            AssistantQuickAction(
                id = "suggest_venue",
                title = "Suggest Venue",
                prompt = "Suggest football venues available tomorrow evening"
            ),
            AssistantQuickAction(
                id = "alternative_slots",
                title = "Alternative Slot",
                prompt = "Suggest another time if my preferred slot is unavailable"
            ),
            AssistantQuickAction(
                id = "generate_invitation",
                title = "Generate Invitation",
                prompt = "Generate invitation message for my upcoming match"
            ),
            AssistantQuickAction(
                id = "generate_reminder",
                title = "Generate Reminder",
                prompt = "Generate reminder message for tomorrow's match"
            ),
            AssistantQuickAction(
                id = "check_readiness",
                title = "Check Readiness",
                prompt = "Assess match readiness and suggest next steps if players are low"
            )
        )

    override fun getSuggestedPrompts(): List<String> =
        listOf(
            "Organize a football match for Saturday at 6 PM for 10 players",
            "Suggest available basketball venues around 7 PM tomorrow",
            "Suggest another time because my preferred slot is unavailable",
            "Generate invitation message for my upcoming match",
            "Generate reminder message for tomorrow's match",
            "My match is short of players. Suggest reminders and reschedule options."
        )

    private suspend fun logAssistantRequestAndSuggestions(
        userMessage: String,
        suggestions: List<AISuggestion>
    ) {
        runCatching {
            val userId = resolveAssistantUserId()
            val now = Instant.now()
            val requestId = "ai-req-${UUID.randomUUID()}"

            assistantDao.upsertAIRequest(
                AIRequest(
                    id = requestId,
                    userId = userId,
                    promptText = userMessage,
                    contextType = "assistant_orchestrator",
                    createdAt = now
                ).toEntity()
            )

            if (suggestions.isNotEmpty()) {
                assistantDao.upsertSuggestions(
                    suggestions.map { suggestion ->
                        suggestion.copy(
                            requestId = requestId,
                            createdAt = now
                        ).toEntity()
                    }
                )
            }
        }
    }

    private suspend fun resolveAssistantUserId(): String {
        val sessionUserId = sessionLocalDataSource.authenticatedUserId.value
            ?.takeIf { value -> value.isNotBlank() }
        if (sessionUserId != null) {
            ensureUserExists(sessionUserId)
            return sessionUserId
        }

        ensureDemoAssistantUser()
        return DEMO_ASSISTANT_USER_ID
    }

    private suspend fun ensureUserExists(userId: String) {
        val existing = userDao.getUserById(userId)
        if (existing != null) {
            return
        }

        val now = Instant.now()
        userDao.upsertUser(
            User(
                id = userId,
                displayName = "Dakti User",
                email = "$userId@dakti.app",
                phoneNumber = null,
                avatarUrl = null,
                role = UserRole.PLAYER,
                bio = null,
                createdAt = now,
                updatedAt = now
            ).toEntity()
        )
    }

    private suspend fun ensureDemoAssistantUser() {
        if (userDao.getUserById(DEMO_ASSISTANT_USER_ID) != null) {
            return
        }

        val now = Instant.now()
        userDao.upsertUser(
            User(
                id = DEMO_ASSISTANT_USER_ID,
                displayName = "Dakti Assistant Demo User",
                email = "assistant.demo@dakti.app",
                phoneNumber = null,
                avatarUrl = null,
                role = UserRole.PLAYER,
                bio = "Seed user for assistant logs",
                createdAt = now,
                updatedAt = now
            ).toEntity()
        )
    }

    private companion object {
        private const val DEMO_ASSISTANT_USER_ID: String = "assistant-demo-user"
    }
}
