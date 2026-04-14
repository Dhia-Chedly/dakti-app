package com.dakti.app.notification

import android.content.Context
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    private val context: Context
) {
    fun showPlaceholderNotification() {
        // Notification channels and actual notifications are part of future phases.
    }
}
