package com.dakti.app.integration.calendar

import android.content.Intent
import android.provider.CalendarContract
import com.dakti.app.integration.CalendarEventPayload
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarIntegration @Inject constructor() {

    fun buildIntent(payload: CalendarEventPayload): Intent? {
        if (payload.title.isBlank()) {
            return null
        }
        if (!payload.endTime.isAfter(payload.startTime)) {
            return null
        }

        return Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, payload.title)
            putExtra(CalendarContract.Events.DESCRIPTION, payload.description)
            putExtra(CalendarContract.Events.EVENT_LOCATION, payload.location)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, payload.startTime.toEpochMilli())
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, payload.endTime.toEpochMilli())
        }
    }
}
