package com.dakti.app.ai.suggestion

import com.dakti.app.domain.model.MatchWithContext
import com.dakti.app.domain.model.ReschedulingSuggestion
import com.dakti.app.domain.repository.VenueRepository
import com.dakti.app.util.Resource
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

class ReschedulingSuggestionEngine @Inject constructor(
    private val venueRepository: VenueRepository
) {

    suspend fun suggestAlternatives(
        match: MatchWithContext,
        limit: Int = DEFAULT_LIMIT
    ): List<ReschedulingSuggestion> {
        val venueResult = venueRepository.searchVenues(
            query = "",
            sportType = match.match.sportType
        )
        val venuesWithSlots = (venueResult as? Resource.Success)?.data.orEmpty()
        if (venuesWithSlots.isEmpty()) {
            return emptyList()
        }

        val targetStart = match.match.scheduledStartTime
        val minStart = targetStart.plus(Duration.ofHours(MIN_HOURS_AFTER_CURRENT))
        val maxStart = targetStart.plus(Duration.ofDays(MAX_DAYS_AHEAD))

        return venuesWithSlots
            .flatMap { relation ->
                relation.slots
                    .asSequence()
                    .filter { slot -> slot.isAvailable }
                    .filter { slot -> slot.startTime.isAfter(minStart) && slot.startTime.isBefore(maxStart) }
                    .map { slot ->
                        val isSameVenue = relation.venue.id == match.match.venueId
                        val timeGapMinutes = kotlin.math.abs(Duration.between(targetStart, slot.startTime).toMinutes())
                        val venuePenalty = if (isSameVenue) 0L else VENUE_CHANGE_PENALTY_MINUTES
                        val score = timeGapMinutes + venuePenalty
                        score to ReschedulingSuggestion(
                            id = "rs-${UUID.randomUUID()}",
                            venueId = relation.venue.id,
                            venueName = relation.venue.name,
                            venueAddress = relation.venue.address,
                            timeSlotId = slot.id,
                            timeSlotLabel = formatSlotLabel(slot.startTime, slot.endTime),
                            startTime = slot.startTime,
                            endTime = slot.endTime,
                            reason = if (isSameVenue) {
                                "Same venue option with a later available slot."
                            } else {
                                "Alternative venue with an available matching-time slot."
                            }
                        )
                    }
                    .toList()
            }
            .sortedBy { (score, _) -> score }
            .map { (_, suggestion) -> suggestion }
            .take(limit)
    }

    private fun formatSlotLabel(
        start: Instant,
        end: Instant
    ): String {
        val zoneId = ZoneId.systemDefault()
        val startText = start.atZone(zoneId).format(startFormatter)
        val endText = end.atZone(zoneId).format(endFormatter)
        return "$startText - $endText"
    }

    private companion object {
        private const val DEFAULT_LIMIT: Int = 3
        private const val MIN_HOURS_AFTER_CURRENT: Long = 1
        private const val MAX_DAYS_AHEAD: Long = 7
        private const val VENUE_CHANGE_PENALTY_MINUTES: Long = 120
        private val startFormatter: DateTimeFormatter =
            DateTimeFormatter.ofPattern("EEE, d MMM HH:mm", Locale.getDefault())
        private val endFormatter: DateTimeFormatter =
            DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    }
}

