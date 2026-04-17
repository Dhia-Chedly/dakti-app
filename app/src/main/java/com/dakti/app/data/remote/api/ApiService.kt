package com.dakti.app.data.remote.api

import com.dakti.app.data.remote.dto.VenueDetailsDto
import com.dakti.app.data.remote.dto.VenueDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("venues")
    suspend fun getVenues(
        @Query("query") query: String? = null,
        @Query("sportType") sportType: String? = null
    ): List<VenueDto>

    @GET("venues/{venueId}")
    suspend fun getVenueDetails(
        @Path("venueId") venueId: String
    ): VenueDetailsDto
}
