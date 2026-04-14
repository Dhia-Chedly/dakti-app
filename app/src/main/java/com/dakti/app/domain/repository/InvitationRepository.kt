package com.dakti.app.domain.repository

import com.dakti.app.domain.model.Invitation
import com.dakti.app.util.Resource
import kotlinx.coroutines.flow.Flow

interface InvitationRepository {
    suspend fun getInvitations(): Resource<List<Invitation>>
    suspend fun respondToInvitation(invitationId: String, accepted: Boolean): Resource<Unit>

    fun observeInvitationsByPlayer(playerId: String): Flow<List<Invitation>>
    fun observeInvitationsByMatch(matchId: String): Flow<List<Invitation>>
    suspend fun saveInvitation(invitation: Invitation): Resource<Invitation>
}
