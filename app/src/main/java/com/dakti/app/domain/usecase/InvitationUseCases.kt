package com.dakti.app.domain.usecase

import com.dakti.app.domain.model.InvitationResponseStatus
import com.dakti.app.domain.repository.InvitationRepository
import javax.inject.Inject

class InvitePlayersUseCase @Inject constructor(
    private val invitationRepository: InvitationRepository
) {
    suspend operator fun invoke(
        matchId: String,
        playerIds: List<String>,
        message: String?
    ) = invitationRepository.invitePlayers(
        matchId = matchId,
        playerIds = playerIds,
        message = message
    )
}

class GetPlayerInvitationsUseCase @Inject constructor(
    private val invitationRepository: InvitationRepository
) {
    suspend operator fun invoke() = invitationRepository.getInvitationsForCurrentPlayer()
}

class GetMatchInvitationsUseCase @Inject constructor(
    private val invitationRepository: InvitationRepository
) {
    suspend operator fun invoke(matchId: String) = invitationRepository.getInvitationsForMatch(matchId)
}

class GetInviteCandidatesUseCase @Inject constructor(
    private val invitationRepository: InvitationRepository
) {
    suspend operator fun invoke(matchId: String) = invitationRepository.getInviteCandidates(matchId)
}

class RespondToInvitationUseCase @Inject constructor(
    private val invitationRepository: InvitationRepository
) {
    suspend operator fun invoke(
        invitationId: String,
        status: InvitationResponseStatus
    ) = invitationRepository.respondToInvitation(
        invitationId = invitationId,
        status = status
    )
}

