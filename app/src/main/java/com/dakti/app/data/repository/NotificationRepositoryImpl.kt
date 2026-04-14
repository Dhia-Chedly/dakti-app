package com.dakti.app.data.repository

import com.dakti.app.data.local.dao.NotificationDao
import com.dakti.app.data.mapper.toDomain
import com.dakti.app.data.mapper.toEntity
import com.dakti.app.domain.model.Notification
import com.dakti.app.domain.repository.NotificationRepository
import com.dakti.app.util.Resource
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NotificationRepositoryImpl @Inject constructor(
    private val notificationDao: NotificationDao
) : NotificationRepository {

    override suspend fun syncNotificationPreferences(): Resource<Unit> = Resource.Success(Unit)

    override fun observeNotifications(userId: String): Flow<List<Notification>> =
        notificationDao.observeNotificationsByUser(userId)
            .map { entities -> entities.map { entity -> entity.toDomain() } }

    override suspend fun saveNotification(notification: Notification): Resource<Unit> {
        notificationDao.upsertNotification(notification.toEntity())
        return Resource.Success(Unit)
    }

    override suspend fun markAsRead(notificationId: String): Resource<Unit> {
        notificationDao.markAsRead(
            notificationId = notificationId,
            readAt = Instant.now().toEpochMilli()
        )
        return Resource.Success(Unit)
    }
}
