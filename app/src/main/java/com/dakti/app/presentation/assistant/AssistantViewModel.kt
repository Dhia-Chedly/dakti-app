package com.dakti.app.presentation.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dakti.app.domain.model.AssistantActionProposal
import com.dakti.app.domain.model.AssistantActionType
import com.dakti.app.domain.model.AssistantContext
import com.dakti.app.domain.model.AssistantConversationMessage
import com.dakti.app.domain.model.AssistantGeneratedMessageKind
import com.dakti.app.domain.model.AssistantIntent
import com.dakti.app.domain.model.AssistantMessageRole
import com.dakti.app.domain.usecase.ExecuteAssistantActionUseCase
import com.dakti.app.domain.usecase.GetAssistantQuickActionsUseCase
import com.dakti.app.domain.usecase.GetAssistantSuggestedPromptsUseCase
import com.dakti.app.domain.usecase.InterpretAssistantRequestUseCase
import com.dakti.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AssistantSuggestionUi(
    val id: String,
    val title: String,
    val description: String?,
    val typeLabel: String
)

data class AssistantQuickActionUi(
    val id: String,
    val title: String,
    val prompt: String
)

data class AssistantVenueSuggestionUi(
    val venueId: String,
    val venueName: String,
    val venueAddress: String,
    val sportType: String,
    val timeSlotId: String,
    val timeSlotLabel: String,
    val startTime: Instant,
    val endTime: Instant,
    val slotCapacity: Int?,
    val isPreferredTime: Boolean,
    val reason: String
)

data class AssistantGeneratedMessageUi(
    val kind: AssistantGeneratedMessageKind,
    val title: String,
    val content: String,
    val variants: List<String>
)

data class AssistantActionProposalUi(
    val id: String,
    val type: AssistantActionType,
    val title: String,
    val summary: String,
    val requiresConfirmation: Boolean,
    val venueId: String?,
    val timeSlotId: String?,
    val sportType: String?,
    val requiredPlayers: Int?,
    val scheduledStartTime: Instant?,
    val reservationId: String?,
    val description: String?
)

data class AssistantMessageUi(
    val id: String,
    val role: AssistantMessageRole,
    val text: String,
    val createdAt: Instant,
    val timestampLabel: String,
    val intentLabel: String?,
    val suggestions: List<AssistantSuggestionUi> = emptyList(),
    val venueSuggestions: List<AssistantVenueSuggestionUi> = emptyList(),
    val generatedMessage: AssistantGeneratedMessageUi? = null,
    val actionProposal: AssistantActionProposalUi? = null,
    val providerLabel: String? = null,
    val isError: Boolean = false
)

data class AssistantUiState(
    val inputText: String = "",
    val isLoading: Boolean = false,
    val isExecutingAction: Boolean = false,
    val messages: List<AssistantMessageUi> = emptyList(),
    val quickActions: List<AssistantQuickActionUi> = emptyList(),
    val suggestedPrompts: List<String> = emptyList(),
    val pendingActionProposal: AssistantActionProposalUi? = null,
    val actionResultMessage: String? = null,
    val errorMessage: String? = null,
    val lastFailedPrompt: String? = null,
    val context: AssistantContext? = null
) {
    val canSend: Boolean
        get() = inputText.trim().isNotBlank() && !isLoading

    val showWelcomeState: Boolean
        get() = messages.isEmpty()
}

