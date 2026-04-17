package com.dakti.app.domain.repository

import com.dakti.app.domain.model.AssistantConversationMessage
import com.dakti.app.domain.model.AssistantActionExecutionResult
import com.dakti.app.domain.model.AssistantActionProposal
import com.dakti.app.domain.model.AssistantContext
import com.dakti.app.domain.model.AssistantGeneratedMessage
import com.dakti.app.domain.model.AssistantQuickAction
import com.dakti.app.domain.model.AssistantReply
import com.dakti.app.domain.model.AssistantStructuredRequest
import com.dakti.app.domain.model.AssistantVenueSuggestion
import com.dakti.app.util.Resource

interface AssistantRepository {
    suspend fun interpretAssistantRequest(
        message: String,
        conversationHistory: List<AssistantConversationMessage>,
        context: AssistantContext? = null
    ): Resource<AssistantReply>
    suspend fun suggestVenues(
        request: AssistantStructuredRequest
    ): Resource<List<AssistantVenueSuggestion>>
    suspend fun suggestAlternativeSlots(
        request: AssistantStructuredRequest
    ): Resource<List<AssistantVenueSuggestion>>
    suspend fun organizeMatchFromRequest(
        request: AssistantStructuredRequest
    ): Resource<AssistantReply>
    suspend fun generateInvitationMessage(
        request: AssistantStructuredRequest
    ): Resource<AssistantGeneratedMessage>
    suspend fun generateReminderMessage(
        request: AssistantStructuredRequest
    ): Resource<AssistantGeneratedMessage>
    suspend fun executeAssistantAction(
        proposal: AssistantActionProposal
    ): Resource<AssistantActionExecutionResult>
    fun getQuickActions(): List<AssistantQuickAction>
    fun getSuggestedPrompts(): List<String>
}
