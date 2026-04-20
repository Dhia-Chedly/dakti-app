package com.dakti.app.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dakti.app.MainActivity
import com.dakti.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channels = listOf(
            NotificationChannel(
                NotificationChannels.RESERVATION_CHANNEL_ID,
                NotificationChannels.RESERVATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = NotificationChannels.RESERVATION_CHANNEL_DESC
            },
            NotificationChannel(
                NotificationChannels.MATCH_CHANNEL_ID,
                NotificationChannels.MATCH_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = NotificationChannels.MATCH_CHANNEL_DESC
            },
            NotificationChannel(
                NotificationChannels.INVITATION_CHANNEL_ID,
                NotificationChannels.INVITATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = NotificationChannels.INVITATION_CHANNEL_DESC
            }
        )

        manager.createNotificationChannels(channels)
    }

    fun dispatch(payload: AppNotificationPayload): NotificationDispatchResult {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            return NotificationDispatchResult.Failed("Notifications are disabled on this device.")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                return NotificationDispatchResult.Failed("Notification permission is not granted.")
            }
        }

        val clickIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(NotificationNavigation.EXTRA_TARGET_ROUTE, payload.targetRoute)
            putExtra(NotificationNavigation.EXTRA_MATCH_ID, payload.relatedMatchId)
            putExtra(NotificationNavigation.EXTRA_RESERVATION_ID, payload.relatedReservationId)
            putExtra(NotificationNavigation.EXTRA_INVITATION_ID, payload.relatedInvitationId)
        }

        val requestCode = payload.stableKey.hashCode()
        val pendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            clickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, payload.channelId())
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(payload.title)
            .setContentText(payload.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(payload.body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        return runCatching {
            NotificationManagerCompat.from(context)
                .notify(requestCode, notification)
            NotificationDispatchResult.Dispatched
        }.getOrElse {
            NotificationDispatchResult.Failed("Could not show notification right now.")
        }
    }

    private fun AppNotificationPayload.channelId(): String {
        return when (category) {
            AppNotificationCategory.RESERVATION_CONFIRMATION -> NotificationChannels.RESERVATION_CHANNEL_ID
            AppNotificationCategory.MATCH_REMINDER,
            AppNotificationCategory.MATCH_UPDATE -> NotificationChannels.MATCH_CHANNEL_ID
            AppNotificationCategory.INVITATION_REMINDER -> NotificationChannels.INVITATION_CHANNEL_ID
        }
    }
}
