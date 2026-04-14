package com.dakti.app.domain.usecase

import com.dakti.app.domain.repository.AssistantRepository
import com.dakti.app.domain.repository.VenueRepository
import javax.inject.Inject

class GetVenuesUseCase @Inject constructor(
    private val venueRepository: VenueRepository
) {
    suspend operator fun invoke() = venueRepository.getVenues()
}

class AskAssistantUseCase @Inject constructor(
    private val assistantRepository: AssistantRepository
) {
    suspend operator fun invoke(prompt: String) = assistantRepository.askAssistant(prompt)
}
