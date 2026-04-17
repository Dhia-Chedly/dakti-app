package com.dakti.app.domain.usecase

import com.dakti.app.domain.model.MatchCreatePayload
import com.dakti.app.domain.repository.MatchRepository
import javax.inject.Inject

class CreateMatchUseCase @Inject constructor(
    private val matchRepository: MatchRepository
) {
    suspend operator fun invoke(payload: MatchCreatePayload) =
        matchRepository.createMatch(payload)
}

class CreateMatchFromReservationUseCase @Inject constructor(
    private val matchRepository: MatchRepository
) {
    suspend operator fun invoke(payload: MatchCreatePayload) =
        matchRepository.createMatch(payload)
}

class GetMyMatchesUseCase @Inject constructor(
    private val matchRepository: MatchRepository
) {
    suspend operator fun invoke() = matchRepository.getMyMatches()
}

class GetMatchDetailsUseCase @Inject constructor(
    private val matchRepository: MatchRepository
) {
    suspend operator fun invoke(matchId: String) = matchRepository.getMatchDetails(matchId)
}

class GetMatchReservationContextsUseCase @Inject constructor(
    private val matchRepository: MatchRepository
) {
    suspend operator fun invoke() = matchRepository.getReservationContextsForCurrentOrganizer()
}
