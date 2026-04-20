package com.dakti.app.integration

import java.time.Instant

data class ShareMessagePayload(
    val text: String,
    val phoneNumber: String? = null
)

data class EmailPayload(
    val recipients: List<String> = emptyList(),
    val subject: String,
    val body: String
)

data class DialerPayload(
    val phoneNumber: String
)

data class VenueLocationPayload(
    val venueName: String?,
    val address: String?,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class CalendarEventPayload(
    val title: String,
    val description: String?,
    val location: String?,
    val startTime: Instant,
    val endTime: Instant
)

sealed interface ExternalLaunchResult {
    data class Launched(
        val message: String? = null
    ) : ExternalLaunchResult

    data class Failed(
        val reason: String
    ) : ExternalLaunchResult
}

fun ExternalLaunchResult.toUserMessageOrNull(): String? =
    when (this) {
        is ExternalLaunchResult.Launched -> message
        is ExternalLaunchResult.Failed -> reason
    }
