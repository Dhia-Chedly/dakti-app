package com.dakti.app.domain.usecase

import com.dakti.app.domain.model.Notification
import com.dakti.app.domain.model.MonitoringAlert
import com.dakti.app.domain.model.Reservation
import com.dakti.app.domain.repository.NotificationRepository
import java.time.Instant
import javax.inject.Inject

class ObserveNotificationsUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    operator fun invoke(userId: String) = notificationRepository.observeNotifications(userId)
}

class SaveNotificationUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(notification: Notification) =
        notificationRepository.saveNotification(notification)
}

class MarkNotificationReadUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(notificationId: String) =
        notificationRepository.markAsRead(notificationId)
}

class SendReservationConfirmationNotificationUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(reservation: Reservation) =
        notificationRepository.sendReservationConfirmationNotification(reservation)
}

class ScheduleMatchReminderUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(
        matchId: String,
        scheduledStartTime: Instant
    ) = notificationRepository.scheduleMatchReminder(
        matchId = matchId,
        scheduledStartTime = scheduledStartTime
    )
}

class ScheduleInvitationReminderUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(matchId: String) =
        notificationRepository.scheduleInvitationReminder(matchId)
}

class NotifyMatchUpdatedUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(
        matchId: String,
        updateMessage: String? = null
    ) = notificationRepository.sendMatchUpdatedNotification(
        matchId = matchId,
        updateMessage = updateMessage
    )
}

class SendMatchMonitoringAlertNotificationUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(alert: MonitoringAlert) =
        notificationRepository.sendMatchMonitoringAlertNotification(alert)
}

class ScheduleMatchReadinessMonitoringUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(
        matchId: String,
        scheduledStartTime: Instant
    ) = notificationRepository.scheduleMatchReadinessMonitoring(
        matchId = matchId,
        scheduledStartTime = scheduledStartTime
    )
}

class CancelMatchReadinessMonitoringUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(matchId: String) =
        notificationRepository.cancelMatchReadinessMonitoring(matchId)
}
