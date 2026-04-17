package com.dakti.app.data.mapper

import com.dakti.app.data.remote.dto.TimeSlotDto
import com.dakti.app.data.remote.dto.VenueDetailsDto
import com.dakti.app.data.remote.dto.VenueDto
import com.dakti.app.domain.model.TimeSlot
import com.dakti.app.domain.model.Venue
import com.dakti.app.domain.model.VenueWithTimeSlots
import java.time.Instant

fun VenueDto.toDomain(now: Instant = Instant.now()): Venue =
    Venue(
        id = id,
        name = name,
        sportType = sportType,
        description = description,
        address = address,
        contactPhone = contactPhone,
        imageUrl = imageUrl,
        city = city,
        state = state,
        country = country,
        latitude = latitude,
        longitude = longitude,
        pricePerHour = pricePerHour,
        currency = currency,
        amenities = amenities,
        createdAt = now,
        updatedAt = now
    )

fun TimeSlotDto.toDomain(): TimeSlot =
    TimeSlot(
        id = id,
        venueId = venueId,
        startTime = startTimeIso.toInstantOrNow(),
        endTime = endTimeIso.toInstantOrNow(),
        isAvailable = isAvailable,
        capacity = capacity
    )

fun VenueDetailsDto.toDomain(now: Instant = Instant.now()): VenueWithTimeSlots =
    VenueWithTimeSlots(
        venue = venue.toDomain(now = now),
        slots = timeSlots.map { slot -> slot.toDomain() }
    )

private fun String.toInstantOrNow(): Instant =
    runCatching { Instant.parse(this) }.getOrElse { Instant.now() }
