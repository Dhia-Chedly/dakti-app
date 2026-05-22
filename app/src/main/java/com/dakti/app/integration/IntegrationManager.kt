package com.dakti.app.integration

import android.content.Context
import android.content.Intent
import com.dakti.app.integration.calendar.CalendarIntegration
import com.dakti.app.integration.dialer.DialerIntegration
import com.dakti.app.integration.email.EmailIntegration
import com.dakti.app.integration.maps.MapsIntegration
import com.dakti.app.integration.whatsapp.WhatsAppIntegration
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IntegrationManager @Inject constructor(
    private val whatsappIntegration: WhatsAppIntegration,
    private val emailIntegration: EmailIntegration,
    private val mapsIntegration: MapsIntegration,
    private val calendarIntegration: CalendarIntegration,
    private val dialerIntegration: DialerIntegration
) {

    fun launchWhatsApp(
        context: Context,
        payload: ShareMessagePayload
    ): ExternalLaunchResult {
        val primaryIntent = whatsappIntegration.buildIntent(payload)
            ?: return ExternalLaunchResult.Failed("Message content is empty.")

        if (tryLaunchIntent(context, primaryIntent)) {
            return ExternalLaunchResult.Launched()
        }

        val fallback = whatsappIntegration.buildShareFallbackIntent(payload)
            ?: return ExternalLaunchResult.Failed("Message content is empty.")
        return if (tryLaunchIntent(context, fallback)) {
            ExternalLaunchResult.Launched(
                message = "WhatsApp is unavailable. Opened the share sheet instead."
            )
        } else {
            ExternalLaunchResult.Failed("No compatible app found for sharing this message.")
        }
    }

    fun launchEmail(
        context: Context,
        payload: EmailPayload
    ): ExternalLaunchResult {
        val intent = emailIntegration.buildIntent(payload)
            ?: return ExternalLaunchResult.Failed("Email subject or body is required.")
        return launchOrFailure(
            context = context,
            intent = intent,
            failureMessage = "No email app is available on this device."
        )
    }

    fun launchDialer(
        context: Context,
        payload: DialerPayload
    ): ExternalLaunchResult {
        val intent = dialerIntegration.buildIntent(payload)
            ?: return ExternalLaunchResult.Failed("Venue phone number is missing or invalid.")
        return launchOrFailure(
            context = context,
            intent = intent,
            failureMessage = "No dialer app is available on this device."
        )
    }

    fun launchMaps(
        context: Context,
        payload: VenueLocationPayload
    ): ExternalLaunchResult {
        val googleIntent = mapsIntegration.buildGoogleMapsIntent(payload)
        val genericIntent = mapsIntegration.buildGenericMapsIntent(payload)

        if (googleIntent == null && genericIntent == null) {
            return ExternalLaunchResult.Failed("Venue location is unavailable.")
        }

        if (googleIntent != null && tryLaunchIntent(context, googleIntent)) {
            return ExternalLaunchResult.Launched()
        }

        if (genericIntent != null && tryLaunchIntent(context, genericIntent)) {
            return ExternalLaunchResult.Launched(
                message = "Google Maps is unavailable. Opened another maps app."
            )
        }

        return ExternalLaunchResult.Failed("No maps app is available on this device.")
    }

    fun launchCalendar(
        context: Context,
        payload: CalendarEventPayload
    ): ExternalLaunchResult {
        val intent = calendarIntegration.buildIntent(payload)
            ?: return ExternalLaunchResult.Failed("Match schedule is missing or invalid for calendar.")
        return launchOrFailure(
            context = context,
            intent = intent,
            failureMessage = "No calendar app is available on this device."
        )
    }

    private fun launchOrFailure(
        context: Context,
        intent: Intent,
        failureMessage: String
    ): ExternalLaunchResult {
        return if (tryLaunchIntent(context, intent)) {
            ExternalLaunchResult.Launched()
        } else {
            ExternalLaunchResult.Failed(failureMessage)
        }
    }

    private fun tryLaunchIntent(
        context: Context,
        intent: Intent
    ): Boolean {
        return runCatching {
            val launchIntent = Intent(intent).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            true
        }.getOrDefault(false)
    }
}
