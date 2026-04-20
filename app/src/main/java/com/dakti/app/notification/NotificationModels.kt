package com.dakti.app.notification

enum class AppNotificationCategory {
    RESERVATION_CONFIRMATION,
    MATCH_REMINDER,
    INVITATION_REMINDER,
    MATCH_UPDATE
}

data class AppNotificationPayload(
    val stableKey: String,
    val category: AppNotificationCategory,
    val title: String,
    val body: String,
    val targetRoute: String? = null,
    val relatedMatchId: String? = null,
    val relatedReservationId: String? = null,
    val relatedInvitationId: String? = null
)

sealed interface NotificationDispatchResult {
    data object Dispatched : NotificationDispatchResult
    data class Failed(
        val reason: String
    ) : NotificationDispatchResult
}
