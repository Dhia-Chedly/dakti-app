package com.dakti.app.domain.usecase

import com.dakti.app.domain.model.AssistantConversationMessage
import com.dakti.app.domain.repository.AssistantRepository
import javax.inject.Inject

class SendAssistantMessageUseCase @Inject constructor(
    private val assistantRepository: AssistantRepository
) {
    suspend operator fun invoke(
        message: String,
        conversationHistory: List<AssistantConversationMessage>
    ) = assistantRepository.sendAssistantMessage(
        message = message,
        conversationHistory = conversationHistory
    )
}

class GetAssistantQuickActionsUseCase @Inject constructor(
    private val assistantRepository: AssistantRepository
) {
    operator fun invoke() = assistantRepository.getQuickActions()
}

class GetAssistantSuggestedPromptsUseCase @Inject constructor(
    private val assistantRepository: AssistantRepository
) {
    operator fun invoke() = assistantRepository.getSuggestedPrompts()
}

