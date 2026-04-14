package com.dakti.app.domain.repository

import com.dakti.app.domain.model.Invitation
import com.dakti.app.util.Resource

interface InvitationRepository {
    suspend fun getInvitations(): Resource<List<Invitation>>
    suspend fun respondToInvitation(invitationId: String, accepted: Boolean): Resource<Unit>
}
