package com.dakti.app.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationWorkScheduler @Inject constructor(
    @ApplicationContext context: Context
) {

    private val workManager: WorkManager = WorkManager.getInstance(context)

    fun scheduleMatchReminder(
        matchId: String,
        scheduledStartTime: Instant
    ) {
        val triggerTime = scheduledStartTime.minus(WorkerConstants.MATCH_REMINDER_LEAD_TIME)
        val delay = Duration.between(Instant.now(), triggerTime)
            .coerceAtLeast(Duration.ZERO)

        val request = OneTimeWorkRequestBuilder<MatchReminderWorker>()
            .setInputData(workDataOf(WorkerConstants.INPUT_MATCH_ID to matchId))
            .setInitialDelay(delay.toMillis(), TimeUnit.MILLISECONDS)
            .addTag(WorkerConstants.matchReminderWorkName(matchId))
            .build()

        workManager.enqueueUniqueWork(
            WorkerConstants.matchReminderWorkName(matchId),
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancelMatchReminder(matchId: String) {
        workManager.cancelUniqueWork(WorkerConstants.matchReminderWorkName(matchId))
    }

    fun scheduleInvitationReminder(matchId: String) {
        val request = OneTimeWorkRequestBuilder<InvitationReminderWorker>()
            .setInputData(workDataOf(WorkerConstants.INPUT_MATCH_ID to matchId))
            .setInitialDelay(
                WorkerConstants.INVITATION_REMINDER_DELAY.toMillis(),
                TimeUnit.MILLISECONDS
            )
            .addTag(WorkerConstants.invitationReminderWorkName(matchId))
            .build()

        workManager.enqueueUniqueWork(
            WorkerConstants.invitationReminderWorkName(matchId),
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancelInvitationReminder(matchId: String) {
        workManager.cancelUniqueWork(WorkerConstants.invitationReminderWorkName(matchId))
    }

    fun scheduleMatchReadinessMonitoring(
        matchId: String,
        scheduledStartTime: Instant
    ) {
        scheduleSingleMonitoringCheck(
            matchId = matchId,
            workName = WorkerConstants.matchMonitoringEarlyWorkName(matchId),
            checkpoint = "early",
            triggerTime = scheduledStartTime.minus(WorkerConstants.MATCH_MONITORING_FIRST_CHECK_LEAD_TIME)
        )
        scheduleSingleMonitoringCheck(
            matchId = matchId,
            workName = WorkerConstants.matchMonitoringLateWorkName(matchId),
            checkpoint = "late",
            triggerTime = scheduledStartTime.minus(WorkerConstants.MATCH_MONITORING_SECOND_CHECK_LEAD_TIME)
        )
    }

    fun cancelMatchReadinessMonitoring(matchId: String) {
        workManager.cancelUniqueWork(WorkerConstants.matchMonitoringEarlyWorkName(matchId))
        workManager.cancelUniqueWork(WorkerConstants.matchMonitoringLateWorkName(matchId))
    }

    fun ensurePeriodicMatchMonitoring() {
        val request = PeriodicWorkRequestBuilder<MatchReadinessMonitoringWorker>(
            WorkerConstants.PERIODIC_MONITORING_REPEAT_INTERVAL.toHours(),
            TimeUnit.HOURS
        )
            .addTag(WorkerConstants.PERIODIC_MATCH_MONITORING_WORK_NAME)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WorkerConstants.PERIODIC_MATCH_MONITORING_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun scheduleSingleMonitoringCheck(
        matchId: String,
        workName: String,
        checkpoint: String,
        triggerTime: Instant
    ) {
        val delay = Duration.between(Instant.now(), triggerTime)
            .coerceAtLeast(Duration.ZERO)

        val request = OneTimeWorkRequestBuilder<MatchReadinessMonitoringWorker>()
            .setInputData(
                workDataOf(
                    WorkerConstants.INPUT_MATCH_ID to matchId,
                    WorkerConstants.INPUT_MONITORING_CHECKPOINT to checkpoint
                )
            )
            .setInitialDelay(delay.toMillis(), TimeUnit.MILLISECONDS)
            .addTag(workName)
            .build()

        workManager.enqueueUniqueWork(
            workName,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}
