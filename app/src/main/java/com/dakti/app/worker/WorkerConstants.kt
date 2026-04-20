package com.dakti.app.worker

import java.time.Duration

object WorkerConstants {
    const val INPUT_MATCH_ID: String = "input_match_id"

    val MATCH_REMINDER_LEAD_TIME: Duration = Duration.ofHours(2)
    val INVITATION_REMINDER_DELAY: Duration = Duration.ofHours(6)

    fun matchReminderWorkName(matchId: String): String = "match_reminder_$matchId"
    fun invitationReminderWorkName(matchId: String): String = "invitation_reminder_$matchId"
}
