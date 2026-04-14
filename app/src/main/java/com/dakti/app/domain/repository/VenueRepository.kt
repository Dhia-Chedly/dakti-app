package com.dakti.app.domain.repository

import com.dakti.app.domain.model.Venue
import com.dakti.app.util.Resource

interface VenueRepository {
    suspend fun getVenues(): Resource<List<Venue>>
    suspend fun getVenueDetails(venueId: String): Resource<Venue>
}
