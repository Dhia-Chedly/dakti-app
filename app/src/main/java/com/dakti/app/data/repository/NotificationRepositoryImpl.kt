package com.dakti.app.data.repository

import com.dakti.app.domain.repository.NotificationRepository
import com.dakti.app.util.Resource
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor() : NotificationRepository {
    override suspend fun syncNotificationPreferences(): Resource<Unit> = Resource.Success(Unit)
}
