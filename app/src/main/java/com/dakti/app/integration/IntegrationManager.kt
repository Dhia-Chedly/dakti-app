package com.dakti.app.integration

import com.dakti.app.integration.calendar.CalendarIntegration
import com.dakti.app.integration.dialer.DialerIntegration
import com.dakti.app.integration.email.EmailIntegration
import com.dakti.app.integration.maps.MapsIntegration
import com.dakti.app.integration.whatsapp.WhatsAppIntegration
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IntegrationManager @Inject constructor(
    val whatsappIntegration: WhatsAppIntegration,
    val emailIntegration: EmailIntegration,
    val mapsIntegration: MapsIntegration,
    val calendarIntegration: CalendarIntegration,
    val dialerIntegration: DialerIntegration
)
