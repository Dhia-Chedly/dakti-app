package com.dakti.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dakti.app.domain.repository.AssistantRepository
import com.dakti.app.domain.repository.NotificationRepository
import com.dakti.app.util.Resource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class MatchReadinessMonitoringWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val assistantRepository: AssistantRepository,
    private val notificationRepository: NotificationRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val matchId = inputData.getString(WorkerConstants.INPUT_MATCH_ID)
            ?.takeIf { value -> value.isNotBlank() }

        return if (matchId != null) {
            evaluateSingleMatch(matchId)
        } else {
            evaluateOrganizerMatches()
        }
    }

    private suspend fun evaluateSingleMatch(matchId: String): Result {
        return when (val alertResult = assistantRepository.monitorMatchAndBuildAlert(matchId)) {
            is Resource.Success -> {
                val alert = alertResult.data
                if (alert != null) {
                    notificationRepository.sendMatchMonitoringAlertNotification(alert)
                }
                Result.success()
            }

            is Resource.Error -> Result.success()
            Resource.Loading -> Result.retry()
        }
    }

    private suspend fun evaluateOrganizerMatches(): Result {
        return when (val results = assistantRepository.evaluateMyMatchReadiness()) {
            is Resource.Success -> {
                results.data
                    .filter { result -> result.shouldAlertOrganizer }
                    .take(MAX_ALERTS_PER_RUN)
                    .forEach { result ->
                        val alert = assistantRepository.monitorMatchAndBuildAlert(result.matchId)
                        if (alert is Resource.Success && alert.data != null) {
                            notificationRepository.sendMatchMonitoringAlertNotification(alert.data)
                        }
                    }
                Result.success()
            }

            is Resource.Error -> Result.success()
            Resource.Loading -> Result.retry()
        }
    }

    private companion object {
        private const val MAX_ALERTS_PER_RUN: Int = 2
    }
}

