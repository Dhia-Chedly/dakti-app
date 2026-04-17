package com.dakti.app.domain.repository

import com.dakti.app.domain.model.InvitationResponseStatus
import com.dakti.app.domain.model.InvitationWithContext
import com.dakti.app.domain.model.InvitePlayerCandidate
import com.dakti.app.util.Resource

interface InvitationRepository {
    suspend fun getInvitationsForCurrentPlayer(): Resource<List<InvitationWithContext>>
    suspend fun getInvitationsForMatch(matchId: String): Resource<List<InvitationWithContext>>
    suspend fun getInviteCandidates(matchId: String): Resource<List<InvitePlayerCandidate>>
    suspend fun invitePlayers(
        matchId: String,
        playerIds: List<String>,
        message: String?
    ): Resource<Int>
    suspend fun respondToInvitation(
        invitationId: String,
        status: InvitationResponseStatus
    ): Resource<Unit>
}
