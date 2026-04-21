package com.dakti.app.domain.usecase

import com.dakti.app.domain.model.AssistantActionProposal
import com.dakti.app.domain.model.AssistantContext
import com.dakti.app.domain.model.AssistantConversationMessage
import com.dakti.app.domain.model.AssistantStructuredRequest
import com.dakti.app.domain.repository.AssistantRepository
import javax.inject.Inject

class InterpretAssistantRequestUseCase @Inject constructor(
    private val assistantRepository: AssistantRepository
) {
    suspend operator fun invoke(
        message: String,
        conversationHistory: List<AssistantConversationMessage>,
        context: AssistantContext? = null
    ) = assistantRepository.interpretAssistantRequest(
        message = message,
        conversationHistory = conversationHistory,
        context = context
    )
}

class SuggestVenuesUseCase @Inject constructor(
    private val assistantRepository: AssistantRepository
) {
    suspend operator fun invoke(request: AssistantStructuredRequest) =
        assistantRepository.suggestVenues(request)
}

class SuggestAlternativeSlotsUseCase @Inject constructor(
    private val assistantRepository: AssistantRepository
) {
    suspend operator fun invoke(request: AssistantStructuredRequest) =
        assistantRepository.suggestAlternativeSlots(request)
}

class OrganizeMatchWithAssistantUseCase @Inject constructor(
    private val assistantRepository: AssistantRepository
) {
    suspend operator fun invoke(request: AssistantStructuredRequest) =
        assistantRepository.organizeMatchFromRequest(request)
}

class GenerateInvitationMessageUseCase @Inject constructor(
    private val assistantRepository: AssistantRepository
) {
    suspend operator fun invoke(request: AssistantStructuredRequest) =
        assistantRepository.generateInvitationMessage(request)
}

class GenerateReminderMessageUseCase @Inject constructor(
    private val assistantRepository: AssistantRepository
) {
    suspend operator fun invoke(request: AssistantStructuredRequest) =
        assistantRepository.generateReminderMessage(request)
}

class ExecuteAssistantActionUseCase @Inject constructor(
    private val assistantRepository: AssistantRepository
) {
    suspend operator fun invoke(proposal: AssistantActionProposal) =
        assistantRepository.executeAssistantAction(proposal)
}

class EvaluateMatchReadinessUseCase @Inject constructor(
    private val assistantRepository: AssistantRepository
) {
    suspend operator fun invoke(matchId: String) =
        assistantRepository.evaluateMatchReadiness(matchId)
}

class EvaluateMyMatchReadinessUseCase @Inject constructor(
    private val assistantRepository: AssistantRepository
) {
    suspend operator fun invoke() = assistantRepository.evaluateMyMatchReadiness()
}

class GenerateMonitoringReminderMessageUseCase @Inject constructor(
    private val assistantRepository: AssistantRepository
) {
    suspend operator fun invoke(matchId: String) =
        assistantRepository.generateMonitoringReminderMessage(matchId)
}

class GenerateMonitoringUpdateMessageUseCase @Inject constructor(
    private val assistantRepository: AssistantRepository
) {
    suspend operator fun invoke(matchId: String) =
        assistantRepository.generateMonitoringUpdateMessage(matchId)
}

class MonitorMatchAndBuildAlertUseCase @Inject constructor(
    private val assistantRepository: AssistantRepository
) {
    suspend operator fun invoke(matchId: String) =
        assistantRepository.monitorMatchAndBuildAlert(matchId)
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
