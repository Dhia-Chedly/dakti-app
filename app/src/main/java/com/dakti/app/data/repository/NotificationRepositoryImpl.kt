package com.dakti.app.data.repository

import com.dakti.app.data.local.dao.InvitationDao
import com.dakti.app.data.local.dao.MatchDao
import com.dakti.app.data.local.dao.NotificationDao
import com.dakti.app.data.local.session.SessionLocalDataSource
import com.dakti.app.data.mapper.toDomain
import com.dakti.app.data.mapper.toEntity
import com.dakti.app.domain.model.Notification
import com.dakti.app.domain.model.NotificationType
import com.dakti.app.domain.model.Reservation
import com.dakti.app.domain.model.MatchStatus
import com.dakti.app.domain.model.MonitoringAlert
import com.dakti.app.domain.repository.NotificationRepository
import com.dakti.app.notification.AppNotificationCategory
import com.dakti.app.notification.AppNotificationPayload
import com.dakti.app.notification.NotificationDispatchResult
import com.dakti.app.util.Resource
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import com.dakti.app.notification.NotificationHelper
import com.dakti.app.ui.navigation.AppRoute
import com.dakti.app.worker.NotificationWorkScheduler

class NotificationRepositoryImpl @Inject constructor(
    private val notificationDao: NotificationDao,
    private val matchDao: MatchDao,
    private val invitationDao: InvitationDao,
    private val sessionLocalDataSource: SessionLocalDataSource,
    private val notificationHelper: NotificationHelper,
    private val workScheduler: NotificationWorkScheduler
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

    override suspend fun sendReservationConfirmationNotification(
        reservation: Reservation
    ): Resource<Unit> {
        val payload = AppNotificationPayload(
            stableKey = "reservation_${reservation.id}",
            category = AppNotificationCategory.RESERVATION_CONFIRMATION,
            title = "Reservation Confirmed",
            body = "${reservation.venueName} - ${reservation.timeSlot}",
            targetRoute = AppRoute.MyReservations.route,
            relatedReservationId = reservation.id
        )

        val dispatchResult = notificationHelper.dispatch(payload)
        persistNotification(
            userId = reservation.organizerId,
            type = NotificationType.RESERVATION_UPDATE,
            title = payload.title,
            content = payload.body,
            relatedMatchId = null,
            relatedReservationId = reservation.id
        )

        return dispatchResult.toResource()
    }

    override suspend fun sendMatchReminderNotification(matchId: String): Resource<Unit> {
        val relation = matchDao.getMatchWithContextById(matchId)
            ?: return Resource.Success(Unit)

        val match = relation.match
        if (match.status == MatchStatus.CANCELLED || match.status == MatchStatus.COMPLETED) {
            return Resource.Success(Unit)
        }
        if (match.scheduledStartTime <= Instant.now().toEpochMilli()) {
            return Resource.Success(Unit)
        }

        val payload = AppNotificationPayload(
            stableKey = "match_reminder_${match.id}",
            category = AppNotificationCategory.MATCH_REMINDER,
            title = "Match Reminder",
            body = "${match.title} starts at ${formatDateTime(match.scheduledStartTime)} at ${relation.venue.name}.",
            targetRoute = AppRoute.MatchDetails.create(match.id),
            relatedMatchId = match.id
        )

        val dispatchResult = notificationHelper.dispatch(payload)
        persistNotification(
            userId = match.organizerId,
            type = NotificationType.MATCH_UPDATE,
            title = payload.title,
            content = payload.body,
            relatedMatchId = match.id,
            relatedReservationId = match.reservationId
        )

        return dispatchResult.toResource()
    }

    override suspend fun sendInvitationReminderNotification(matchId: String): Resource<Unit> {
        val relation = matchDao.getMatchWithContextById(matchId)
            ?: return Resource.Success(Unit)
        val pendingCount = invitationDao.countPendingInvitationsForMatch(matchId)
        if (pendingCount <= 0) {
            return Resource.Success(Unit)
        }

        val payload = AppNotificationPayload(
            stableKey = "invitation_reminder_${relation.match.id}",
            category = AppNotificationCategory.INVITATION_REMINDER,
            title = "Pending Invitations",
            body = "$pendingCount invitation response(s) are still pending for ${relation.match.title}.",
            targetRoute = AppRoute.InvitePlayers.create(relation.match.id),
            relatedMatchId = relation.match.id
        )

        val dispatchResult = notificationHelper.dispatch(payload)
        persistNotification(
            userId = relation.match.organizerId,
            type = NotificationType.INVITATION_RESPONSE,
            title = payload.title,
            content = payload.body,
            relatedMatchId = relation.match.id,
            relatedReservationId = relation.match.reservationId
        )

        return dispatchResult.toResource()
    }

    override suspend fun sendMatchUpdatedNotification(
        matchId: String,
        updateMessage: String?
    ): Resource<Unit> {
        val relation = matchDao.getMatchWithContextById(matchId)
            ?: return Resource.Success(Unit)
        val body = updateMessage
            ?.trim()
            ?.takeIf { value -> value.isNotBlank() }
            ?: "Match details were updated for ${relation.match.title}."

        val payload = AppNotificationPayload(
            stableKey = "match_update_${relation.match.id}_${relation.match.updatedAt}",
            category = AppNotificationCategory.MATCH_UPDATE,
            title = "Match Update",
            body = body,
            targetRoute = AppRoute.MatchDetails.create(relation.match.id),
            relatedMatchId = relation.match.id
        )

        val dispatchResult = notificationHelper.dispatch(payload)
        persistNotification(
            userId = relation.match.organizerId,
            type = NotificationType.MATCH_UPDATE,
            title = payload.title,
            content = payload.body,
            relatedMatchId = relation.match.id,
            relatedReservationId = relation.match.reservationId
        )

        return dispatchResult.toResource()
    }

    override suspend fun sendMatchMonitoringAlertNotification(
        alert: MonitoringAlert
    ): Resource<Unit> {
        val payload = AppNotificationPayload(
            stableKey = "match_monitoring_${alert.matchId}_${alert.status.name}",
            category = AppNotificationCategory.MATCH_MONITORING_ALERT,
            title = alert.title,
            body = alert.body,
            targetRoute = AppRoute.MatchDetails.create(alert.matchId),
            relatedMatchId = alert.matchId
        )

        val dispatchResult = notificationHelper.dispatch(payload)
        val relatedMatch = matchDao.getMatchById(alert.matchId)
        persistNotification(
            userId = relatedMatch?.organizerId,
            type = NotificationType.SYSTEM_ALERT,
            title = payload.title,
            content = payload.body,
            relatedMatchId = alert.matchId,
            relatedReservationId = relatedMatch?.reservationId
        )
        return dispatchResult.toResource()
    }

    override suspend fun scheduleMatchReminder(
        matchId: String,
        scheduledStartTime: Instant
    ): Resource<Unit> = withContext(Dispatchers.Default) {
        if (matchId.isBlank()) {
            return@withContext Resource.Error("Match id is required.")
        }
        if (!scheduledStartTime.isAfter(Instant.now())) {
            return@withContext Resource.Error("Match time has already passed.")
        }
        workScheduler.scheduleMatchReminder(matchId, scheduledStartTime)
        Resource.Success(Unit)
    }

    override suspend fun cancelMatchReminder(matchId: String): Resource<Unit> =
        withContext(Dispatchers.Default) {
            if (matchId.isBlank()) {
                return@withContext Resource.Error("Match id is required.")
            }
            workScheduler.cancelMatchReminder(matchId)
            Resource.Success(Unit)
        }

    override suspend fun scheduleInvitationReminder(matchId: String): Resource<Unit> =
        withContext(Dispatchers.Default) {
            if (matchId.isBlank()) {
                return@withContext Resource.Error("Match id is required.")
            }
            workScheduler.scheduleInvitationReminder(matchId)
            Resource.Success(Unit)
        }

    override suspend fun cancelInvitationReminder(matchId: String): Resource<Unit> =
        withContext(Dispatchers.Default) {
            if (matchId.isBlank()) {
                return@withContext Resource.Error("Match id is required.")
            }
            workScheduler.cancelInvitationReminder(matchId)
            Resource.Success(Unit)
        }

    override suspend fun scheduleMatchReadinessMonitoring(
        matchId: String,
        scheduledStartTime: Instant
    ): Resource<Unit> = withContext(Dispatchers.Default) {
        if (matchId.isBlank()) {
            return@withContext Resource.Error("Match id is required.")
        }
        if (!scheduledStartTime.isAfter(Instant.now())) {
            return@withContext Resource.Error("Match time has already passed.")
        }
        workScheduler.scheduleMatchReadinessMonitoring(
            matchId = matchId,
            scheduledStartTime = scheduledStartTime
        )
        Resource.Success(Unit)
    }

    override suspend fun cancelMatchReadinessMonitoring(matchId: String): Resource<Unit> =
        withContext(Dispatchers.Default) {
            if (matchId.isBlank()) {
                return@withContext Resource.Error("Match id is required.")
            }
            workScheduler.cancelMatchReadinessMonitoring(matchId)
            Resource.Success(Unit)
        }

    private suspend fun persistNotification(
        userId: String?,
        type: NotificationType,
        title: String,
        content: String,
        relatedMatchId: String?,
        relatedReservationId: String?
    ) {
        val targetUserId = userId
            ?.takeIf { value -> value.isNotBlank() }
            ?: sessionLocalDataSource.authenticatedUserId.value
            ?.takeIf { value -> value.isNotBlank() }
            ?: return

        notificationDao.upsertNotification(
            Notification(
                id = "notif-${UUID.randomUUID()}",
                userId = targetUserId,
                type = type,
                title = title,
                content = content,
                isRead = false,
                relatedMatchId = relatedMatchId,
                relatedReservationId = relatedReservationId,
                createdAt = Instant.now(),
                readAt = null
            ).toEntity()
        )
    }

    private fun NotificationDispatchResult.toResource(): Resource<Unit> =
        when (this) {
            NotificationDispatchResult.Dispatched -> Resource.Success(Unit)
            is NotificationDispatchResult.Failed -> Resource.Error(reason)
        }

    private fun formatDateTime(epochMillis: Long): String {
        return Instant.ofEpochMilli(epochMillis)
            .atZone(ZoneId.systemDefault())
            .format(dateTimeFormatter)
    }

    private companion object {
        private val dateTimeFormatter: DateTimeFormatter =
            DateTimeFormatter.ofPattern("EEE, d MMM HH:mm")
    }
}
