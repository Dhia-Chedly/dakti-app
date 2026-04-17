package com.dakti.app.ai.service

data class AiAssistantTurn(
    val role: String,
    val text: String
)

data class AiAssistantRequest(
    val systemPrompt: String,
    val compiledPrompt: String,
    val conversation: List<AiAssistantTurn>,
    val userMessage: String
)

data class AiAssistantResponse(
    val rawText: String,
    val providerLabel: String,
    val usedFallback: Boolean
)

interface AiAssistantService {
    suspend fun generateReply(request: AiAssistantRequest): AiAssistantResponse
}

class DemoAiAssistantService : AiAssistantService {
    override suspend fun generateReply(request: AiAssistantRequest): AiAssistantResponse {
        val input = request.userMessage.trim()
        if (input.isBlank()) {
            return AiAssistantResponse(
                rawText = "Please share what you need help with so I can assist.",
                providerLabel = "Dakti Demo Assistant",
                usedFallback = true
            )
        }

        val normalized = input.lowercase()
        if ("simulate error" in normalized || "assistant fail" in normalized) {
            throw IllegalStateException("Assistant temporarily unavailable")
        }

        val response = when {
            "venue" in normalized -> {
                """
                You can shortlist venues by sport type, distance, and available evening slots to reduce booking conflicts.
                SUGGESTION|VENUE_RECOMMENDATION|Filter football venues first|Start with venues tagged for your sport and check open slots for your preferred day.
                SUGGESTION|SCHEDULE|Keep two backup slots|Pick one primary and one backup slot in case the first gets reserved.
                SUGGESTION|GENERAL|Ask players for location preference|Confirm travel convenience before finalizing venue choice.
                """.trimIndent()
            }

            "invitation" in normalized || "invite" in normalized -> {
                """
                Keep invitation messages short, clear, and action-oriented so players can respond quickly.
                SUGGESTION|INVITATION_MESSAGE|Use a 3-part invitation|Include match type, venue/time, and RSVP instruction in one message.
                SUGGESTION|GENERAL|Mention remaining spots|This creates urgency and improves response speed.
                SUGGESTION|REMINDER_MESSAGE|Send reminder 24h before kickoff|A short reminder reduces no-shows.
                """.trimIndent()
            }

            "reschedule" in normalized || "schedule" in normalized -> {
                """
                For rescheduling, propose two alternatives and keep the confirmed players informed in one thread.
                SUGGESTION|RESCHEDULE_PLAN|Offer two new kickoff times|Choose options close to the original schedule to maximize acceptance.
                SUGGESTION|GENERAL|Prioritize accepted players|Check accepted players first before locking a new time.
                SUGGESTION|REMINDER_MESSAGE|Send updated confirmation|After selecting a new slot, send one final confirmation message.
                """.trimIndent()
            }

            "match" in normalized || "organize" in normalized -> {
                """
                A reliable match setup flow is: lock venue/time, invite players, monitor acceptances, then send a reminder.
                SUGGESTION|MATCH_FORMAT|Set target format early|Decide 5v5, 7v7, or 11v11 based on available players.
                SUGGESTION|PLAYER_ALLOCATION|Keep two standby players|Standby players help absorb late declines.
                SUGGESTION|GENERAL|Confirm attendance checkpoint|Review acceptances 24 hours before match day.
                """.trimIndent()
            }

            else -> {
                """
                I can help with venue selection, match setup, invitation content, reminders, and schedule planning.
                SUGGESTION|GENERAL|Try a specific request|Example: "Help me organize a 7v7 football match this Saturday."
                SUGGESTION|VENUE_RECOMMENDATION|Ask for venue checklist|I can suggest what to verify before booking.
                SUGGESTION|INVITATION_MESSAGE|Ask for invitation draft|I can draft concise invitation text for players.
                """.trimIndent()
            }
        }

        return AiAssistantResponse(
            rawText = response,
            providerLabel = "Dakti Demo Assistant",
            usedFallback = true
        )
    }
}
