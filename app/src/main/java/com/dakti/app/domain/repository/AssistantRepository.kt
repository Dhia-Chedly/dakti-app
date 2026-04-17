package com.dakti.app.domain.repository

import com.dakti.app.domain.model.AssistantConversationMessage
import com.dakti.app.domain.model.AssistantQuickAction
import com.dakti.app.domain.model.AssistantReply
import com.dakti.app.util.Resource

interface AssistantRepository {
    suspend fun sendAssistantMessage(
        message: String,
        conversationHistory: List<AssistantConversationMessage>
    ): Resource<AssistantReply>
    fun getQuickActions(): List<AssistantQuickAction>
    fun getSuggestedPrompts(): List<String>
}
