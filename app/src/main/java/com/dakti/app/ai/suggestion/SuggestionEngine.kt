package com.dakti.app.ai.suggestion

import com.dakti.app.ai.parser.ParsedAssistantSuggestion
import com.dakti.app.domain.model.AISuggestionType
import com.dakti.app.domain.model.AssistantQuickAction
import com.dakti.app.domain.model.AssistantSuggestionItem
import java.util.UUID
import javax.inject.Inject

class SuggestionEngine @Inject constructor() {
    fun defaultQuickActions(): List<AssistantQuickAction> =
        listOf(
            AssistantQuickAction(
                id = "organize_match",
                title = "Organize Match",
                prompt = "Help me organize a 7v7 football match this weekend."
            ),
            AssistantQuickAction(
                id = "suggest_venue",
                title = "Suggest Venue",
                prompt = "Suggest how I should shortlist venues for an evening football game."
            ),
            AssistantQuickAction(
                id = "generate_invitation",
                title = "Generate Invitation",
                prompt = "Generate a friendly invitation message for players to confirm attendance."
            ),
            AssistantQuickAction(
                id = "remind_players",
                title = "Remind Players",
                prompt = "Draft a concise reminder message for players one day before kickoff."
            ),
            AssistantQuickAction(
                id = "reschedule_help",
                title = "Reschedule Help",
                prompt = "Help me reschedule a match after two players declined."
            )
        )

    fun defaultPromptSuggestions(): List<String> =
        listOf(
            "Help me plan a 5v5 basketball match for Friday evening.",
            "What should I check before confirming a venue slot?",
            "Write a short invitation message with RSVP deadline.",
            "How do I manage late player declines without cancelling the match?"
        )

    fun buildSuggestionItems(
        parsedSuggestions: List<ParsedAssistantSuggestion>,
        userMessage: String
    ): List<AssistantSuggestionItem> {
        if (parsedSuggestions.isNotEmpty()) {
            return parsedSuggestions.take(MAX_SUGGESTIONS).map { parsed ->
                AssistantSuggestionItem(
                    id = "sg-${UUID.randomUUID()}",
                    type = parsed.type,
                    title = parsed.title,
                    description = parsed.description
                )
            }
        }

        return fallbackSuggestions(userMessage).take(MAX_SUGGESTIONS)
    }

    private fun fallbackSuggestions(userMessage: String): List<AssistantSuggestionItem> {
        val normalized = userMessage.lowercase()
        val seed = when {
            "venue" in normalized -> listOf(
                fallbackItem(
                    type = AISuggestionType.VENUE_RECOMMENDATION,
                    title = "Filter by sport and time",
                    description = "Start with venues that match your sport and target kickoff window."
                ),
                fallbackItem(
                    type = AISuggestionType.SCHEDULE,
                    title = "Keep a backup slot",
                    description = "Prepare one extra time slot in case your first choice is booked."
                )
            )

            "invite" in normalized || "invitation" in normalized -> listOf(
                fallbackItem(
                    type = AISuggestionType.INVITATION_MESSAGE,
                    title = "Use clear RSVP instruction",
                    description = "Ask players to accept/decline with a deadline."
                ),
                fallbackItem(
                    type = AISuggestionType.REMINDER_MESSAGE,
                    title = "Schedule reminder draft",
                    description = "Prepare a short reminder for 24 hours before kickoff."
                )
            )

            else -> listOf(
                fallbackItem(
                    type = AISuggestionType.GENERAL,
                    title = "Clarify your objective",
                    description = "Tell the assistant your sport, date, and player target."
                ),
                fallbackItem(
                    type = AISuggestionType.MATCH_FORMAT,
                    title = "Pick a match format",
                    description = "Decide 5v5, 7v7, or 11v11 based on expected attendance."
                )
            )
        }
        return seed
    }

    private fun fallbackItem(
        type: AISuggestionType,
        title: String,
        description: String
    ): AssistantSuggestionItem =
        AssistantSuggestionItem(
            id = "sg-${UUID.randomUUID()}",
            type = type,
            title = title,
            description = description
        )

    private companion object {
        private const val MAX_SUGGESTIONS: Int = 3
    }
}
