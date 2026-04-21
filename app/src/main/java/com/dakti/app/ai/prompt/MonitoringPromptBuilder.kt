package com.dakti.app.ai.prompt

import com.dakti.app.domain.model.MatchMonitoringResult

object MonitoringPromptBuilder {

    fun buildSummaryPrompt(result: MatchMonitoringResult): String =
        buildString {
            append("You are an assistant for a sports match organizer. ")
            append("Write one concise explanation of why this match needs attention and what to do next. ")
            append("Avoid markdown and keep it under 45 words.\n")
            append("Match: ${result.matchTitle} (${result.sportType})\n")
            append("Status: ${result.status.name}\n")
            append("Players: confirmed ${result.confirmedPlayersCount}/${result.requiredPlayers}, ")
            append("pending ${result.pendingPlayersCount}, declined ${result.declinedPlayersCount}\n")
            append("Time to kickoff: ${result.minutesUntilMatch} minutes\n")
            append("Reason: ${result.reason}")
        }

    fun buildReminderMessagePrompt(result: MatchMonitoringResult): String =
        buildString {
            append("Generate a short reminder message to pending players. ")
            append("Keep it friendly and action-oriented. ")
            append("Mention ACCEPT or DECLINE response.\n")
            append("Match: ${result.matchTitle} (${result.sportType})\n")
            append("Venue: ${result.venueName}\n")
            append("Kickoff: ${result.scheduledStartTime}")
        }

    fun buildUpdateMessagePrompt(result: MatchMonitoringResult): String =
        buildString {
            append("Generate a concise update message for invited players. ")
            append("Mention current participation risk and possible reschedule planning.\n")
            append("Match: ${result.matchTitle}\n")
            append("Confirmed ${result.confirmedPlayersCount}/${result.requiredPlayers}, ")
            append("pending ${result.pendingPlayersCount}, declined ${result.declinedPlayersCount}\n")
            append("Reason: ${result.reason}")
        }
}

