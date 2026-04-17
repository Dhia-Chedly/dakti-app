package com.dakti.app.ai.prompt

import com.dakti.app.ai.service.AiAssistantTurn

object PromptBuilder {
    fun buildSystemPrompt(): String {
        return """
            You are Dakti Assistant for a sports venue reservation and match organization app.
            Provide concise and practical guidance.
            Focus on planning help: venue selection, match setup, invitations, reminders, and rescheduling.
            Do not claim to execute real bookings, match creation, notifications, or external actions.
            When useful, include 2-3 structured suggestions using format:
            SUGGESTION|TYPE|TITLE|DESCRIPTION
            Valid TYPE values: MATCH_FORMAT, PLAYER_ALLOCATION, SCHEDULE, VENUE_RECOMMENDATION, INVITATION_MESSAGE, REMINDER_MESSAGE, RESCHEDULE_PLAN, GENERAL.
        """.trimIndent()
    }

    fun buildChatPrompt(
        userMessage: String,
        conversation: List<AiAssistantTurn>
    ): String {
        val historyBlock = conversation
            .takeLast(8)
            .joinToString(separator = "\n") { turn ->
                "${turn.role.uppercase()}: ${turn.text.trim()}"
            }

        return buildString {
            appendLine("SYSTEM:")
            appendLine(buildSystemPrompt())
            if (historyBlock.isNotBlank()) {
                appendLine()
                appendLine("RECENT_CONVERSATION:")
                appendLine(historyBlock)
            }
            appendLine()
            appendLine("USER:")
            appendLine(userMessage.trim())
            appendLine()
            appendLine("ASSISTANT:")
        }
    }
}
