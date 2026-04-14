package com.dakti.app.domain.repository

import com.dakti.app.domain.model.Notification
import com.dakti.app.util.Resource
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    suspend fun syncNotificationPreferences(): Resource<Unit>

    fun observeNotifications(userId: String): Flow<List<Notification>>
    suspend fun saveNotification(notification: Notification): Resource<Unit>
    suspend fun markAsRead(notificationId: String): Resource<Unit>
}
