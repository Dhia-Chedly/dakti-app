package com.dakti.app.data.repository

import com.dakti.app.domain.model.Venue
import com.dakti.app.domain.repository.VenueRepository
import com.dakti.app.util.Resource
import javax.inject.Inject

class VenueRepositoryImpl @Inject constructor() : VenueRepository {
    override suspend fun getVenues(): Resource<List<Venue>> {
        return Resource.Success(
            listOf(
                Venue(id = "venue-1", name = "Central Football Arena", sportType = "Football"),
                Venue(id = "venue-2", name = "City Padel Hub", sportType = "Padel"),
                Venue(id = "venue-3", name = "North Tennis Club", sportType = "Tennis")
            )
        )
    }

    override suspend fun getVenueDetails(venueId: String): Resource<Venue> {
        return Resource.Success(
            Venue(
                id = venueId,
                name = "Placeholder Venue $venueId",
                sportType = "Mixed"
            )
        )
    }
}
