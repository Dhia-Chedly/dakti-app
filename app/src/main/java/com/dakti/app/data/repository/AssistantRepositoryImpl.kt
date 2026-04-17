package com.dakti.app.data.repository

import com.dakti.app.ai.parser.AssistantResponseParser
import com.dakti.app.ai.prompt.PromptBuilder
import com.dakti.app.ai.service.AiAssistantRequest
import com.dakti.app.ai.service.AiAssistantService
import com.dakti.app.ai.service.AiAssistantTurn
import com.dakti.app.ai.suggestion.SuggestionEngine
import com.dakti.app.data.local.dao.AssistantDao
import com.dakti.app.data.local.dao.UserDao
import com.dakti.app.data.local.session.SessionLocalDataSource
import com.dakti.app.data.mapper.toEntity
import com.dakti.app.domain.model.AIRequest
import com.dakti.app.domain.model.AISuggestion
import com.dakti.app.domain.model.AssistantConversationMessage
import com.dakti.app.domain.model.AssistantMessageRole
import com.dakti.app.domain.model.AssistantQuickAction
import com.dakti.app.domain.model.AssistantReply
import com.dakti.app.domain.model.User
import com.dakti.app.domain.model.UserRole
import com.dakti.app.domain.repository.AssistantRepository
import com.dakti.app.util.Resource
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class AssistantRepositoryImpl @Inject constructor(
    private val aiAssistantService: AiAssistantService,
    private val assistantDao: AssistantDao,
    private val userDao: UserDao,
    private val sessionLocalDataSource: SessionLocalDataSource,
    private val suggestionEngine: SuggestionEngine
) : AssistantRepository {

    override suspend fun sendAssistantMessage(
        message: String,
        conversationHistory: List<AssistantConversationMessage>
    ): Resource<AssistantReply> {
        val normalizedMessage = message.trim()
        if (normalizedMessage.isBlank()) {
            return Resource.Error("Message cannot be empty")
        }

        val promptConversation = conversationHistory.map { item ->
            AiAssistantTurn(
                role = if (item.role == AssistantMessageRole.USER) "user" else "assistant",
                text = item.text
            )
        }

        val requestPayload = AiAssistantRequest(
            systemPrompt = PromptBuilder.buildSystemPrompt(),
            compiledPrompt = PromptBuilder.buildChatPrompt(
                userMessage = normalizedMessage,
                conversation = promptConversation
            ),
            conversation = promptConversation,
            userMessage = normalizedMessage
        )

        return try {
            val serviceResponse = aiAssistantService.generateReply(requestPayload)
            val parsedResponse = AssistantResponseParser.parse(serviceResponse.rawText)
            val suggestionItems = suggestionEngine.buildSuggestionItems(
                parsedSuggestions = parsedResponse.suggestions,
                userMessage = normalizedMessage
            )
            logAssistantRequestAndSuggestions(
                userMessage = normalizedMessage,
                suggestions = suggestionItems.map { item ->
                    AISuggestion(
                        id = "ai-sg-${UUID.randomUUID()}",
                        requestId = "",
                        type = item.type,
                        suggestionText = buildSuggestionText(
                            title = item.title,
                            description = item.description
                        ),
                        confidenceScore = null,
                        createdAt = Instant.now()
                    )
                }
            )

            Resource.Success(
                AssistantReply(
                    text = parsedResponse.replyText,
                    suggestions = suggestionItems,
                    quickActions = suggestionEngine.defaultQuickActions(),
                    providerLabel = serviceResponse.providerLabel,
                    usedFallback = serviceResponse.usedFallback
                )
            )
        } catch (exception: Exception) {
            Resource.Error(
                exception.message ?: "Assistant is unavailable right now. Please try again."
            )
        }
    }

    override fun getQuickActions(): List<AssistantQuickAction> =
        suggestionEngine.defaultQuickActions()

    override fun getSuggestedPrompts(): List<String> =
        suggestionEngine.defaultPromptSuggestions()

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
                    contextType = "assistant_chat",
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

    private fun buildSuggestionText(
        title: String,
        description: String?
    ): String {
        return if (description.isNullOrBlank()) {
            title
        } else {
            "$title: $description"
        }
    }

    private companion object {
        private const val DEMO_ASSISTANT_USER_ID: String = "assistant-demo-user"
    }
}

