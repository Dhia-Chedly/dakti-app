package com.dakti.app.domain.repository

import com.dakti.app.util.Resource

interface NotificationRepository {
    suspend fun syncNotificationPreferences(): Resource<Unit>
}
