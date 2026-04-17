package com.dakti.app.domain.usecase

import com.dakti.app.domain.repository.VenueRepository
import javax.inject.Inject

class GetVenuesUseCase @Inject constructor(
    private val venueRepository: VenueRepository
) {
    suspend operator fun invoke() = venueRepository.searchVenues(query = "", sportType = null)
}

class SearchVenuesUseCase @Inject constructor(
    private val venueRepository: VenueRepository
) {
    suspend operator fun invoke(
        query: String,
        sportType: String?
    ) = venueRepository.searchVenues(query = query, sportType = sportType)
}

class GetVenueDetailsUseCase @Inject constructor(
    private val venueRepository: VenueRepository
) {
    suspend operator fun invoke(venueId: String) = venueRepository.getVenueWithTimeSlots(venueId)
}

class GetVenueSportTypesUseCase @Inject constructor(
    private val venueRepository: VenueRepository
) {
    suspend operator fun invoke() = venueRepository.getSportTypes()
}
