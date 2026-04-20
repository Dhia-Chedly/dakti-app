package com.dakti.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dakti.app.domain.repository.NotificationRepository
import com.dakti.app.util.Resource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class MatchReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationRepository: NotificationRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val matchId = inputData.getString(WorkerConstants.INPUT_MATCH_ID)
            ?.takeIf { value -> value.isNotBlank() }
            ?: return Result.failure()

        return when (notificationRepository.sendMatchReminderNotification(matchId)) {
            is Resource.Success -> Result.success()
            is Resource.Error -> Result.success()
            Resource.Loading -> Result.retry()
        }
    }
}
