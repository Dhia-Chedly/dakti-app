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

    override suspend fun searchVenues(
        query: String,
        sportType: String?
    ): Resource<List<VenueWithTimeSlots>> {
        seedIfEmpty()
        val results = venueDao.searchVenuesWithTimeSlots(
            query = query.trim(),
            sportType = sportType?.trim()?.takeIf { value -> value.isNotEmpty() }
        ).map { relation -> relation.toDomain() }
        return Resource.Success(results)
    }

    override suspend fun getVenueWithTimeSlots(venueId: String): Resource<VenueWithTimeSlots> {
        seedIfEmpty()
        val venue = venueDao.getVenueWithTimeSlotsOnce(venueId)?.toDomain()
            ?: return Resource.Error("Venue details unavailable")
        return Resource.Success(venue)
    }

    override suspend fun getSportTypes(): Resource<List<String>> {
        seedIfEmpty()
        return Resource.Success(venueDao.getSportTypes())
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
                id = "venue-football-001",
                name = "Central Football Arena",
                sportType = "Football",
                description = "Synthetic 5-a-side pitch with floodlights and changing rooms.",
                address = "15 Unity Avenue",
                contactPhone = "+234-803-000-1001",
                imageUrl = null,
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
                id = "venue-basketball-001",
                name = "Metro Basketball Court",
                sportType = "Basketball",
                description = "Indoor full court with spectator seats and scoreboard.",
                address = "42 Marina Road",
                contactPhone = "+234-803-000-1002",
                imageUrl = null,
                city = "Lagos",
                state = "Lagos",
                country = "Nigeria",
                latitude = 6.4498,
                longitude = 3.3995,
                pricePerHour = 15000.0,
                currency = "NGN",
                amenities = listOf("Locker room", "Scoreboard", "Water station"),
                createdAt = now,
                updatedAt = now
            ),
            Venue(
                id = "venue-tennis-001",
                name = "Northside Tennis Club",
                sportType = "Tennis",
                description = "Hard courts suitable for training, doubles, and friendly sets.",
                address = "9 Airport Link",
                contactPhone = "+234-803-000-1003",
                imageUrl = null,
                city = "Abuja",
                state = "FCT",
                country = "Nigeria",
                latitude = 9.0765,
                longitude = 7.3986,
                pricePerHour = 14000.0,
                currency = "NGN",
                amenities = listOf("Pro shop", "Ball machine", "Rest area"),
                createdAt = now,
                updatedAt = now
            )
        )

        venueDao.upsertVenues(venues.map { venue -> venue.toEntity() })

        val baseStart = now.plusSeconds(6L * 3600L)
        val slots = listOf(
            buildSlot("slot-football-1", "venue-football-001", baseStart, 90L, true, 14),
            buildSlot("slot-football-2", "venue-football-001", baseStart.plusSeconds(2L * 3600L), 90L, false, 14),
            buildSlot("slot-football-3", "venue-football-001", baseStart.plusSeconds(4L * 3600L), 90L, true, 14),
            buildSlot("slot-basketball-1", "venue-basketball-001", baseStart.plusSeconds(3600L), 120L, true, 10),
            buildSlot("slot-basketball-2", "venue-basketball-001", baseStart.plusSeconds(3L * 3600L), 120L, false, 10),
            buildSlot("slot-basketball-3", "venue-basketball-001", baseStart.plusSeconds(5L * 3600L), 120L, true, 10),
            buildSlot("slot-tennis-1", "venue-tennis-001", baseStart.plusSeconds(30L * 60L), 60L, false, 4),
            buildSlot("slot-tennis-2", "venue-tennis-001", baseStart.plusSeconds(2L * 3600L), 60L, true, 4),
            buildSlot("slot-tennis-3", "venue-tennis-001", baseStart.plusSeconds(6L * 3600L), 60L, true, 4)
        )

        venueDao.upsertTimeSlots(slots.map { slot -> slot.toEntity() })
    }

    private fun buildSlot(
        id: String,
        venueId: String,
        startTime: Instant,
        durationMinutes: Long,
        isAvailable: Boolean,
        capacity: Int
    ): TimeSlot {
        val endTime = startTime.plusSeconds(durationMinutes * 60L)
        return TimeSlot(
            id = id,
            venueId = venueId,
            startTime = startTime,
            endTime = endTime,
            isAvailable = isAvailable,
            capacity = capacity
        )
    }
}