@HiltViewModel
class AssistantViewModel @Inject constructor(
    private val interpretAssistantRequestUseCase: InterpretAssistantRequestUseCase,
    private val executeAssistantActionUseCase: ExecuteAssistantActionUseCase,
    getAssistantQuickActionsUseCase: GetAssistantQuickActionsUseCase,
    getAssistantSuggestedPromptsUseCase: GetAssistantSuggestedPromptsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AssistantUiState(
            quickActions = getAssistantQuickActionsUseCase().map { action ->
                AssistantQuickActionUi(
                    id = action.id,
                    title = action.title,
                    prompt = action.prompt
                )
            },
            suggestedPrompts = getAssistantSuggestedPromptsUseCase()
        )
    )
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    fun setAssistantContext(context: AssistantContext?) {
        _uiState.update { state -> state.copy(context = context) }
    }

    fun onInputChanged(value: String) {
        _uiState.update { state ->
            state.copy(
                inputText = value,
                errorMessage = null
            )
        }
    }

    fun sendCurrentMessage() {
        sendMessage(_uiState.value.inputText)
    }

    fun sendSuggestedPrompt(prompt: String) {
        sendMessage(prompt)
    }

    fun sendQuickAction(action: AssistantQuickActionUi) {
        sendMessage(action.prompt)
    }

    fun retryLastFailedMessage() {
        val retryPrompt = _uiState.value.lastFailedPrompt ?: return
        sendMessage(retryPrompt)
    }

    fun clearError() {
        _uiState.update { state -> state.copy(errorMessage = null) }
    }

    fun clearActionResultMessage() {
        _uiState.update { state -> state.copy(actionResultMessage = null) }
    }

    fun cancelPendingAction() {
        _uiState.update { state ->
            state.copy(
                pendingActionProposal = null,
                actionResultMessage = "Assistant action canceled."
            )
        }
    }

    fun useVenueSuggestion(
        messageId: String,
        suggestionId: String
    ) {
        val state = _uiState.value
        val sourceMessage = state.messages.firstOrNull { message -> message.id == messageId } ?: return
        val selectedSuggestion = sourceMessage.venueSuggestions.firstOrNull { suggestion ->
            suggestion.timeSlotId == suggestionId
        } ?: return

        val baseProposal = state.pendingActionProposal ?: sourceMessage.actionProposal ?: return
        val updatedProposal = baseProposal.copy(
            venueId = selectedSuggestion.venueId,
            timeSlotId = selectedSuggestion.timeSlotId,
            sportType = baseProposal.sportType ?: selectedSuggestion.sportType,
            scheduledStartTime = selectedSuggestion.startTime,
            summary = "Reserve ${selectedSuggestion.venueName} at ${selectedSuggestion.timeSlotLabel} and continue with match setup."
        )

        _uiState.update { current ->
            current.copy(
                pendingActionProposal = updatedProposal,
                actionResultMessage = "Selected ${selectedSuggestion.venueName} at ${selectedSuggestion.timeSlotLabel}."
            )
        }
    }

    fun confirmPendingAction() {
        val proposal = _uiState.value.pendingActionProposal ?: return

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isExecutingAction = true,
                    errorMessage = null,
                    actionResultMessage = null
                )
            }

            when (val result = executeAssistantActionUseCase(proposal.toDomain())) {
                is Resource.Success -> {
                    val resultData = result.data
                    val assistantMessage = createMessage(
                        role = AssistantMessageRole.ASSISTANT,
                        text = resultData.message,
                        createdAt = Instant.now(),
                        intent = AssistantIntent.ORGANIZE_MATCH,
                        providerLabel = "Dakti Assistant Orchestrator"
                    )

                    _uiState.update { state ->
                        state.copy(
                            isExecutingAction = false,
                            pendingActionProposal = null,
                            actionResultMessage = buildString {
                                append(resultData.message)
                                if (!resultData.createdReservationId.isNullOrBlank()) {
                                    append(" Reservation ID: ${resultData.createdReservationId}.")
                                }
                                if (!resultData.createdMatchId.isNullOrBlank()) {
                                    append(" Match ID: ${resultData.createdMatchId}.")
                                }
                            },
                            messages = state.messages + assistantMessage
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.update { state ->
                        state.copy(
                            isExecutingAction = false,
                            errorMessage = result.message
                        )
                    }
                }

                Resource.Loading -> {
                    _uiState.update { state -> state.copy(isExecutingAction = true) }
                }
            }
        }
    }

    private fun sendMessage(rawInput: String) {
        val normalized = rawInput.trim()
        if (normalized.isBlank()) {
            _uiState.update { state -> state.copy(errorMessage = "Type a message first.") }
            return
        }

        val userMessage = createMessage(
            role = AssistantMessageRole.USER,
            text = normalized,
            createdAt = Instant.now(),
            intent = null
        )

        val updatedMessages = _uiState.value.messages + userMessage
        _uiState.update { state ->
            state.copy(
                inputText = "",
                isLoading = true,
                errorMessage = null,
                lastFailedPrompt = null,
                actionResultMessage = null,
                messages = updatedMessages
            )
        }

        val conversationHistory = updatedMessages
            .filterNot { item -> item.isError }
            .map { item ->
                AssistantConversationMessage(
                    id = item.id,
                    role = item.role,
                    text = item.text,
                    createdAt = item.createdAt
                )
            }

        viewModelScope.launch {
            when (
                val result = interpretAssistantRequestUseCase(
                    message = normalized,
                    conversationHistory = conversationHistory,
                    context = _uiState.value.context
                )
            ) {
                is Resource.Success -> {
                    val reply = result.data
                    val assistantMessage = createMessage(
                        role = AssistantMessageRole.ASSISTANT,
                        text = reply.text,
                        createdAt = Instant.now(),
                        intent = reply.intent,
                        suggestions = reply.suggestions.map { item ->
                            AssistantSuggestionUi(
                                id = item.id,
                                title = item.title,
                                description = item.description,
                                typeLabel = item.type.name.replace("_", " ")
                            )
                        },
                        venueSuggestions = reply.venueSuggestions.map { suggestion ->
                            AssistantVenueSuggestionUi(
                                venueId = suggestion.venueId,
                                venueName = suggestion.venueName,
                                venueAddress = suggestion.venueAddress,
                                sportType = suggestion.sportType,
                                timeSlotId = suggestion.timeSlotId,
                                timeSlotLabel = suggestion.timeSlotLabel,
                                startTime = suggestion.startTime,
                                endTime = suggestion.endTime,
                                slotCapacity = suggestion.slotCapacity,
                                isPreferredTime = suggestion.isPreferredTime,
                                reason = suggestion.reason
                            )
                        },
                        generatedMessage = reply.generatedMessage?.let { generated ->
                            AssistantGeneratedMessageUi(
                                kind = generated.kind,
                                title = generated.title,
                                content = generated.content,
                                variants = generated.variants
                            )
                        },
                        actionProposal = reply.actionProposal?.toUi(),
                        providerLabel = buildProviderLabel(
                            provider = reply.providerLabel,
                            usedFallback = reply.usedFallback
                        )
                    )

                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            messages = state.messages + assistantMessage,
                            quickActions = reply.quickActions.map { action ->
                                AssistantQuickActionUi(
                                    id = action.id,
                                    title = action.title,
                                    prompt = action.prompt
                                )
                            },
                            pendingActionProposal = if (assistantMessage.actionProposal?.requiresConfirmation == true) {
                                assistantMessage.actionProposal
                            } else {
                                state.pendingActionProposal
                            }
                        )
                    }
                }

                is Resource.Error -> {
                    val errorBubble = createMessage(
                        role = AssistantMessageRole.ASSISTANT,
                        text = "Assistant is unavailable right now.",
                        createdAt = Instant.now(),
                        intent = AssistantIntent.GENERAL_CHAT,
                        isError = true
                    )
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            messages = state.messages + errorBubble,
                            errorMessage = result.message,
                            lastFailedPrompt = normalized
                        )
                    }
                }

                Resource.Loading -> {
                    _uiState.update { state -> state.copy(isLoading = true) }
                }
            }
        }
    }

    private fun createMessage(
        role: AssistantMessageRole,
        text: String,
        createdAt: Instant,
        intent: AssistantIntent?,
        suggestions: List<AssistantSuggestionUi> = emptyList(),
        venueSuggestions: List<AssistantVenueSuggestionUi> = emptyList(),
        generatedMessage: AssistantGeneratedMessageUi? = null,
        actionProposal: AssistantActionProposalUi? = null,
        providerLabel: String? = null,
        isError: Boolean = false
    ): AssistantMessageUi =
        AssistantMessageUi(
            id = "msg-${UUID.randomUUID()}",
            role = role,
            text = text,
            createdAt = createdAt,
            timestampLabel = createdAt.atZone(ZoneId.systemDefault()).format(timeFormatter),
            intentLabel = intent?.toDisplayLabel(),
            suggestions = suggestions,
            venueSuggestions = venueSuggestions,
            generatedMessage = generatedMessage,
            actionProposal = actionProposal,
            providerLabel = providerLabel,
            isError = isError
        )

    private fun buildProviderLabel(
        provider: String,
        usedFallback: Boolean
    ): String {
        return if (usedFallback) {
            "$provider (fallback mode)"
        } else {
            provider
        }
    }

    private fun AssistantIntent.toDisplayLabel(): String =
        when (this) {
            AssistantIntent.ORGANIZE_MATCH -> "Organize Match"
            AssistantIntent.SUGGEST_VENUE -> "Venue Suggestion"
            AssistantIntent.SUGGEST_ALTERNATIVE_SLOT -> "Alternative Slots"
            AssistantIntent.GENERATE_INVITATION_MESSAGE -> "Invitation Message"
            AssistantIntent.GENERATE_REMINDER_MESSAGE -> "Reminder Message"
            AssistantIntent.RESCHEDULE_HELP -> "Reschedule Help"
            AssistantIntent.GENERAL_CHAT -> "General Assistant"
        }

    private fun AssistantActionProposalUi.toDomain(): AssistantActionProposal =
        AssistantActionProposal(
            id = id,
            type = type,
            title = title,
            summary = summary,
            requiresConfirmation = requiresConfirmation,
            venueId = venueId,
            timeSlotId = timeSlotId,
            sportType = sportType,
            requiredPlayers = requiredPlayers,
            scheduledStartTime = scheduledStartTime,
            reservationId = reservationId,
            description = description
        )

    private fun AssistantActionProposal.toUi(): AssistantActionProposalUi =
        AssistantActionProposalUi(
            id = id,
            type = type,
            title = title,
            summary = summary,
            requiresConfirmation = requiresConfirmation,
            venueId = venueId,
            timeSlotId = timeSlotId,
            sportType = sportType,
            requiredPlayers = requiredPlayers,
            scheduledStartTime = scheduledStartTime,
            reservationId = reservationId,
            description = description
        )

    companion object {
        private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}

