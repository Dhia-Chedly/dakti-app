package com.dakti.app.data.repository

import com.dakti.app.domain.model.Invitation
import com.dakti.app.domain.repository.InvitationRepository
import com.dakti.app.util.Resource
import javax.inject.Inject

class InvitationRepositoryImpl @Inject constructor() : InvitationRepository {
    override suspend fun getInvitations(): Resource<List<Invitation>> {
        return Resource.Success(
            listOf(
                Invitation(id = "inv-1", matchTitle = "Friday Night 5v5", fromUser = "Amine"),
                Invitation(id = "inv-2", matchTitle = "Padel Partners", fromUser = "Lina")
            )
        )
    }

    override suspend fun respondToInvitation(invitationId: String, accepted: Boolean): Resource<Unit> {
        return Resource.Success(Unit)
    }
}
