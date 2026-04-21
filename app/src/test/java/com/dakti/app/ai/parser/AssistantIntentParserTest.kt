package com.dakti.app.ai.parser

import com.dakti.app.domain.model.AssistantIntent
import java.time.DayOfWeek
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AssistantIntentParserTest {

    private val parser = AssistantIntentParser()

    @Test
    fun parse_organizeMatchRequest_extractsIntentSportPlayersAndTime() {
        val result = parser.parse("Organize a football match for Saturday at 6 PM for 10 players")

        assertEquals(AssistantIntent.ORGANIZE_MATCH, result.intent)
        assertEquals("Football", result.sportType)
        assertEquals(10, result.desiredPlayers)
        assertNotNull(result.preferredDateTime)
        val resolved = result.preferredDateTime!!.atZone(ZoneId.systemDefault())
        assertEquals(18, resolved.hour)
        assertEquals(DayOfWeek.SATURDAY, resolved.dayOfWeek)
    }

    @Test
    fun parse_invitationMessageRequest_detectsInvitationIntent() {
        val result = parser.parse("Generate invitation message for my match tomorrow")

        assertEquals(AssistantIntent.GENERATE_INVITATION_MESSAGE, result.intent)
        assertNotNull(result.preferredDateTime)
    }

    @Test
    fun parse_rescheduleHelp_detectsRescheduleIntent() {
        val result = parser.parse("My match is at risk, help me reschedule")

        assertEquals(AssistantIntent.RESCHEDULE_HELP, result.intent)
        assertTrue(result.rawText.contains("reschedule", ignoreCase = true))
    }
}
