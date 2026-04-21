package com.dakti.app.domain.repository

import com.dakti.app.domain.model.Notification
import com.dakti.app.domain.model.MonitoringAlert
import com.dakti.app.domain.model.Reservation
import com.dakti.app.util.Resource
import java.time.Instant
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    suspend fun syncNotificationPreferences(): Resource<Unit>

    fun observeNotifications(userId: String): Flow<List<Notification>>
    suspend fun saveNotification(notification: Notification): Resource<Unit>
    suspend fun markAsRead(notificationId: String): Resource<Unit>

    suspend fun sendReservationConfirmationNotification(
        reservation: Reservation
    ): Resource<Unit>

    suspend fun sendMatchReminderNotification(matchId: String): Resource<Unit>

    suspend fun sendInvitationReminderNotification(matchId: String): Resource<Unit>

    suspend fun sendMatchUpdatedNotification(
        matchId: String,
        updateMessage: String? = null
    ): Resource<Unit>

    suspend fun sendMatchMonitoringAlertNotification(
        alert: MonitoringAlert
    ): Resource<Unit>

    suspend fun scheduleMatchReminder(
        matchId: String,
        scheduledStartTime: Instant
    ): Resource<Unit>

    suspend fun cancelMatchReminder(matchId: String): Resource<Unit>

    suspend fun scheduleInvitationReminder(matchId: String): Resource<Unit>

    suspend fun cancelInvitationReminder(matchId: String): Resource<Unit>

    suspend fun scheduleMatchReadinessMonitoring(
        matchId: String,
        scheduledStartTime: Instant
    ): Resource<Unit>

    suspend fun cancelMatchReadinessMonitoring(matchId: String): Resource<Unit>
}
