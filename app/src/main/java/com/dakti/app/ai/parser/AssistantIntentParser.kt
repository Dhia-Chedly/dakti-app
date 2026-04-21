package com.dakti.app.ai.parser

import com.dakti.app.domain.model.AssistantContext
import com.dakti.app.domain.model.AssistantIntent
import com.dakti.app.domain.model.AssistantStructuredRequest
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Locale
import javax.inject.Inject

class AssistantIntentParser @Inject constructor() {

    fun parse(
        rawText: String,
        context: AssistantContext? = null
    ): AssistantStructuredRequest {
        val normalized = rawText.trim()
        val lowered = normalized.lowercase(Locale.getDefault())

        val intent = detectIntent(lowered)
        val sportType = detectSportType(lowered)
        val desiredPlayers = detectPlayerCount(lowered)
        val preferredDateTime = detectPreferredDateTime(
            lowered = lowered,
            defaultHour = if (intent == AssistantIntent.ORGANIZE_MATCH) DEFAULT_MATCH_HOUR else null
        )
        val venuePreference = detectVenuePreference(normalized)

        return AssistantStructuredRequest(
            rawText = normalized,
            intent = intent,
            sportType = sportType,
            preferredDateTime = preferredDateTime,
            desiredPlayers = desiredPlayers,
            venuePreference = venuePreference,
            targetMatchId = context?.matchId,
            context = context
        )
    }

    private fun detectIntent(lowered: String): AssistantIntent {
        val hasOrganize = lowered.contains("organize") || lowered.contains("plan") || lowered.contains("setup")
        val hasMatch = lowered.contains("match") || lowered.contains("game")
        val hasVenue = lowered.contains("venue") || lowered.contains("field") || lowered.contains("court")
        val hasAlternative = lowered.contains("alternative") ||
            lowered.contains("another time") ||
            lowered.contains("different time") ||
            lowered.contains("unavailable") ||
            lowered.contains("else")
        val hasInvitation = lowered.contains("invitation") || lowered.contains("invite message")
        val hasReminder = lowered.contains("reminder") || lowered.contains("remind")
        val hasReschedule = lowered.contains("reschedule") || lowered.contains("postpone") || lowered.contains("move time")
        val hasReadiness = lowered.contains("readiness") ||
            lowered.contains("at risk") ||
            lowered.contains("insufficient players") ||
            lowered.contains("not enough players") ||
            lowered.contains("needs attention")

        return when {
            hasInvitation -> AssistantIntent.GENERATE_INVITATION_MESSAGE
            hasReminder -> AssistantIntent.GENERATE_REMINDER_MESSAGE
            hasReschedule || hasReadiness -> AssistantIntent.RESCHEDULE_HELP
            hasAlternative -> AssistantIntent.SUGGEST_ALTERNATIVE_SLOT
            hasOrganize && hasMatch -> AssistantIntent.ORGANIZE_MATCH
            hasVenue -> AssistantIntent.SUGGEST_VENUE
            else -> AssistantIntent.GENERAL_CHAT
        }
    }

    private fun detectSportType(lowered: String): String? {
        val sports = listOf(
            "football" to "Football",
            "soccer" to "Football",
            "basketball" to "Basketball",
            "tennis" to "Tennis",
            "volleyball" to "Volleyball"
        )
        return sports.firstOrNull { (token, _) -> lowered.contains(token) }?.second
    }

    private fun detectPlayerCount(lowered: String): Int? {
        val explicitPattern = Regex("""\b(\d{1,2})\s*(players?|people|persons?)\b""")
        explicitPattern.find(lowered)?.let { match ->
            return match.groupValues[1].toIntOrNull()
        }

        val forPattern = Regex("""\bfor\s+(\d{1,2})\b""")
        forPattern.find(lowered)?.let { match ->
            return match.groupValues[1].toIntOrNull()
        }

        return null
    }

    private fun detectPreferredDateTime(
        lowered: String,
        defaultHour: Int?
    ): Instant? {
        val zoneId = ZoneId.systemDefault()
        val nowDate = LocalDate.now(zoneId)

        val date = when {
            lowered.contains("today") -> nowDate
            lowered.contains("tomorrow") -> nowDate.plusDays(1)
            else -> detectWeekdayDate(lowered, nowDate)
        }

        val time = detectTime(lowered)
            ?: defaultHour?.let { hour -> LocalTime.of(hour, 0) }

        if (date == null && time == null) {
            return null
        }

        val resolvedDate = date ?: nowDate
        val resolvedTime = time ?: LocalTime.of(DEFAULT_FALLBACK_HOUR, 0)
        return LocalDateTime.of(resolvedDate, resolvedTime)
            .atZone(zoneId)
            .toInstant()
    }

    private fun detectWeekdayDate(
        lowered: String,
        referenceDate: LocalDate
    ): LocalDate? {
        val weekdayMap = mapOf(
            "monday" to DayOfWeek.MONDAY,
            "tuesday" to DayOfWeek.TUESDAY,
            "wednesday" to DayOfWeek.WEDNESDAY,
            "thursday" to DayOfWeek.THURSDAY,
            "friday" to DayOfWeek.FRIDAY,
            "saturday" to DayOfWeek.SATURDAY,
            "sunday" to DayOfWeek.SUNDAY
        )
        val targetDay = weekdayMap.entries.firstOrNull { (token, _) -> lowered.contains(token) }?.value
            ?: return null

        var candidate = referenceDate
        var steps = 0
        while (candidate.dayOfWeek != targetDay && steps < 8) {
            candidate = candidate.plusDays(1)
            steps += 1
        }
        return candidate
    }

    private fun detectTime(lowered: String): LocalTime? {
        val withAmPm = Regex("""\b(\d{1,2})(?::(\d{2}))?\s*(am|pm)\b""")
        withAmPm.find(lowered)?.let { match ->
            var hour = match.groupValues[1].toIntOrNull() ?: return null
            val minute = match.groupValues[2].toIntOrNull() ?: 0
            val meridiem = match.groupValues[3]

            if (meridiem == "pm" && hour < 12) {
                hour += 12
            } else if (meridiem == "am" && hour == 12) {
                hour = 0
            }
            return runCatching { LocalTime.of(hour, minute) }.getOrNull()
        }

        val military = Regex("""\b(?:at\s+)?([01]?\d|2[0-3]):([0-5]\d)\b""")
        military.find(lowered)?.let { match ->
            val hour = match.groupValues[1].toIntOrNull() ?: return null
            val minute = match.groupValues[2].toIntOrNull() ?: return null
            return runCatching { LocalTime.of(hour, minute) }.getOrNull()
        }

        return null
    }

    private fun detectVenuePreference(rawText: String): String? {
        val pattern = Regex("""(?:at|in)\s+([A-Z][a-zA-Z0-9\s]{2,30})""")
        val match = pattern.find(rawText) ?: return null
        val value = match.groupValues[1].trim()
        if (value.any { char -> char.isDigit() }) {
            return null
        }
        return value
    }

    private companion object {
        private const val DEFAULT_MATCH_HOUR: Int = 18
        private const val DEFAULT_FALLBACK_HOUR: Int = 18
    }
}
