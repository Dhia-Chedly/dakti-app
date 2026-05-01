package com.dakti.app.data.repository

import com.dakti.app.data.remote.supabase.SupabaseRemoteDataSource
import com.dakti.app.data.remote.supabase.model.TimeSlotRowDto
import com.dakti.app.data.remote.supabase.model.VenueRowDto
import com.dakti.app.domain.model.TimeSlot
import com.dakti.app.domain.model.Venue
import com.dakti.app.domain.model.VenueWithTimeSlots
import com.dakti.app.domain.repository.VenueRepository
import com.dakti.app.util.Resource
import java.time.Instant
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

@Singleton
class VenueRepositoryImpl @Inject constructor(
    private val supabaseRemoteDataSource: SupabaseRemoteDataSource
) : VenueRepository {

    private val venuesCache = MutableStateFlow<List<Venue>>(emptyList())
    private val venueWithSlotsCache = MutableStateFlow<Map<String, VenueWithTimeSlots>>(emptyMap())

    override suspend fun getVenues(): Resource<List<Venue>> {
        return runCatching {
            val venues = supabaseRemoteDataSource.getVenues(queryText = "", sportType = null)
                .map { dto -> dto.toDomain() }
            venuesCache.value = venues
            Resource.Success(venues)
        }.getOrElse { error ->
            Resource.Error(error.message ?: "Could not fetch venues")
        }
    }

    override suspend fun getVenueDetails(venueId: String): Resource<Venue> {
        return runCatching {
            val venue = supabaseRemoteDataSource.getVenueById(venueId)?.toDomain()
                ?: return@runCatching Resource.Error("Venue not found")
            Resource.Success(venue)
        }.getOrElse { error ->
            Resource.Error(error.message ?: "Could not fetch venue")
        }
    }

    override suspend fun searchVenues(
        query: String,
        sportType: String?
    ): Resource<List<VenueWithTimeSlots>> {
        return runCatching {
            val venues = supabaseRemoteDataSource.getVenues(queryText = query, sportType = sportType)
            val byVenue = supabaseRemoteDataSource.selectTimeSlots()
                .groupBy { row -> row.venueId }

            val result = venues.map { venueRow ->
                VenueWithTimeSlots(
                    venue = venueRow.toDomain(),
                    slots = byVenue[venueRow.id].orEmpty().map { slot -> slot.toDomain() }
                )
            }

            venuesCache.value = result.map { item -> item.venue }
            venueWithSlotsCache.value = result.associateBy { item -> item.venue.id }
            Resource.Success(result)
        }.getOrElse { error ->
            Resource.Error(error.message ?: "Could not search venues")
        }
    }

    override suspend fun getVenueWithTimeSlots(venueId: String): Resource<VenueWithTimeSlots> {
        return runCatching {
            val venue = supabaseRemoteDataSource.getVenueById(venueId)
                ?: return@runCatching Resource.Error("Venue not found")
            val slots = supabaseRemoteDataSource.selectTimeSlots(filters = mapOf("venue_id" to "eq.$venueId"))
            val domain = VenueWithTimeSlots(
                venue = venue.toDomain(),
                slots = slots.map { dto -> dto.toDomain() }
            )
            venueWithSlotsCache.value = venueWithSlotsCache.value + (venueId to domain)
            Resource.Success(domain)
        }.getOrElse { error ->
            Resource.Error(error.message ?: "Could not fetch venue details")
        }
    }

    override suspend fun getSportTypes(): Resource<List<String>> {
        return runCatching {
            val sportTypes = supabaseRemoteDataSource.getVenues(queryText = "", sportType = null)
                .map { dto -> dto.sportType }
                .distinct()
                .sorted()
            Resource.Success(sportTypes)
        }.getOrElse { error ->
            Resource.Error(error.message ?: "Could not fetch sport types")
        }
    }

    override fun observeVenues(): Flow<List<Venue>> = venuesCache.asStateFlow()

    override fun observeVenueWithSlots(venueId: String): Flow<VenueWithTimeSlots?> =
        venueWithSlotsCache
            .asStateFlow()
            .map { cache -> cache[venueId] }

    override suspend fun upsertVenue(venue: Venue): Resource<Unit> {
        return runCatching {
            supabaseRemoteDataSource.upsertVenue(
                payload = mapOf(
                    "id" to venue.id.ifBlank { UUID.randomUUID().toString() },
                    "name" to venue.name,
                    "sport_type" to venue.sportType,
                    "address" to venue.address,
                    "latitude" to venue.latitude,
                    "longitude" to venue.longitude,
                    "contact_number" to venue.contactPhone,
                    "description" to venue.description,
                    "capacity" to venue.amenities.size.coerceAtLeast(DEFAULT_CAPACITY)
                )
            )
            Resource.Success(Unit)
        }.getOrElse { error ->
            Resource.Error(error.message ?: "Could not save venue")
        }
    }

    override suspend fun upsertTimeSlots(slots: List<TimeSlot>): Resource<Unit> {
        return runCatching {
            supabaseRemoteDataSource.upsertTimeSlots(
                payload = slots.map { slot ->
                    mapOf(
                        "id" to slot.id.ifBlank { UUID.randomUUID().toString() },
                        "venue_id" to slot.venueId,
                        "start_time" to slot.startTime.toString(),
                        "end_time" to slot.endTime.toString(),
                        "is_available" to slot.isAvailable
                    )
                }
            )
            Resource.Success(Unit)
        }.getOrElse { error ->
            Resource.Error(error.message ?: "Could not save time slots")
        }
    }

    private fun VenueRowDto.toDomain(): Venue {
        val inferredCity = address.substringAfterLast(",", missingDelimiterValue = "").trim()
            .ifBlank { "Unknown" }

        return Venue(
            id = id,
            name = name,
            sportType = sportType,
            description = description,
            address = address,
            contactPhone = contactNumber,
            imageUrl = null,
            city = inferredCity,
            state = null,
            country = "Nigeria",
            latitude = latitude,
            longitude = longitude,
            pricePerHour = 0.0,
            currency = "NGN",
            amenities = listOf("Capacity: $capacity"),
            createdAt = createdAt.toInstantOrNow(),
            updatedAt = createdAt.toInstantOrNow()
        )
    }

    private fun TimeSlotRowDto.toDomain(): TimeSlot = TimeSlot(
        id = id,
        venueId = venueId,
        startTime = startTime.toInstantOrNow(),
        endTime = endTime.toInstantOrNow(),
        isAvailable = isAvailable,
        capacity = null
    )

    private fun String.toInstantOrNow(): Instant =
        runCatching { Instant.parse(this) }.getOrElse { Instant.now() }

    private companion object {
        private const val DEFAULT_CAPACITY: Int = 10
    }
}