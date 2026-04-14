package com.dakti.app.domain.repository

import com.dakti.app.domain.model.TimeSlot
import com.dakti.app.domain.model.Venue
import com.dakti.app.domain.model.VenueWithTimeSlots
import com.dakti.app.util.Resource
import kotlinx.coroutines.flow.Flow

interface VenueRepository {
    suspend fun getVenues(): Resource<List<Venue>>
    suspend fun getVenueDetails(venueId: String): Resource<Venue>

    fun observeVenues(): Flow<List<Venue>>
    fun observeVenueWithSlots(venueId: String): Flow<VenueWithTimeSlots?>

    suspend fun upsertVenue(venue: Venue): Resource<Unit>
    suspend fun upsertTimeSlots(slots: List<TimeSlot>): Resource<Unit>
}
