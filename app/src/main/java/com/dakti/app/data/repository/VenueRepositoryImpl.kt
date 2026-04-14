package com.dakti.app.data.repository

import com.dakti.app.data.local.dao.VenueDao
import com.dakti.app.data.mapper.toDomain
import com.dakti.app.data.mapper.toEntity
import com.dakti.app.domain.model.TimeSlot
import com.dakti.app.domain.model.Venue
import com.dakti.app.domain.model.VenueWithTimeSlots
import com.dakti.app.domain.repository.VenueRepository
import com.dakti.app.util.Resource
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class VenueRepositoryImpl @Inject constructor(
    private val venueDao: VenueDao
) : VenueRepository {

    override suspend fun getVenues(): Resource<List<Venue>> {
        seedIfEmpty()
        return Resource.Success(venueDao.getVenuesOnce().map { entity -> entity.toDomain() })
    }

    override suspend fun getVenueDetails(venueId: String): Resource<Venue> {
        seedIfEmpty()
        val venue = venueDao.getVenueById(venueId)?.toDomain()
            ?: return Resource.Error("Venue not found")
        return Resource.Success(venue)
    }

    override fun observeVenues(): Flow<List<Venue>> =
        venueDao.observeVenues().map { entities -> entities.map { entity -> entity.toDomain() } }

    override fun observeVenueWithSlots(venueId: String): Flow<VenueWithTimeSlots?> =
        venueDao.observeVenueWithTimeSlots(venueId).map { relation -> relation?.toDomain() }

    override suspend fun upsertVenue(venue: Venue): Resource<Unit> {
        venueDao.upsertVenue(venue.toEntity())
        return Resource.Success(Unit)
    }

    override suspend fun upsertTimeSlots(slots: List<TimeSlot>): Resource<Unit> {
        venueDao.upsertTimeSlots(slots.map { slot -> slot.toEntity() })
        return Resource.Success(Unit)
    }

    private suspend fun seedIfEmpty() {
        if (venueDao.getVenuesOnce().isNotEmpty()) {
            return
        }

        val now = Instant.now()
        val venues = listOf(
            Venue(
                id = "venue-1",
                name = "Central Football Arena",
                sportType = "Football",
                description = "Outdoor 5-a-side pitch with lights.",
                address = "15 Unity Avenue",
                city = "Lagos",
                state = "Lagos",
                country = "Nigeria",
                latitude = 6.5244,
                longitude = 3.3792,
                pricePerHour = 12000.0,
                currency = "NGN",
                amenities = listOf("Parking", "Floodlights", "Changing room"),
                createdAt = now,
                updatedAt = now
            ),
            Venue(
                id = "venue-2",
                name = "City Padel Hub",
                sportType = "Padel",
                description = "Indoor padel courts with lounge area.",
                address = "42 Marina Road",
                city = "Lagos",
                state = "Lagos",
                country = "Nigeria",
                latitude = 6.4498,
                longitude = 3.3995,
                pricePerHour = 18000.0,
                currency = "NGN",
                amenities = listOf("Locker", "Cafe", "Showers"),
                createdAt = now,
                updatedAt = now
            ),
            Venue(
                id = "venue-3",
                name = "North Tennis Club",
                sportType = "Tennis",
                description = "Hard courts for training and friendlies.",
                address = "9 Airport Link",
                city = "Abuja",
                state = "FCT",
                country = "Nigeria",
                latitude = 9.0765,
                longitude = 7.3986,
                pricePerHour = 15000.0,
                currency = "NGN",
                amenities = listOf("Ball machine", "Pro shop"),
                createdAt = now,
                updatedAt = now
            )
        )

        venueDao.upsertVenues(venues.map { venue -> venue.toEntity() })

        val slots = venues.flatMapIndexed { index, venue ->
            val baseStart = now.plusSeconds((index + 1L) * 7200L)
            listOf(
                TimeSlot(
                    id = "slot-${UUID.randomUUID()}",
                    venueId = venue.id,
                    startTime = baseStart,
                    endTime = baseStart.plusSeconds(90L * 60L),
                    isAvailable = true,
                    capacity = 14
                ),
                TimeSlot(
                    id = "slot-${UUID.randomUUID()}",
                    venueId = venue.id,
                    startTime = baseStart.plusSeconds(2L * 3600L),
                    endTime = baseStart.plusSeconds((2L * 3600L) + (90L * 60L)),
                    isAvailable = true,
                    capacity = 14
                )
            )
        }

        venueDao.upsertTimeSlots(slots.map { slot -> slot.toEntity() })
    }
}
