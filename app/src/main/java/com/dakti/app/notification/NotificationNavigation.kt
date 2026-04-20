package com.dakti.app.notification

import android.content.Intent

object NotificationNavigation {
    const val EXTRA_TARGET_ROUTE: String = "notification_target_route"
    const val EXTRA_MATCH_ID: String = "notification_match_id"
    const val EXTRA_RESERVATION_ID: String = "notification_reservation_id"
    const val EXTRA_INVITATION_ID: String = "notification_invitation_id"

    fun extractTargetRoute(intent: Intent?): String? {
        return intent
            ?.getStringExtra(EXTRA_TARGET_ROUTE)
            ?.takeIf { value -> value.isNotBlank() }
    }
}
