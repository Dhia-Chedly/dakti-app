package com.dakti.app.domain.repository

import com.dakti.app.domain.model.TimeSlot
import com.dakti.app.domain.model.Venue
import com.dakti.app.domain.model.VenueWithTimeSlots
import com.dakti.app.util.Resource
import kotlinx.coroutines.flow.Flow

interface VenueRepository {
    suspend fun getVenues(): Resource<List<Venue>>
    suspend fun getVenueDetails(venueId: String): Resource<Venue>
    suspend fun searchVenues(
        query: String,
        sportType: String? = null
    ): Resource<List<VenueWithTimeSlots>>
    suspend fun getVenueWithTimeSlots(venueId: String): Resource<VenueWithTimeSlots>
    suspend fun getSportTypes(): Resource<List<String>>

    fun observeVenues(): Flow<List<Venue>>
    fun observeVenueWithSlots(venueId: String): Flow<VenueWithTimeSlots?>

    suspend fun upsertVenue(venue: Venue): Resource<Unit>
    suspend fun upsertTimeSlots(slots: List<TimeSlot>): Resource<Unit>
}
