package com.dakti.app.notification

object NotificationChannels {
    const val RESERVATION_CHANNEL_ID: String = "dakti_reservation_channel"
    const val MATCH_CHANNEL_ID: String = "dakti_match_channel"
    const val INVITATION_CHANNEL_ID: String = "dakti_invitation_channel"
    const val MONITORING_CHANNEL_ID: String = "dakti_monitoring_channel"

    const val RESERVATION_CHANNEL_NAME: String = "Reservations"
    const val MATCH_CHANNEL_NAME: String = "Matches"
    const val INVITATION_CHANNEL_NAME: String = "Invitations"
    const val MONITORING_CHANNEL_NAME: String = "Monitoring Alerts"

    const val RESERVATION_CHANNEL_DESC: String = "Reservation confirmations and booking updates"
    const val MATCH_CHANNEL_DESC: String = "Match reminders and match updates"
    const val INVITATION_CHANNEL_DESC: String = "Invitation reminders and response nudges"
    const val MONITORING_CHANNEL_DESC: String = "AI-supported match readiness alerts"
}
