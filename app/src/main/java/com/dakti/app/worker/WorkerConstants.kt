package com.dakti.app.worker

import java.time.Duration

object WorkerConstants {
    const val INPUT_MATCH_ID: String = "input_match_id"
    const val INPUT_MONITORING_CHECKPOINT: String = "input_monitoring_checkpoint"

    val MATCH_REMINDER_LEAD_TIME: Duration = Duration.ofHours(2)
    val INVITATION_REMINDER_DELAY: Duration = Duration.ofHours(6)
    val MATCH_MONITORING_FIRST_CHECK_LEAD_TIME: Duration = Duration.ofHours(24)
    val MATCH_MONITORING_SECOND_CHECK_LEAD_TIME: Duration = Duration.ofHours(3)
    val PERIODIC_MONITORING_REPEAT_INTERVAL: Duration = Duration.ofHours(6)

    fun matchReminderWorkName(matchId: String): String = "match_reminder_$matchId"
    fun invitationReminderWorkName(matchId: String): String = "invitation_reminder_$matchId"
    fun matchMonitoringEarlyWorkName(matchId: String): String = "match_monitoring_early_$matchId"
    fun matchMonitoringLateWorkName(matchId: String): String = "match_monitoring_late_$matchId"

    const val PERIODIC_MATCH_MONITORING_WORK_NAME: String = "periodic_match_monitoring"
}
